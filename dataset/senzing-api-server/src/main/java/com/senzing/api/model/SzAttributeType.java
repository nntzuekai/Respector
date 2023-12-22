package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeTypeImpl;
import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a Senzing attribute type which helps describe a feature of an
 * entity and/or record in part or in whole.
 */
@JsonDeserialize(using=SzAttributeType.Factory.class)
public interface SzAttributeType {
  /**
   * Returns the unique attribute code identifying this attribute type.
   *
   * @return The unique attribute code identifying this attribute type.
   */
  String getAttributeCode();

  /**
   * Sets the unique attribute code identifying this attribute type.
   *
   * @param attributeCode The unique attribute code identifying this
   *                      attribute type.
   */
  void setAttributeCode(String attributeCode);

  /**
   * Gets the default value associated with the attribute type when an attribute
   * value is not provided.
   *
   * @return The default value associated with the attribute type when an
   *         attribute value is not provided.
   */
  String getDefaultValue();

  /**
   * Sets the default value associated with the attribute type when an attribute
   * value is not provided.
   *
   * @param defaultValue The default value associated with the attribute type
   *                     when an attribute value is not provided.
   */
  void setDefaultValue(String defaultValue);

  /**
   * Gets the {@linkplain SzAttributeNecessity necessity} for an attribute
   * of this type with the associated feature type.
   *
   * @return The {@link SzAttributeNecessity} describing the neccessity.
   */
  SzAttributeNecessity getNecessity();

  /**
   * Sets the {@linkplain SzAttributeNecessity necessity} for an attribute
   * of this type with the associated feature type.
   *
   * @param necessity The {@link SzAttributeNecessity} describing the
   *                  neccessity.
   */
  void setNecessity(SzAttributeNecessity necessity);

  /**
   * Gets the {@linkplain SzAttributeClass attribute class} associated with the
   * attribute type.
   *
   * @return The {@link SzAttributeClass} describing the attribute class
   *         associated with the attribute type.
   */
  SzAttributeClass getAttributeClass();

  /**
   * Sets the {@linkplain SzAttributeClass attribute class} associated with the
   * attribute type.
   *
   * @param attributeClass The {@link SzAttributeClass} describing the attribute
   *                       class associated with the attribute type.
   */
  void setAttributeClass(SzAttributeClass attributeClass);

  /**
   * Gets the name of feature type to which this attribute type belongs (if
   * any).  If <tt>null</tt> is returned, then the attribute type is stand-alone
   * and not part of a feature type.
   *
   * @return The name of the feature type to which this attribute type belongs,
   *         or <tt>null</tt> if this is a stand-alone attribute type.
   */
  String getFeatureType();

  /**
   * Sets the name of feature type to which this attribute type belongs (if
   * any).  If <tt>null</tt> is specified, then the attribute type is
   * stand-alone and not part of a feature type.
   *
   * @param featureType The name of the feature type to which this attribute
   *                    type belongs, or <tt>null</tt> if this is a stand-alone
   *                    attribute type.
   */
  void setFeatureType(String featureType);

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
  boolean isAdvanced();

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
  void setAdvanced(boolean advanced);

  /**
   * Checks whether or not an attribute type that is typically generated
   * internally based on other attribute types.  These are not commonly used by
   * the user except in some rare cases.  Examples include pre-hashed versions
   * of attributes that are hashed.
   *
   * @return <tt>true</tt> if this attribute type is internal, otherwise
   *         <tt>false</tt>.
   */
  boolean isInternal();

  /**
   * Sets whether or not an attribute type that is typically generated
   * internally based on other attribute types.  These are not commonly used by
   * the user except in some rare cases.  Examples include pre-hashed versions
   * of attributes that are hashed.
   *
   * @param internal <tt>true</tt> if this attribute type is internal,
   *                 otherwise <tt>false</tt>.
   */
  void setInternal(boolean internal);

