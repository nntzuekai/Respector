package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzConfigResponse;
import com.senzing.api.model.SzLinks;
import com.senzing.api.model.SzMeta;

/**
 * A response object that contains license data.
 *
 */
@JsonDeserialize
public class SzConfigResponseImpl extends SzResponseWithRawDataImpl
  implements SzConfigResponse
{
  /**
   * Default constructor.
   */
  protected SzConfigResponseImpl() {
    // do nothing
  }

  /**
   * Constructs with the specified HTTP method and self link.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzConfigResponseImpl(SzMeta meta, SzLinks links)
  {
    super(meta, links);
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
  public SzConfigResponseImpl(SzMeta meta, SzLinks links, String rawData)
  {
    super(meta, links, rawData);
  }
}
