package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementatio of {@link SzVersionResponse}.
 *
 */
@JsonDeserialize
public class SzVersionResponseImpl extends SzResponseWithRawDataImpl
  implements SzVersionResponse
{
  /**
   * The data for this instance.
   */
  private SzVersionInfo versionInfo = null;

  /**
   * Default constructor.
   */
  protected SzVersionResponseImpl() {
    this.versionInfo = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * license info data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzVersionResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link SzVersionInfo}
   * describing the version.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param versionInfo The {@link SzVersionInfo} describing the version.
   */
  public SzVersionResponseImpl(SzMeta         meta,
                               SzLinks        links,
                               SzVersionInfo  versionInfo)
  {
    super(meta, links);
    this.versionInfo = versionInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzVersionInfo getData() {
    return this.versionInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzVersionInfo info) {
    this.versionInfo = info;
  }
}
