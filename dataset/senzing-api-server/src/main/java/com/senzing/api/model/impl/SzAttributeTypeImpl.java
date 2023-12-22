package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzAttributeClass;
import com.senzing.api.model.SzAttributeNecessity;
import com.senzing.api.model.SzAttributeType;

/**
 * Provides the default implementation of {@link SzAttributeType}.
 */
@JsonDeserialize
public class SzAttributeTypeImpl implements SzAttributeType {
  /**
   * The unique attribute code identifying this attribute type.
   */
  private String attributeCode;

  /**
   * The default value associated with the attribute type when an attribute
   * value is not provided.
   */
  private String defaultValue;

  /**
   * Describes the {@linkplain SzAttributeNecessity necessity} for an attribute
   * of this type with the associated feature type.
   */
  private SzAttributeNecessity necessity;

  /**
   * The {@linkplain SzAttributeClass attribute class} associated with the
   * attribute.
   */
  private SzAttributeClass attributeClass;

  /**
   * The feature type to which this attribute type belongs (if any).  If this
   * is <tt>null</tt> then it is a stand-alone attribute.
   */
  private String featureType;

  /**
   * Whether or not the attribute type is considered to be "advanced". Advanced
   * attribute types usually require the user to have some knowledge of how the
   * data is mapped in the entity repository (e.g.: "RECORD_ID" or
   * "DATA_SOURCE"). An application may exclude displaying these as options if
   * these things are being auto-generated or automatically selected for the
   * user.
   */
  private boolean advanced;

  /**
   * Whether or not an attribute type that is typically generated internally
   * based on other attribute types.  These are not commonly used by the user
   * except in some rare cases.  Examples include pre-hashed versions of
   * attributes that are hashed.
   */
  private boolean internal;

  /**
   * Default constructor.
   */
  public SzAttributeTypeImpl() {
    this.attributeCode    = null;
    this.defaultValue     = null;
    this.necessity        = null;
    this.attributeClass   = null;
    this.featureType      = null;
    this.advanced         = false;
    this.internal         = false;
  }

  /**
   * Returns the unique attribute code identifying this attribute type.
   *
   * @return The unique attribute code identifying this attribute type.
   */
  @Override
  public String getAttributeCode() {
    return attributeCode;
  }

  /**
   * Sets the unique attribute code identifying this attribute type.
   *
   * @param attributeCode The unique attribute code identifying this
   *                      attribute type.
   */
  @Override
  public void setAttributeCode(String attributeCode) {
    this.attributeCode = attributeCode;
  }

  /**
   * Gets the default value associated with the attribute type when an attribute
   * value is not provided.
   *
   * @return The default value associated with the attribute type when an
   *         attribute value is not provided.
   */
  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Sets the default value associated with the attribute type when an attribute
   * value is not provided.
   *
   * @param defaultValue The default value associated with the attribute type
   *                     when an attribute value is not provided.
   */
  @Override
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * Gets the {@linkplain SzAttributeNecessity necessity} for an attribute
   * of this type with the associated feature type.
   *
   * @return The {@link SzAttributeNecessity} describing the neccessity.
   */
  @Override
  public SzAttributeNecessity getNecessity() {
    return necessity;
  }

  /**
   * Sets the {@linkplain SzAttributeNecessity necessity} for an attribute
   * of this type with the associated feature type.
   *
   * @param necessity The {@link SzAttributeNecessity} describing the
   *                  neccessity.
   */
  @Override
  public void setNecessity(SzAttributeNecessity necessity) {
    this.necessity = necessity;
  }

  /**
   * Gets the {@linkplain SzAttributeClass attribute class} associated with the
   * attribute type.
   *
   * @return The {@link SzAttributeClass} describing the attribute class
   *         associated with the attribute type.
   */
  @Override
  public SzAttributeClass getAttributeClass() {
    return attributeClass;
  }

  /**
   * Sets the {@linkplain SzAttributeClass attribute class} associated with the
   * attribute type.
   *
   * @param attributeClass The {@link SzAttributeClass} describing the attribute
   *                       class associated with the attribute type.
   */
  @Override
  public void setAttributeClass(SzAttributeClass attributeClass) {
    this.attributeClass = attributeClass;
  }

  /**
   * Gets the name of feature type to which this attribute type belongs (if
   * any).  If <tt>null</tt> is returned, then the attribute type is stand-alone
   * and not part of a feature type.
   *
   * @return The name of the feature type to which this attribute type belongs,
   *         or <tt>null</tt> if this is a stand-alone attribute type.
   */
  @Override
  public String getFeatureType() {
    return featureType;
  }

  /**
   * Sets the name of feature type to which this attribute type belongs (if
   * any).  If <tt>null</tt> is specified, then the attribute type is
   * stand-alone and not part of a feature type.
   *
   * @param featureType The name of the feature type to which this attribute
   *                    type belongs, or <tt>null</tt> if this is a stand-alone
   *                    attribute type.
   */
  @Override
  public void setFeatureType(String featureType) {
    this.featureType = featureType;
  }

  /**
   * Checks whether or not the attribute type is considered to be "advanced".
   * Advanced attribute types usually require the user to have some knowledge
   * of how the data is mapped in the entity repository (e.g.: "RECORD_ID" or
   * "DATA_SOURCE"). An application may exclude displaying these as options if
   * these things are being auto-generated or automatically selected for the
   * user.
   *
   * @return <tt>true</tt> if this attribute type is advanced, otherwise
   *         <tt>false</tt>
   */
  @Override
  public boolean isAdvanced() {
    return advanced;
  }

  /**
   * Sets whether or not the attribute type is considered to be "advanced".
   * Advanced attribute types usually require the user to have some knowledge
   * of how the data is mapped in the entity repository (e.g.: "RECORD_ID" or
   * "DATA_SOURCE"). An application may exclude displaying these as options if
   * these things are being auto-generated or automatically selected for the
   * user.
   *
   * @param advanced <tt>true</tt> if this attribute type is advanced,
   *                 otherwise <tt>false</tt>.
   */
  @Override
  public void setAdvanced(boolean advanced) {
    this.advanced = advanced;
  }

  /**
   * Checks whether or not an attribute type that is typically generated
   * internally based on other attribute types.  These are not commonly used by
   * the user except in some rare cases.  Examples include pre-hashed versions
   * of attributes that are hashed.
   *
   * @return <tt>true</tt> if this attribute type is internal, otherwise
   *         <tt>false</tt>.
   */
  @Override
  public boolean isInternal() {
    return internal;
  }

  /**
   * Sets whether or not an attribute type that is typically generated
   * internally based on other attribute types.  These are not commonly used by
   * the user except in some rare cases.  Examples include pre-hashed versions
   * of attributes that are hashed.
   *
   * @param internal <tt>true</tt> if this attribute type is internal,
   *                 otherwise <tt>false</tt>.
   */
  @Override
  public void setInternal(boolean internal) {
    this.internal = internal;
  }

  @Override
  public String toString() {
    return "SzAttributeType{" +
        "attributeCode='" + attributeCode + '\'' +
        ", defaultValue='" + defaultValue + '\'' +
        ", necessity=" + necessity +
        ", attributeClass=" + attributeClass +
        ", featureType='" + featureType + '\'' +
        ", advanced=" + advanced +
        ", internal=" + internal +
        '}';
  }
}
