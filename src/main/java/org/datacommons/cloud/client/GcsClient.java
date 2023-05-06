package org.datacommons.cloud.client;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;

/**
 * The client interacts with Google Cloud Storage APIs.
 */
public class GcsClient {

  private static final String DC_IMPORT_BUCKET = "dc-imports-test";
  private static final String GCS_PUBLIC_URL_PREFIX =
      "https://storage.googleapis.com/" + DC_IMPORT_BUCKET + "/";

  private final Storage storage;

  public GcsClient() {
    // Hard code to an existing project for the purpose of this task
    storage = StorageOptions.newBuilder().setProjectId("gship").build().getService();
  }

  /**
   * Uploads an object to GCS and returns the public link to the object.
   */
  public String upload(String objectName, byte[] data) throws IOException {
    BlobId blobId = BlobId.of(DC_IMPORT_BUCKET, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(getContentType(objectName)).build();
    storage.create(blobInfo, data);
    // Make the result publicly accessible
    storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
    return GCS_PUBLIC_URL_PREFIX + objectName;
  }

  private String getContentType(String objectName) {
    if (objectName.endsWith("html")) {
      return "text/html";
    } else if (objectName.endsWith("json")) {
      return "application/json";
    } else {
      return "text/plain";
    }
  }

}
