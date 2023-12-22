package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzLicenseResponse}.
 */
@JsonDeserialize
public class SzLicenseResponseImpl extends SzResponseWithRawDataImpl
  implements SzLicenseResponse
{
  /**
   * The data for this instance.
   */
  private SzLicenseResponseData data = null;

  /**
   * Default constructor.
   */
  protected SzLicenseResponseImpl() {
    super();
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * license info data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzLicenseResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzLicenseResponseData.FACTORY.create());
  }

  /**
   * Constructs with the specified {@link SzMeta}, {@link SzLinks} and {@link
   * SzLicenseResponseData}.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The data for the response.
   */
  public SzLicenseResponseImpl(SzMeta                 meta,
                               SzLinks                links,
                               SzLicenseResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * Constructs with the HTTP method, self link and the {@link SzLicenseInfo}
   * describing the license.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzLicenseInfo} describing the license.
   */
  public SzLicenseResponseImpl(SzMeta meta, SzLinks links, SzLicenseInfo data)
  {
    super(meta, links);
    this.data = SzLicenseResponseData.FACTORY.create();
    this.data.setLicense(data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzLicenseResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzLicenseResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLicense(SzLicenseInfo data) {
    this.data.setLicense(data);
  }
}
