package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeTypeResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;

/**
 * A response object that contains attribute type data.
 *
 */
@JsonDeserialize(using=SzAttributeTypeResponse.Factory.class)
public interface SzAttributeTypeResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzAttributeTypeResponseData} associated with this
   * response which contains an {@link SzAttributeType}.
   *
   * @return The data associated with this response.
   */
  SzAttributeTypeResponseData getData();

  /**
   * Sets the {@link SzAttributeTypeResponseData} associated with this
   * response which contains an {@link SzAttributeType}.
   *
   * @param data The {@link SzAttributeTypeResponseData} for this response.
   */
  void setData(SzAttributeTypeResponseData data);

  /**
   * Convenience method to set the attribute type on the contained {@link
   * SzAttributeTypeResponseData}.
   *
   * @param attrType The {@link SzAttributeType} describing the attribute type.
   */
  void setAttributeType(SzAttributeType attrType);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeTypeResponse}.
   */
  interface Provider extends ModelProvider<SzAttributeTypeResponse> {
    /**
     * Creates an instance of {@link SzAttributeTypeResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzAttributeTypeResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzAttributeTypeResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzAttributeTypeResponseData}
     * that has the attribute type.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzAttributeTypeResponseData} for the response.
     */
    SzAttributeTypeResponse create(SzMeta                       meta,
                                   SzLinks                      links,
                                   SzAttributeTypeResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeTypeResponse} that produces instances of
   * {@link SzAttributeTypeResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzAttributeTypeResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeTypeResponse.class,
            SzAttributeTypeResponseImpl.class);
    }

    @Override
    public SzAttributeTypeResponse create(SzMeta meta, SzLinks links) {
      return new SzAttributeTypeResponseImpl(meta, links);
    }

    @Override
    public SzAttributeTypeResponse create(SzMeta                      meta,
                                          SzLinks                     links,
                                          SzAttributeTypeResponseData data)
    {
      return new SzAttributeTypeResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeTypeResponse}.
   */
  class Factory extends ModelFactory<SzAttributeTypeResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeTypeResponse.class);
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
     * Creates an instance of {@link SzAttributeTypeResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzAttributeTypeResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzAttributeTypeResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzAttributeTypeResponseData}
     * that has the attribute type.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzAttributeTypeResponseData} for the response.
     */
    public SzAttributeTypeResponse create(SzMeta                      meta,
                                          SzLinks                     links,
                                          SzAttributeTypeResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
