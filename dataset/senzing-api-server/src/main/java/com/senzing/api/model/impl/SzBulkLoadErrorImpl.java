package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzBulkLoadError;
import com.senzing.api.model.SzError;

/**
 * Provides a default implementation of {@link SzBulkLoadError}.
 */
@JsonDeserialize
public class SzBulkLoadErrorImpl implements SzBulkLoadError {
  /**
   * The associated error.
   */
  private SzError error;

  /**
   * The number of occurrences of the error.
   */
  private int occurrenceCount;

  /**
   * Default constructor.
   */
  public SzBulkLoadErrorImpl() {
    this(null);
  }

  /**
   * Constructs with the specified {@link SzError} describing the error that
   * occurred and an occurrence count of zero (0).
   *
   * @param error The {@link SzError} describing the error that occurred.
   */
  public SzBulkLoadErrorImpl(SzError error) {
    this(error, 0);
  }

  /**
   * Constructs with the specified {@link SzError} and the specified
   * occurrence count.
   *
   * @param error The {@link SzError} describing the error that occurred.
   *
   * @param occurrenceCount The number of times the error occurred.
   */
  public SzBulkLoadErrorImpl(SzError error, int occurrenceCount)
  {
    this.error = error;
    this.occurrenceCount = occurrenceCount;
  }

  /**
   * Gets the associated {@link SzError} describing the error.
   *
   * @return The associated {@link SzError} describing the error.
   */
  @Override
  public SzError getError() {
    return this.error;
  }

  /**
   * Sets the associated {@link SzError} describing the error.
   *
   * @param error The {@link SzError} describing the error.
   */
  @Override
  public void setError(SzError error) {
    this.error = error;
  }

  /**
   * Gets the number of times the error occurred.
   *
   * @return The number of times the error occurred.
   */
  @Override
  public int getOccurrenceCount() {
    return this.occurrenceCount;
  }

  /**
   * Sets the number of times the error occurred.
   *
   * @param count The number of times the error occurred.
   */
  @Override
  public void setOccurrenceCount(int count) {
    this.occurrenceCount = count;
  }

  /**
   * Increments the occurrence count and returns the new occurrence count.
   *
   * @return The new occurrence count.
   */
  @Override
  public int trackOccurrence() {
    return ++this.occurrenceCount;
  }

  @Override
  public String toString() {
    return "SzBulkLoadError{" +
        "error=" + error +
        ", occurrenceCount=" + occurrenceCount +
        '}';
  }
}
