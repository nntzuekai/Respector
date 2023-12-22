package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzBasicResponse;
import com.senzing.api.model.SzLinks;
import com.senzing.api.model.SzMeta;

import java.util.Objects;

/**
 * Provides a default implementation of {@link SzBasicResponse}.
 */
@JsonDeserialize
public class SzBasicResponseImpl implements SzBasicResponse {
  /**
   * The meta section for this response.
   */
  private SzMeta meta;

  /**
   * The links associated with this response.
   */
  private SzLinks links;

  /**
   * Default constructor.
   */
  protected SzBasicResponseImpl() {
    this.meta = null;
    this.links = null;
  }

  /**
   * Constructs with the specified HTTP method and self link.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzBasicResponseImpl(SzMeta meta, SzLinks links) {
    Objects.requireNonNull(meta, "The meta data cannot be null");
    Objects.requireNonNull(links, "The links cannot be null");
    this.meta   = meta;
    this.links  = links;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzMeta getMeta() {
    return meta;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzLinks getLinks() {
    return links;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void concludeTimers() {
    this.getMeta().concludeTimers();
  }
}
