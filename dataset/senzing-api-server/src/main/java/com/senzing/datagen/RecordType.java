package com.senzing.datagen;

/**
 * Enumerates the various kinds of records that can be generated.
 */
public enum RecordType {
  /**
   * Record describes a person.
   */
  PERSON,

  /**
   * Record describes an organization or corporation (but not an office or
   * store location).
   */
  ORGANIZATION,

  /**
   * Record describes a business, store or office location of an organization.
   */
  BUSINESS;
}
