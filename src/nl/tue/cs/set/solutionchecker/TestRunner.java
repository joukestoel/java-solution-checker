package nl.tue.cs.set.solutionchecker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TestRunner {
  private final Path junitLibPath;

  public TestRunner(Path junitLibPath) {
    if (!Files.exists(junitLibPath)) {
      throw new IllegalArgumentException("JUnit lib not found at " + junitLibPath.toString());
    }

    this.junitLibPath = junitLibPath;
  }

  public List<TestRunResult> runTests(Path classesToBeTestedPath, List<String> testClasses) {
    if (!Files.exists(classesToBeTestedPath)) {
      throw new IllegalArgumentException("Path of test classes does not exist");
    }

    List<TestRunResult> result = new ArrayList<>();
    for (String testClass : testClasses) {
      result.add(runSingleTest(classesToBeTestedPath, testClass));
    }

    return result;
  }

  private TestRunResult runSingleTest(Path classesToBeTestedPath, String testClass) {
    ProcessBuilder pb = new ProcessBuilder("java", "-cp", classesToBeTestedPath.toString() + ":" + junitLibPath.toString(), "junit.textui.TestRunner", testClass);

    try {
      Process testResult = pb.start();
      if (!testResult.waitFor(5, TimeUnit.SECONDS)) {
        testResult.destroy();
        return TestRunResult.timeout();
      }

      if (testResult.exitValue() == 0) {
        return TestRunResult.successful(readInputStream(testResult.getInputStream()));
      } else {
        return TestRunResult.unsuccessful(readInputStream(testResult.getInputStream()), readInputStream(testResult.getErrorStream()));
      }

    } catch (InterruptedException | IOException e) {
      throw new IllegalStateException("Unable to run tests, reason: " + e.getMessage(), e);
    }
  }

  private String readInputStream(InputStream stream) {
    String output = "";
    Scanner scn = new Scanner(stream);
    while (scn.hasNext()) {
      output += scn.nextLine() + "\n";
    }
    scn.close();

    return output;
  }

  static class TestRunResult {
    private final TestRunOutcome outcome;
    private final String output;
    private final String error;

    private TestRunResult(TestRunOutcome outcome, String output, String error) {
      this.outcome = outcome;
      this.output = output;
      this.error = error;
    }

    public static TestRunResult successful(String output) {
      return new TestRunResult(TestRunOutcome.SUCCESS, output,"");
    }

    public static TestRunResult unsuccessful(String output, String error) {
      if (output == null || "".equals(output)) {
        return new TestRunResult(TestRunOutcome.ERROR, "", error);
      } else {
        return new TestRunResult(TestRunOutcome.FAILURE, output, "");
      }
    }

    public static TestRunResult timeout() {
      return new TestRunResult(TestRunOutcome.TIMEOUT, "", "");
    }

    public boolean isSuccessful() {
      return outcome == TestRunOutcome.SUCCESS;
    }

    public TestRunOutcome getOutcome() {
      return outcome;
    }

    public String getOutput() {
      return output;
    }

    public String getError() {
      return error;
    }

    @Override
    public String toString() {
      return "TestRunResult{" +
              "outcome=" + outcome +
              ", output='" + output + '\'' +
              ", error='" + error + '\'' +
              '}';
    }
  }

  enum TestRunOutcome {
    SUCCESS,FAILURE,ERROR,TIMEOUT;
  }
}
