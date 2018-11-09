package nl.tue.cs.set.solutionchecker;

import java.nio.file.Path;
import java.util.List;

public class SubmissionCheckResult {
  private final Path path;
  private final Compiler.CompileResult compileResult;
  private final Compiler.CompileResult testsCompileResults;
  private final List<TestRunner.TestRunResult> testResults;

  private SubmissionCheckResult(Path path, Compiler.CompileResult compileResult, Compiler.CompileResult testsCompileResults, List<TestRunner.TestRunResult> testResults) {
    this.path = path;
    this.compileResult = compileResult;
    this.testsCompileResults = testsCompileResults;
    this.testResults = testResults;
  }

  public static SubmissionCheckResult allSuccessful(Path path, Compiler.CompileResult compileResult, Compiler.CompileResult testsCompileResults, List<TestRunner.TestRunResult> testResults) {
    return new SubmissionCheckResult(path, compileResult, testsCompileResults, testResults);
  }

  public static SubmissionCheckResult compilationErrorsOnTestFiles(Path path, Compiler.CompileResult compileResult, Compiler.CompileResult testsCompileResults) {
    return new SubmissionCheckResult(path, compileResult, testsCompileResults, null);
  }

  public static SubmissionCheckResult compilationErrors(Path path, Compiler.CompileResult compileResult) {
    return new SubmissionCheckResult(path, compileResult, null, null);
  }

  public Path getPath() {
    return path;
  }

  public Compiler.CompileResult getCompileResult() {
    return compileResult;
  }

  public Compiler.CompileResult getTestsCompileResults() {
    return testsCompileResults;
  }

  public List<TestRunner.TestRunResult> getTestResults() {
    return testResults;
  }
}
