package org.datacommons.imports.service;

import com.google.auto.value.AutoValue;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A container for all configurable inputs to the import tool.
 */
@AutoValue
public abstract class Configuration {

  public abstract boolean existenceChecks();
  public abstract boolean existenceChecksPlace();
  public abstract boolean statChecks();
  public abstract boolean allowNonNumericObsValues();
  public abstract boolean checkMeasurementResult();
  public abstract boolean summaryReport();
  public abstract boolean verbose();

  @Nullable public abstract String outputDir();
  @Nullable public abstract String samplePlaces();
  public abstract Resolution resolution();
  public abstract Mode mode();
  public abstract List<String> inputFilePaths();

  public abstract String requestIdentifier();

  public static Builder builder() {
    return new AutoValue_Configuration.Builder()
        .existenceChecks(true)
        .existenceChecksPlace(false)
        .statChecks(true)
        .allowNonNumericObsValues(false)
        .checkMeasurementResult(false)
        .resolution(Resolution.LOCAL)
        .summaryReport(true)
        .verbose(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder existenceChecks(boolean existenceChecks);

    public abstract Builder outputDir(String outputDir);

    public abstract Builder existenceChecksPlace(boolean existenceChecksPlace);

    public abstract Builder statChecks(boolean statChecks);

    public abstract Builder allowNonNumericObsValues(boolean allowNonNumericObsValues);

    public abstract Builder checkMeasurementResult(boolean checkMeasurementResult);

    public abstract Builder samplePlaces(String samplePlaces);

    public abstract Builder resolution(Resolution resolution);

    public abstract Builder summaryReport(boolean summaryReport);

    public abstract Builder verbose(boolean verbose);

    public abstract Builder mode(Mode mode);

    public abstract Builder inputFilePaths(List<String> inputFilePaths);

    public abstract Builder requestIdentifier(String requestIdentifier);

    abstract Configuration autoBuild();

    public final Configuration build() {
      Configuration cfg = autoBuild();
      // Add any additional configuration validations in this place
      if (cfg.samplePlaces() != null && !cfg.statChecks()) {
        throw new RuntimeException("sample-places should only be set if stat-checks is true");
      }
      return cfg;
    }

  }

  public static void main(String[] args) {
    Configuration cfg = Configuration.builder()
        .mode(Mode.LINT)
        .inputFilePaths(Arrays.asList(" "))
        .build();
    System.out.println(cfg.mode().name().toLowerCase());
  }
}

enum Resolution {
  NONE,
  LOCAL,
  FULL
}

enum Mode {
  LINT,
  GENMCF
}
