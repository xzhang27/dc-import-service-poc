package org.datacommons.imports.service;

public interface IDcImportToolRunner {

  /**
   * Runs the import tool and returns the output of the tool.
   */
  String runDcImportTool(Configuration configuration);

}
