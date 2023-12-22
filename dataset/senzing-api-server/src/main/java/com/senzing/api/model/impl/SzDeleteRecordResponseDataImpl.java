package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDeleteRecordResponseData;
import com.senzing.api.model.SzLoadRecordResponseData;
import com.senzing.api.model.SzResolutionInfo;

/**
 * Provides a default implementation of {@link SzDeleteRecordResponseData}.
 */
@JsonDeserialize
public class SzDeleteRecordResponseDataImpl
    implements SzDeleteRecordResponseData
{
  /**
   * The {@link SzResolutionInfo} providing the information associated with
   * the deletion of the record.
   */
  private SzResolutionInfo info;

  /**
   * Default constructor.
   */
  public SzDeleteRecordResponseDataImpl() {
    this(null);
  }

  /**
   * Constructs with the specified {@link SzResolutionInfo}.
   *
   * @param info The {@link SzResolutionInfo} describing the resolution info
   *             for the operation.
   */
  public SzDeleteRecordResponseDataImpl(SzResolutionInfo info)
  {
    this.info = info;
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
