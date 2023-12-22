package com.senzing.api.model;

/**
 * The state of a bulk data operation.
 */
public enum SzBulkDataStatus {
  /**
   * The bulk data operation has not yet started.
   */
  NOT_STARTED,

  /**
   * The bulk data operation is in progress.
   */
  IN_PROGRESS,

  /**
   * The bulk data operation has been aborted.
   */
  ABORTED,

  /**
   * The bulk data operation has completed normally.
   */
  COMPLETED;
}
