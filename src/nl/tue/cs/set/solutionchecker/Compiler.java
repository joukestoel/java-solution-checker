package nl.tue.cs.set.solutionchecker;

import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Compiler {
  private final Path junitLibPath;

  public Compiler(Path junitLibPath) {
    if (!Files.exists(junitLibPath)) {
      throw new IllegalArgumentException("JUnit lib not found at " + junitLibPath.toString());
    }

    this.junitLibPath = junitLibPath;
  }

  public CompileResult compile(Path classpath, List<Path> javaClasses) {
    if (!Files.exists(classpath) || !Files.isDirectory(classpath)) {
      throw new IllegalArgumentException("Must provide a classpath");
    }

    if (javaClasses.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one java class name");
    }

    ByteArrayOutputStream stdOutput = new ByteArrayOutputStream();
    ByteArrayOutputStream errOutput = new ByteArrayOutputStream();

    String[] compilerCommand = new String[2 + javaClasses.size()];
    compilerCommand[0] = "-cp";
    compilerCommand[1] = junitLibPath.toString() + File.pathSeparator + classpath.toString();
    for (int i = 0; i < javaClasses.size(); i++) {
      compilerCommand[i + 2] = javaClasses.get(i).toString();
    }

    int compileResult = ToolProvider.getSystemJavaCompiler().run(null, stdOutput, errOutput, compilerCommand);

    if (compileResult == 0) {
      return CompileResult.successful(stdOutput.toString());
    } else {
      return CompileResult.unsuccessful(errOutput.toString());
    }
  }

  static class CompileResult {
    private final CompileOutcome outcome;
    private final String output;
    private final String error;

    private CompileResult(CompileOutcome outcome, String output, String error) {
      this.outcome = outcome;
      this.output = output;
      this.error = error;
    }

    public static CompileResult successful(String output) {
      return new CompileResult(CompileOutcome.SUCCESS, output, "");
    }

    public static CompileResult unsuccessful(String error) {
      return new CompileResult(CompileOutcome.ERROR, "", error);
    }

    public CompileOutcome getOutcome() {
      return outcome;
    }

    public boolean isSuccessful() {
      return outcome == CompileOutcome.SUCCESS;
    }

    public String getOutput() {
      return output;
    }

    public String getError() {
      return error;
    }

    @Override
    public String toString() {
      return "CompileResult{" +
              "outcome=" + outcome +
              ", output='" + output + '\'' +
              ", error='" + error + '\'' +
              '}';
    }
  }

  enum CompileOutcome {
    SUCCESS,ERROR;
  }
}
