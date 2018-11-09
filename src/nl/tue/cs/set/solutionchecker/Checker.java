package nl.tue.cs.set.solutionchecker;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Checker {
  private final List<String> javaClasses;
  private final List<Path> testClasses;
  private final Path basePath;

  private final Copier copier;
  private final Compiler compiler;
  private final TestRunner testRunner;

  public Checker(Path junitLibPath, Path basePath, List<String> javaClasses, List<Path> testClasses) {
    this.javaClasses = javaClasses;
    this.testClasses = testClasses;
    this.basePath = basePath;

    copier = new Copier(this.testClasses);
    compiler = new Compiler(junitLibPath);
    testRunner = new TestRunner(junitLibPath);
  }

  public void checkAll(Path outputDir, String csvFileName) {
    System.out.println("Start checking. Base path \'" + basePath + "\'");
    List<SubmissionCheckResult> results = new ArrayList<>();

    try {
      DirectoryStream<Path> dirs = Files.newDirectoryStream(basePath);

      for (Path dir : dirs) {
        if (Files.isDirectory(dir)) {
          results.add(checkDir(dir));
        }
      }

      System.out.println("Done, saving results");
      SubmissionResultCsvWriter csvWriter = new SubmissionResultCsvWriter();
      csvWriter.createAndSave(results, outputDir, csvFileName);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private SubmissionCheckResult checkDir(Path dir) {
    System.out.println("Checking \'" + dir + "\'");

    List<Path> testFiles = copier.copyTests(dir);
    if (testFiles == null || testFiles.isEmpty()) {
      throw new IllegalStateException("Unable to copy test classes to submission directory");
    }

    Compiler.CompileResult compileResult = compiler.compile(dir, resolveJavaClassPaths(dir, javaClasses));

    if (!compileResult.isSuccessful()) {
      return SubmissionCheckResult.compilationErrors(dir, compileResult);
    }

    Compiler.CompileResult testCompileResult = compiler.compile(dir, testFiles);
    if (!testCompileResult.isSuccessful()) {
      return SubmissionCheckResult.compilationErrorsOnTestFiles(dir, compileResult, testCompileResult);
    }

    List<TestRunner.TestRunResult> testResults = testRunner.runTests(dir, getFileNamesOnly(testClasses));
    return SubmissionCheckResult.allSuccessful(dir, compileResult, testCompileResult, testResults);
  }

  private List<Path> resolveJavaClassPaths(Path dir, List<String> javaClasses) {
    List<Path> classPaths = new ArrayList<>();
    javaClasses.forEach(jc -> classPaths.add(dir.resolve(Paths.get(jc))));

    return classPaths;
  }

  private List<String> getFileNamesOnly(List<Path> fullPaths) {
    List<String> fileNames = new ArrayList<>(fullPaths.size());
    for (Path path : fullPaths) {
      String fileName = path.getFileName().toString();
      fileNames.add(fileName.substring(0, fileName.lastIndexOf(".java")));
    }

    return fileNames;
  }

  public static void main(String[] args) {
    boolean error = false;

    if (args.length < 11) {
      System.out.println("Incorrect number of arguments");
      error = true;
    }

    String baseDir = getValue(args, "-basedir");
    String junit38xLib = getValue(args, "-junit38xlib");
    String outputDir = getValue(args, "-outputdir");
    String csvFileName = getValue(args, "-csvname");

    List<String> javaClasses = getValues(args, "-jc");
    List<String> testClassesList = getValues(args, "-tc");

    if (baseDir == null || toDir(baseDir) == null) {
      System.out.println("Base dir (-basedir) not supplied or not an (existing) directory");
      error = true;
    } else if (junit38xLib == null || toFile(junit38xLib) == null) {
      System.out.println("JUnit 3.8.x lib path (-junit38xlib) is not supplied or not an existing file");
      error = true;
    } else if (outputDir == null || toDir(outputDir) == null) {
      System.out.println("Output dir (-outputdir) not supplied or not an (existing) directory");
    } else if (csvFileName == null) {
      System.out.println("CSV file name (-csvname) not supplied");
    }

    if (error) {
      printUsage();
      System.exit(-1);
    }

    Path base = toDir(baseDir);
    Path output = toDir(outputDir);
    Path junitLib = toFile(junit38xLib);

    List<Path> testClasses = new ArrayList<>();
    for (String tc : testClassesList) {
      Path testClass = toFile(tc);

      if (testClass == null) {
        System.out.println("Test class " + tc + " is not a correct path or the file does not exist");
        printUsage();
        System.exit(-1);
      } else {
        testClasses.add(testClass);
      }
    }

    Checker checker = new Checker(junitLib, base, javaClasses, testClasses);
    checker.checkAll(output, csvFileName);
  }

  private static String getValue(String[] args, String key) {
    for (int i = 0; i < args.length-1; i+=2) {
      if (key.equals(args[i])) {
        return args[i+1];
      }
    }

    return null;
  }

  private static List<String> getValues(String[] args, String key) {
    List<String> values = new ArrayList<>();
    for (int i = 0; i < args.length-1; i+=2) {
      if (key.equals(args[i])) {
        values.add(args[i+1]);
      }
    }

    return values;
  }

  private static Path toFile(String file) {
    if (file == null) {
      return null;
    }

    Path path = Paths.get(file);
    if (Files.exists(path) && Files.isRegularFile(path)) {
      return path;
    } else {
      return null;
    }
  }

  private static Path toDir(String dir) {
    if (dir == null) {
      return null;
    }

    Path path = Paths.get(dir);
    if (Files.exists(path) && Files.isDirectory(path)) {
      return path;
    } else {
      return null;
    }
  }

  private static void printUsage() {
    System.out.println("Usage: -basedir <base directory of java classes to compile and test> -junit38xlib <path to junit 3.8.x lib> -outputdir <output dir to save csv> <java classes> <test class paths>");
    System.out.println("  where <java classes> is one or more -jc <name of java class> and <test class path> is zero or more -tc <path of test class>");
    System.out.println("  Example: -basedir ./submissions -junit38xlib lib/junit-3.8.2.jar -jc ClassOne.java -jc ClassTwo.java -tc tests/TestClassOne.java -tc tests/TestClassTwo.java");
  }
}
