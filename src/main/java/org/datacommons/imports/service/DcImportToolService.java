package org.datacommons.imports.service;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("run_import_tool")
public class DcImportToolService {

    // The temporary output path stores the output files by the CMD tool
    // before they're uploaded to GCS
    private static final String TMP_LOCAL_OUTPUT_PATH = "/tmp/dc-import/output/";
    // The temporary intput path stores the input files uploaded by the user
    // that will be feed into the CMD tool
    private static final String TMP_LOCAL_INPUT_PATH = "/tmp/dc-import/input/";

    private final IDcImportToolRunner importToolRunner = new JarBasedDcImportToolRunner();

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public String postIt(
        @FormDataParam("mode") String mode,
        @FormDataParam("existence-checks") String existenceChecks,
        @FormDataParam("existence-checks-place") String existenceChecksPlace,
        @FormDataParam("stat-checks") String statChecks,
        @FormDataParam("allow-non-numeric-obs-values") String allowNonNumericObsValues,
        @FormDataParam("check-measurement-result") String checkMeasurementResult,
        @FormDataParam("summary-report") String summaryReport,
        @FormDataParam("verbose") String verbose,
        @FormDataParam("sample-places") String samplePlaces,
        @FormDataParam("resolution") String resolution,
        @FormDataParam("input-files") List<FormDataBodyPart> parts) {
        String requestIdentifier = UUID.randomUUID().toString();

        // Write uploaded files to a temporary folder
        List<String> inputFiles = null;
        try {
            inputFiles = temporarilySaveUploadedFiles(parts, requestIdentifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Check configuration compatibility?
        Configuration configuration = Configuration.builder()
            .requestIdentifier(requestIdentifier)
            .mode(Mode.valueOf(mode.toUpperCase()))
            .existenceChecks(enabled(existenceChecks))
            .existenceChecksPlace(enabled(existenceChecksPlace))
            .statChecks(enabled(statChecks))
            .allowNonNumericObsValues(enabled(allowNonNumericObsValues))
            .checkMeasurementResult(enabled(checkMeasurementResult))
            .summaryReport(enabled(summaryReport))
            .verbose(enabled(verbose))
            .outputDir(TMP_LOCAL_OUTPUT_PATH + requestIdentifier)
            .samplePlaces(samplePlaces)
            .resolution(Resolution.valueOf(resolution.toUpperCase()))
            .inputFilePaths(inputFiles)
            .build();
        return importToolRunner.runDcImportTool(configuration);
    }

    private void saveFile(InputStream inputStream, String fileName) {
        try {
            File outputFile = new File(fileName);
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            FileOutputStream outputStream =
                new FileOutputStream(outputFile, false);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            // System.out.println("Wrote to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the uploaded file data, saves them locally and returns the list of
     * paths to the saved files.
     */
    private List<String> temporarilySaveUploadedFiles(
        // Need to clean up the files
        List<FormDataBodyPart> parts, String requestIdentifier) throws IOException {
        makeDirs(Arrays.asList(
            TMP_LOCAL_OUTPUT_PATH + requestIdentifier,
            TMP_LOCAL_INPUT_PATH + requestIdentifier));

        ImmutableList.Builder<String> inputPathsBuilder = new ImmutableList.Builder<>();
        for (FormDataBodyPart part : parts) {
            InputStream is = part.getEntityAs(InputStream.class);
            ContentDisposition meta = part.getContentDisposition();
            String tmpPath = TMP_LOCAL_INPUT_PATH + requestIdentifier + "/" + meta.getFileName();
            inputPathsBuilder.add(tmpPath);
            saveFile(is, tmpPath);
        }
        return inputPathsBuilder.build();
    }

    private void makeDirs(List<String> paths) throws IOException {
        for (String path : paths) {
            Files.createDirectories(Paths.get(path));
        }
    }

    private boolean enabled(String value) {
        return value != null;
    }

}
