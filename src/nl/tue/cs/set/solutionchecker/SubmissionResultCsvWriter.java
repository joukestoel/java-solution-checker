package nl.tue.cs.set.solutionchecker;

import java.nio.file.Path;
import java.util.List;

public class SubmissionResultCsvWriter extends CsvWriter {


  public void createAndSave(List<SubmissionCheckResult> results, Path outputDir, String csvFileName) {
    writeHeader();
    results.forEach(r -> writeRow(r));

    saveFile(outputDir, csvFileName);
  }

  private void writeHeader() {
    write("Path");
    write("Compile outcome").write("Compile output").write("Compile error output");
    write("Test file(s) compile outcome").write("Test file(s) compile output").write("Test file(s) compile error output");
    write("Test result").write("Test result output").writeLast("Test result error output");
  }

  private void writeRow(SubmissionCheckResult result) {
    write(result.getPath().toString());

    Compiler.CompileResult compileResult = result.getCompileResult();
    if (compileResult != null) {
      write(compileResult.getOutcome().toString());
      write(compileResult.getOutput());
      write(compileResult.getError());
    } else {
      writeEmpty().writeEmpty().writeEmpty();
    }

    Compiler.CompileResult testCompileResult = result.getTestsCompileResults();
    if (testCompileResult != null) {
      write(testCompileResult.getOutcome().toString());
      write(testCompileResult.getOutput());
      write(testCompileResult.getError());
    } else {
      writeEmpty().writeEmpty().writeEmpty();
    }

    List<TestRunner.TestRunResult> testResults = result.getTestResults();
    if (testResults != null && !testResults.isEmpty()) {
      write(getProminent(testResults).toString());
      write(flattenTestResultOutput(testResults));
      writeLast(flattenTestResultErrorOutput(testResults));
    } else {
      writeEmpty().writeEmpty().writeEmptyLast();
    }
  }

  private TestRunner.TestRunOutcome getProminent(List<TestRunner.TestRunResult> testResults) {
    TestRunner.TestRunOutcome overall = TestRunner.TestRunOutcome.SUCCESS;

    for (TestRunner.TestRunResult testResult : testResults) {
      if (testResult.getOutcome() == TestRunner.TestRunOutcome.ERROR) {
        return TestRunner.TestRunOutcome.ERROR;
      } else if (testResult.getOutcome() == TestRunner.TestRunOutcome.FAILURE) {
        overall = TestRunner.TestRunOutcome.FAILURE;
      }
    }

    return overall;
  }

  private String flattenTestResultOutput(List<TestRunner.TestRunResult> testRunResults) {
    StringBuilder flattened = new StringBuilder();
    testRunResults.forEach(r -> flattened.append(r.getOutput() + "\n"));
    return flattened.toString();
  }

  private String flattenTestResultErrorOutput(List<TestRunner.TestRunResult> testRunResults) {
    StringBuilder flattened = new StringBuilder();
    testRunResults.forEach(r -> flattened.append(r.getError() + "\n"));
    return flattened.toString();
  }

}
