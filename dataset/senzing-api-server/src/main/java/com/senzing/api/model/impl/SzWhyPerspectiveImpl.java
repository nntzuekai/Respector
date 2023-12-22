package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzFocusRecordId;
import com.senzing.api.model.SzWhyPerspective;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes the perspective used in evaluating why an entity resolved or why
 * two records may or may not resolve.  The answer to "why" is dependent on
 * which "record" you are comparing against the other "records".  Internally,
 * it is not always based on "record" because multiple records that are
 * effectively identical collapse into a single perspective.
 */
@JsonDeserialize
public class SzWhyPerspectiveImpl implements SzWhyPerspective {
  /**
   * The internal ID uniquely identifying this perspective from others
   * in the complete "why" response.
   */
  private Long internalId;

  /**
   * The associated entity ID for the perspective.
   */
  private Long entityId;

  /**
   * The {@link Set} of {@link SzFocusRecordId} instances identifying the
   * effectively identical records that are being compared against the other
   * records.
   */
  private Set<SzFocusRecordId> focusRecords;

  /**
   * Default constructor.
   */
  public SzWhyPerspectiveImpl() {
    this.internalId = null;
    this.entityId = null;
    this.focusRecords = new LinkedHashSet<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getInternalId() {
    return this.internalId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInternalId(Long internalId) {
    this.internalId = internalId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getEntityId() {
    return this.entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<SzFocusRecordId> getFocusRecords() {
    return Collections.unmodifiableSet(this.focusRecords);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFocusRecord(SzFocusRecordId focusRecord) {
    this.focusRecords.add(focusRecord);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFocusRecords(Collection<SzFocusRecordId> focusRecords) {
    this.focusRecords.clear();
    if (focusRecords != null) this.focusRecords.addAll(focusRecords);
  }
}
