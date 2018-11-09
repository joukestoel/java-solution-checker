package nl.tue.cs.set.solutionchecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class CsvWriter {
  private static final char SEP = ',';

  private final StringBuilder builder;

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm:ss");

  public CsvWriter() {
    this.builder = new StringBuilder();
  }

  public void saveFile(Path outputDir, String csvFileName) {
    if (!Files.exists(outputDir) || !Files.isDirectory(outputDir)) {
      throw new IllegalArgumentException("Output dir \'" + outputDir + "\' does not exist or is not a directory");
    }

    Path csvFile = outputDir.resolve(csvFileName);
    try {
      Files.write(csvFile, builder.toString().getBytes());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to save csv, reason: " + e.getMessage());
    }
  }

  private String quote(String s) {
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  protected CsvWriter sep() {
    builder.append(SEP);
    return this;
  }

  protected CsvWriter nl() {
    builder.append("\n");
    return this;
  }

  protected CsvWriter writeEmpty() {
    sep();
    return this;
  }

  protected CsvWriter writeEmptyLast() {
    nl();
    return this;
  }

  protected CsvWriter write(int i) {
    builder.append(i);
    sep();
    return this;
  }

  protected CsvWriter writeLast(int i) {
    builder.append(i);
    nl();
    return this;
  }

  protected CsvWriter write(String s) {
    builder.append(quote(s));
    sep();
    return this;
  }

  protected CsvWriter writeLast(String s) {
    builder.append(quote(s));
    nl();
    return this;
  }

  protected CsvWriter write(LocalDateTime d) {
    builder.append(quote(d.format(formatter)));
    sep();
    return this;
  }

  protected CsvWriter writeLast(LocalDateTime d) {
    builder.append(quote(d.format(formatter)));
    nl();
    return this;
  }

}
