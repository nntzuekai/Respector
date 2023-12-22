package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzLoadRecordResponseData}.
 */
@JsonDeserialize
public class SzLoadRecordResponseDataImpl implements SzLoadRecordResponseData
{
  /**
   * The record ID of the record that was loaded.
   */
  private String recordId;

  /**
   * The {@link SzResolutionInfo} providing the information associated with
   * the resolution of the record.
   */
  private SzResolutionInfo info;

  /**
   * Default constructor.
   */
  public SzLoadRecordResponseDataImpl() {
    this(null);
  }

  /**
   * Constructs with the specified record ID.
   *
   * @param recordId The record ID of the record that was loaded.
   */
  public SzLoadRecordResponseDataImpl(String recordId) {
    this(recordId, null);
  }

  /**
   * Constructs with the specified record ID and {@link SzResolutionInfo}.
   *
   * @param recordId The record ID of the record that was loaded.
   *
   * @param info The {@link SzResolutionInfo} describing the resolution info
   *             for the operation.
   */
  public SzLoadRecordResponseDataImpl(String            recordId,
                                      SzResolutionInfo  info)
  {
    this.recordId = recordId;
    this.info     = info;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRecordId() {
    return this.recordId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzResolutionInfo getInfo() {
    return this.info;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInfo(SzResolutionInfo info) {
    this.info = info;
  }
}
