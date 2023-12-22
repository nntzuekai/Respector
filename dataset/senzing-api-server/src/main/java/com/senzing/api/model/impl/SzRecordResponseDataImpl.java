package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize
public class SzRecordResponseDataImpl implements SzRecordResponseData
{
  /**
   * The {@link SzEntityRecord} describing the record.
   */
  private SzEntityRecord entityRecord;

  /**
   * Default constructor.
   */
  public SzRecordResponseDataImpl() {
    this.entityRecord = null;
  }

  /**
   * Constructs with the specified {@link SzEntityRecord}.
   *
   * @param record The {@link SzEntityRecord} describing the record.
   */
  public SzRecordResponseDataImpl(SzEntityRecord record) {
    this.entityRecord = record;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzEntityRecord getRecord() {
      return this.entityRecord;
    }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecord(SzEntityRecord entityRecord) {
    this.entityRecord = entityRecord;
  }
}
