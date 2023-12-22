package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzServerInfoResponse}.
 */
@JsonDeserialize
public class SzServerInfoResponseImpl extends SzBasicResponseImpl
  implements SzServerInfoResponse
{
  /**
   * The data for this instance.
   */
  private SzServerInfo serverInfo = null;

  /**
   * Default constructor.
   */
  protected SzServerInfoResponseImpl() {
    this.serverInfo = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * license info data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzServerInfoResponseImpl(SzMeta meta, SzLinks links)
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
   * @param serverInfo The {@link SzServerInfo} describing the version.
   */
  public SzServerInfoResponseImpl(SzMeta        meta,
                                  SzLinks       links,
                                  SzServerInfo  serverInfo)
  {
    super(meta, links);
    this.serverInfo = serverInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzServerInfo getData() {
    return this.serverInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzServerInfo info) {
    this.serverInfo = info;
  }
}
