package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzLinks;
import com.senzing.api.model.SzMeta;
import com.senzing.api.model.SzOpenApiSpecResponse;
import com.senzing.api.model.SzResponseWithRawData;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;

/**
 * Provides a default implementation of {@link SzOpenApiSpecResponse}.
 */
@JsonDeserialize
public class SzOpenApiSpecResponseImpl extends SzBasicResponseImpl
  implements SzOpenApiSpecResponse
{
  /**
   * The object describing the Open API Spec..
   */
  private Object openApiSpec;

  /**
   * Default constructor.
   */
  protected SzOpenApiSpecResponseImpl() {
    this.openApiSpec = null;
  }

  /**
   * Constructs with the specified {@link SzMeta} instance and {@link SzLinks}
   * instance.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   */
  public SzOpenApiSpecResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, null);
  }

  /**
   * Constructs with the specified {@link SzMeta} instance, {@link SzLinks}
   * instance and {@link JsonObject} describing the Open API specification.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param openApiSpec The Open API Spec to associate with the response.
   */
  public SzOpenApiSpecResponseImpl(SzMeta   meta,
                                   SzLinks  links,
                                   Object   openApiSpec)
  {
    super(meta, links);
    if (openApiSpec instanceof JsonObject) {
      openApiSpec = JsonUtilities.toJsonText((JsonObject) openApiSpec);
    }
    if (openApiSpec instanceof String) {
      openApiSpec = JsonUtilities.normalizeJsonText((String) openApiSpec);
    }
    this.openApiSpec = openApiSpec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getData() {
    return this.openApiSpec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(Object openApiSpec) {
    this.openApiSpec = openApiSpec;
  }
}
