package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeTypesResponseImpl;

import java.util.*;

/**
 * The response containing a list of attribute types.  Typically this is the
 * list of all configured attribute types (excluding the "internal" ones).
 *
 */
@JsonDeserialize(using=SzAttributeTypesResponse.Factory.class)
public interface SzAttributeTypesResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzAttributeTypesResponseData} for this instance.
   *
   * @return The {@link SzAttributeTypesResponseData} for this instance.
   */
  SzAttributeTypesResponseData getData();

  /**
   * Sets the {@link SzAttributeTypesResponseData} for this instance.
   *
   * @param data The {@link SzAttributeTypesResponseData} for this instance.
   */
  void setData(SzAttributeTypesResponseData data);

  /**
   * Convenience method to add the {@link SzAttributeType} to the contained
   * {@link SzAttributeTypesResponseData}.
   *
   * @param attributeType The {@link SzAttributeType} to add.
   */
  void addAttributeType(SzAttributeType attributeType);

  /**
   * Sets the specified {@link List} of {@linkplain SzAttributeType attribute
   * types} using the specified {@link Collection} of {@link SzAttributeType}
   * instances.
   *
   * @param attributeTypes The {@link Collection} of {@link SzAttributeType}
   *                       instances.
   */
  void setAttributeTypes(Collection<? extends SzAttributeType> attributeTypes);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeTypesResponse}.
   */
  interface Provider extends ModelProvider<SzAttributeTypesResponse> {
    /**
     * Creates an instance of {@link SzAttributeTypesResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzAttributeTypesResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzAttributeTypesResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link
     * SzAttributeTypesResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    SzAttributeTypesResponse create(SzMeta                        meta,
                                    SzLinks                       links,
                                    SzAttributeTypesResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeTypesResponse} that produces instances of
   * {@link SzAttributeTypesResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzAttributeTypesResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeTypesResponse.class,
            SzAttributeTypesResponseImpl.class);
    }

    @Override
    public SzAttributeTypesResponse create(SzMeta meta, SzLinks links) {
      return new SzAttributeTypesResponseImpl(meta, links);
    }

    @Override
    public SzAttributeTypesResponse create(SzMeta                       meta,
                                           SzLinks                      links,
                                           SzAttributeTypesResponseData data) {
      return new SzAttributeTypesResponseImpl(meta, links, data);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeTypesResponse}.
   */
  class Factory extends ModelFactory<SzAttributeTypesResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeTypesResponse.class);
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
     * Creates an instance of {@link SzAttributeTypesResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzAttributeTypesResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzAttributeTypesResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link
     * SzAttributeTypesResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    public SzAttributeTypesResponse create(SzMeta                       meta,
                                           SzLinks                      links,
                                           SzAttributeTypesResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
