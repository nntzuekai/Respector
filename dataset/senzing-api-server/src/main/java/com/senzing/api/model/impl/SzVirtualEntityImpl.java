package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Describes a virtual entity.
 */
@JsonDeserialize
public class SzVirtualEntityImpl implements SzVirtualEntity {
  /**
   * The virtual entity ID for the virtual entity.
   */
  private String virtualEntityId;

  /**
   * Used for checking if this virtual entity is considered a singleton.  This
   * is the internal ID associated with all the member records if it is a
   * singleton, and it is <code>null</code> if not a singleton.
   */
  private Long singletonId = null;

  /**
   * The {@link Set} of {@link SzVirtualEntityRecord} instances describing the
   * member records for this virtual entity.
   */
  private Set<SzVirtualEntityRecord> records;

  /**
   * Default constructor.
   */
  public SzVirtualEntityImpl() {
    this(null);
  }

  /**
   * Constructs with the specified virtual entity ID.
   *
   * @param virtualEntityId The virtual entity ID uniquely identifying the
   *                        virtual entity.
   */
  public SzVirtualEntityImpl(String virtualEntityId) {
    this.virtualEntityId  = virtualEntityId;
    this.records          = new LinkedHashSet<>();
  }

  @Override
  public String getVirtualEntityId() {
    return this.virtualEntityId;
  }

  @Override
  public void setVirtualEntityId(String virtualEntityId) {
    this.virtualEntityId = virtualEntityId;
  }

  @Override
  public boolean isSingleton() {
    if (this.records.size() == 0) return false;
    return (this.singletonId != null);
  }

  /**
   * Place-holder method for setting the singleton property since the
   * @param value
   */
  private void setSingleton(boolean value) {
    // do nothing
  }

  @Override
  public Set<SzVirtualEntityRecord> getRecords() {
    return Collections.unmodifiableSet(this.records);
  }

  @Override
  public void addRecord(SzVirtualEntityRecord record) {
    Long internalId = record.getInternalId();
    if (internalId == null) {
      throw new IllegalArgumentException(
          "Cannot add a record without an internal ID: " + record);
    }

    if (this.records.size() == 0) {
      this.records.add(record);
      this.singletonId = internalId;
    } else {
      if (!internalId.equals(this.singletonId)) {
        this.singletonId = null;
      }
    }
  }

  @Override
  public void setRecords(Collection<SzVirtualEntityRecord> records) {
    Long singletonId = null;
    if (records == null || records.size() == 0) {
      singletonId = null;
    } else {
      singletonId = records.iterator().next().getInternalId();
      for (SzVirtualEntityRecord record : records) {
        Long internalId = record.getInternalId();
        if (internalId == null) {
          throw new IllegalArgumentException(
              "None of the contained records can have a null internal ID: "
              + record);
        }
        // check if the internal ID differs
        if (!singletonId.equals(internalId)) {
          singletonId = null;
          break;
        }
      }
    }
    this.singletonId = singletonId;
    this.records.clear();
    if (records != null) {
      this.records.addAll(records);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzVirtualEntityImpl that = (SzVirtualEntityImpl) o;
    return Objects.equals(getVirtualEntityId(), that.getVirtualEntityId())
        && Objects.equals(this.singletonId, that.singletonId)
        && Objects.equals(getRecords(), that.getRecords());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getVirtualEntityId(),
                        this.singletonId,
                        this.getRecords());
  }

  @Override
  public String toString() {
    return "SzVirtualEntityImpl{" +
        "virtualEntityId=[ " + virtualEntityId
        + " ], singletonId=[ " + singletonId
        + " ], records=" + records
        + " ]}";
  }
}
