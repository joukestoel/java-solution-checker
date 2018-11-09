package nl.tue.cs.set.solutionchecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Copier {
  private final List<Path> tests;

  public Copier(List<Path> tests) {
    this.tests = tests;
  }

  public List<Path> copyTests(Path toDir) {
    List<Path> copiedFiles = new ArrayList<>(tests.size());

    for (Path test : tests) {
      try {
        Path resolvedTestFile = toDir.resolve(test.getFileName());
        Files.copy(test, resolvedTestFile, StandardCopyOption.REPLACE_EXISTING);

        copiedFiles.add(resolvedTestFile);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to copy test class \'" + test + "\' to \'" + toDir + "\'");
      }
    }

    return copiedFiles;
  }
}
