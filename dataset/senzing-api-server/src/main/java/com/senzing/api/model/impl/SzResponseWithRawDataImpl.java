package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzBasicResponse;
import com.senzing.api.model.SzLinks;
import com.senzing.api.model.SzMeta;
import com.senzing.api.model.SzResponseWithRawData;
import com.senzing.util.JsonUtilities;

/**
 * Provides a default implementation of {@link SzResponseWithRawData}.
 */
@JsonDeserialize
public class SzResponseWithRawDataImpl extends SzBasicResponseImpl
  implements SzResponseWithRawData
{
  /**
   * The raw data associated with the response.
   */
  private Object rawData;

  /**
   * Default constructor.
   */
  protected SzResponseWithRawDataImpl() {
    this.rawData = null;
  }

  /**
   * Constructs with the specified HTTP method and self link.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   */
  public SzResponseWithRawDataImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, null);
  }

  /**
   * Constructs with the specified HTTP method, self link string and
   * object representing the raw data response from the engine.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param rawData The raw data to associate with the response.
   */
  public SzResponseWithRawDataImpl(SzMeta meta, SzLinks links, String rawData)
  {
    super(meta, links);
    this.rawData = JsonUtilities.normalizeJsonText(rawData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getRawData() {
    return this.rawData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRawData(Object rawData) {
    if (rawData instanceof String) {
      this.rawData = JsonUtilities.normalizeJsonText((String) rawData);
    } else {
      this.rawData = rawData;
    }
  }
}