  /**
   * Parses a list of entity data instances from a {@link JsonArray}
   * describing a JSON array in the Senzing native API format for entity
   * features and populates the specified {@link List} or creates a new
   * {@link List}.
   *
   * @param list The {@link List} of {@link SzAttributeType} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzAttributeType} instances.
   */
  static List<SzAttributeType> parseAttributeTypeList(
      List<SzAttributeType> list,
      JsonArray             jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseAttributeType(null, jsonObject));
    }
    return list;
  }

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeType}.
   */
  interface Provider extends ModelProvider<SzAttributeType> {
    /**
     * Creates a new instance of {@link SzAttributeType}.
     *
     * @return The new instance of {@link SzAttributeType}
     */
    SzAttributeType create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzVersionInfo} that produces instances of {@link SzAttributeTypeImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzAttributeType>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeType.class, SzAttributeTypeImpl.class);
    }

    @Override
    public SzAttributeType create() {
      return new SzAttributeTypeImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzAttributeType}.
   */
  class Factory extends ModelFactory<SzAttributeType, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeType.class);
    }

    /**
     * Constructs with the default provider.  This constructor is private and
     * is used for the master singleton instance.
     * @param defaultProvider The default provider.
     */
    private Factory(Provider defaultProvider) {
      super(defaultProvider);
    }

    /**
     * Creates a new instance of {@link SzAttributeType}.
     * @return The new instance of {@link SzAttributeType}.
     */
    public SzAttributeType create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the attribute type data from a {@link JsonObject} describing
   * JSON from the Senzing CFG_ATTR native config format for an attribute type
   * and populates the specified {@link SzAttributeType} or creates a new
   * instance.
   *
   * @param attributeType The {@link SzAttributeType} instance to populate, or
   *                      <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native CFG_ATTR config format.
   *
   * @return The populated (or created) {@link SzAttributeType}.
   */
  static SzAttributeType parseAttributeType(
      SzAttributeType   attributeType,
      JsonObject        jsonObject)
  {
    if (attributeType == null) attributeType = SzAttributeType.FACTORY.create();

    String  attrCode      = jsonObject.getString("ATTR_CODE");
    String  defaultValue  = JsonUtilities.getString(jsonObject, "DEFAULT_VALUE");
    String  felemReq      = jsonObject.getString("FELEM_REQ");
    String  rawAttrClass  = jsonObject.getString("ATTR_CLASS");
    boolean internal      = interpretBoolean(jsonObject, "INTERNAL");
    String  ftypeCode     = JsonUtilities.getString(jsonObject, "FTYPE_CODE");
    boolean advanced      = interpretBoolean(jsonObject,"ADVANCED");

    if (ftypeCode != null && ftypeCode.trim().length() == 0) {
      ftypeCode = null;
    }

    attributeType.setAttributeCode(attrCode);
    attributeType.setDefaultValue(defaultValue);
    attributeType.setNecessity(
        SzAttributeNecessity.parseAttributeNecessity(felemReq));
    attributeType.setAttributeClass(
        SzAttributeClass.parseAttributeClass(rawAttrClass));
    attributeType.setInternal(internal);
    attributeType.setFeatureType(ftypeCode);
    attributeType.setAdvanced(advanced);

    return attributeType;
  }

  /**
   * Interprets a 0/1, true/false, "Yes"/"No" or "true"/"false" value from
   * a JSON object attribute as a boolean value.
   *
   * @param jsonObject The {@link JsonObject} from which to extract the value.
   *
   * @param key The key for extracting the value from the {@link JsonObject}
   *
   * @return <tt>true</tt> or <tt>false</tt> depending on the interpreted value.
   */
  static boolean interpretBoolean(JsonObject jsonObject, String key) {
    JsonValue jsonValue = jsonObject.getValue("/" + key);
    switch (jsonValue.getValueType()) {
      case NUMBER:
      {
        int num = ((JsonNumber) jsonValue).intValue();
        return (num != 0);
      }
      case TRUE:
        return true;
      case FALSE:
        return false;
      case STRING:
      {
        String text = ((JsonString) jsonValue).getString();
        if ("YES".equalsIgnoreCase(text))   return true;
        if ("TRUE".equalsIgnoreCase(text))  return true;
        return false;
      }
      default:
        throw new IllegalArgumentException(
            "The JsonValue does not appear to be a boolean: "
            + jsonValue.toString());
    }
  }
}
