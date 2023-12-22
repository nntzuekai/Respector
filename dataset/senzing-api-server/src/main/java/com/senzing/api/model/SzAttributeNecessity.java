package com.senzing.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates the various classes for attribute types.
 *
 */
public enum SzAttributeNecessity {
  /**
   * The attribute for the attribute type must be provided whenever the feature
   * is provided (e.g.: "PASSPORT_NUMBER" is required with the "PASSPORT"
   * feature).
   */
  REQUIRED,

  /**
   * If no attributes for {@link #REQUIRED} attribute types are provided for
   * the feature, then at least one marked <tt>SUFFICIENT</tt> must be provided
   * (e.g.: "NAME_FULL" or "NAME_ORG" for the "NAME" feature)
   */
  SUFFICIENT,

  /**
   * Attributes of <tt>PREFERRED</tt> attribute types are optional, but
   * providing them greatly enhances accuracy for scoring and matching purposes
   * (e.g.: a "PASSPORT_COUNTRY" for "PASSPORT" feature)
   */
  PREFERRED,

  /**
   * The attribute pertains to a name (like a given name or surname).
   */
  OPTIONAL;

  /**
   * Used for looking up the {@link SzAttributeNecessity} from the
   * <tt>"FELEM_REQ"</tt> value.
   */
  private static Map<String,SzAttributeNecessity> FELEM_REQ_MAP;

  static {
    try {
      Map<String,SzAttributeNecessity> map = new HashMap<>();
      map.put("YES",      REQUIRED);
      map.put("NO",       OPTIONAL);
      map.put("ANY",      SUFFICIENT);
      map.put("DESIRED",  PREFERRED);

      FELEM_REQ_MAP = Collections.unmodifiableMap(map);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Converts the raw JSON Senzing <tt>"FELEM_REQ"</tt> values to instances
   * of {@link SzAttributeNecessity}.  If the specified text is <tt>null</tt>
   * or empty string then <tt>null</tt> is returned.
   *
   * @param felemReq The <tt>"FELEM_REQ"</tt> value for the attribute type.
   *
   * @return The {@link SzAttributeNecessity} corresponding to the
   *         <tt>"FELEM_REQ"</tt> value, or <tt>null</tt> if the specified
   *         text is <tt>null</tt> or empty-string.
   *
   * @throws IllegalArgumentException If the specified text is not recognized.
   */
  public static SzAttributeNecessity parseAttributeNecessity(String felemReq) {
    if (felemReq == null) return null;
    felemReq = felemReq.trim().toUpperCase();
    SzAttributeNecessity result = FELEM_REQ_MAP.get(felemReq);
    if (result == null) {
      throw new IllegalArgumentException(
          "The specified FELEM_REQ value is not recognized: " + felemReq);
    }
    return result;
  }
}
