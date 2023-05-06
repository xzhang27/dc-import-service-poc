package org.datacommons.imports.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.datacommons.cloud.client.GcsClient;

/**
 * An implementation of {@link IDcImportToolRunner} based on the pre-packaged
 * import jar tool. It treats the jar tool as a blackbox.
 */
public class JarBasedDcImportToolRunner implements IDcImportToolRunner{

  private static final String IMPORT_TOOL_PATH =
      "/datacommons-import-tool-0.1-alpha.1-jar-with-dependencies.jar";
  // The temporary output path stores the output files by the CMD tool
  // before they're uploaded to GCS
  private static final String TMP_LOCAL_OUTPUT_PATH = "/tmp/dc-import/output/";
  // The temporary input path stores the input files uploaded by the user
  // that will be feed into the CMD tool
  private static final String TMP_LOCAL_INPUT_PATH = "/tmp/dc-import/input/";
  private static final int TIMEOUT_IN_SECONDS = 60;

  private GcsClient gcsClient;

  public JarBasedDcImportToolRunner() {
    gcsClient = new GcsClient();
  }

  @Override
  public String runDcImportTool(Configuration configuration) {
    // The request identifier will be used to separate the storage of
    // input/output files for different requests
    String requestIdentifier = UUID.randomUUID().toString();
    List<String> command = buildCommand(configuration, requestIdentifier);
    return runJarTool(command, requestIdentifier);
  }

  private String runJarTool(List<String> command, String requestIdentifier) {
    ProcessBuilder pb = new ProcessBuilder(
        command.stream()
            .filter(s -> !Strings.isNullOrEmpty(s))
            .collect(Collectors.toList()))
        .redirectErrorStream(true);

    StringBuilder responseBuilder = new StringBuilder();
    Process p;
    try {
      p = pb.start();
      p.waitFor(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
      byte[] output = p.getInputStream().readAllBytes();
      responseBuilder.append(new String(output) + "\n");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // At this point, the underlying import tool has finished running,
    // and has written the output files to local file system.
    // The next step is to read back the files and write them to GCS,
    // and then clean up files on the local file system.
    File outputDir = new File(TMP_LOCAL_OUTPUT_PATH + requestIdentifier);
    File[] files = outputDir.listFiles();
    try {
      responseBuilder.append("Result file list:\n");
      for (File file : files) {
        byte[] data = Files.readAllBytes(file.toPath());
        String publicUrl = gcsClient.upload(requestIdentifier + "-" + file.getName(), data);
        responseBuilder.append("Result file: " + publicUrl + "\n");
        file.delete(); // delete the local copy
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return responseBuilder.toString();
  }

  private List<String> buildCommand(Configuration cfg, String requestIdentifier) {
    List<String> cmdBuilder = new ArrayList<>();
    cmdBuilder.add("java");
    cmdBuilder.add("-jar");
    cmdBuilder.add(getToolPath());
    cmdBuilder.add(cfg.mode().name().toLowerCase());

    cmdBuilder.add(convertArg("existence-checks", cfg.existenceChecks()));
    cmdBuilder.add(convertArg("existence-checks-place", cfg.existenceChecksPlace()));
    cmdBuilder.add(convertArg("stat-checks", cfg.statChecks()));
    cmdBuilder.add(convertArg("allow-non-numeric-obs-values", cfg.allowNonNumericObsValues()));
    cmdBuilder.add(convertArg("check-measurement-result", cfg.checkMeasurementResult()));
    cmdBuilder.add(convertArg("summary-report", cfg.summaryReport()));
    cmdBuilder.add(convertArg("verbose", cfg.verbose()));
    cmdBuilder.add(convertArg("output-dir", TMP_LOCAL_OUTPUT_PATH + requestIdentifier));
    cmdBuilder.add(convertArg("sample-places", cfg.samplePlaces()));
    cmdBuilder.add(convertArg("resolution", cfg.resolution().name()));

    // Finally append the input file paths.
    cmdBuilder.addAll(cfg.inputFilePaths());
    return cmdBuilder;
  }

  private String getToolPath() {
    URL toolPath = getClass().getResource(
        IMPORT_TOOL_PATH);
    Preconditions.checkNotNull(toolPath,
        "Could not find import CMD tool with the name " + IMPORT_TOOL_PATH);
    return toolPath.getPath();
  }

  private String convertArg(String argName, boolean enabled) {
    return "--" + argName + "=" + enabled;
  }

  private String convertArg(String argName, String inputValue) {
    if (Strings.isNullOrEmpty(inputValue)) {
      return "";
    }
    return "--" + argName + "=" + inputValue;
  }
}
