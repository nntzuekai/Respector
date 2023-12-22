package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzReevaluateResponseImpl;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * The response for a reevaluation operation.
 *
 */
@JsonDeserialize(using=SzReevaluateResponse.Factory.class)
public interface SzReevaluateResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzReevaluateResponseData} for this instance.
   *
   * @return The {@link SzReevaluateResponseData} for this instance.
   */
  SzReevaluateResponseData getData();

  /**
   * Sets the {@link SzReevaluateResponseData} for this instance.
   *
   * @param data The {@link SzReevaluateResponseData} for this instance.
   */
  void setData(SzReevaluateResponseData data);

  /**
   * Convenience method for setting the @link SzResolutionInfo} on the
   * underlying {@link SzReevaluateResponseData}.
   *
   * @param info The @link SzResolutionInfo} providing the information associated
   *             with the reevaluation.
   */
  void setInfo(SzResolutionInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzReevaluateResponse}.
   */
  interface Provider extends ModelProvider<SzReevaluateResponse> {
    /**
     * Creates an instance of {@link SzReevaluateResponse} initialized with
     * the specified {@link SzMeta} and {@link SzLinks}, but no data.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzReevaluateResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzReevaluateResponse} initialized with
     * the specified {@link SzMeta}, {@link SzLinks} and the specified
     * {@link SzResolutionInfo}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzReevaluateResponseData} describing the data
     *             associated with the response.
     */
    SzReevaluateResponse create(SzMeta                    meta,
                                SzLinks                   links,
                                SzReevaluateResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzReevaluateResponse} that produces instances of
   * {@link SzReevaluateResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzReevaluateResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzReevaluateResponse.class, SzReevaluateResponseImpl.class);
    }

    @Override
    public SzReevaluateResponse create(SzMeta meta, SzLinks links) {
      return new SzReevaluateResponseImpl(meta, links);
    }

    @Override
    public SzReevaluateResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzReevaluateResponseData data)
    {
      return new SzReevaluateResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzReevaluateResponse}.
   */
  class Factory extends ModelFactory<SzReevaluateResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzReevaluateResponse.class);
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
     * Creates an instance of {@link SzReevaluateResponse} initialized with
     * the specified {@link SzMeta} and {@link SzLinks}, but no data.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzReevaluateResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzReevaluateResponse} initialized with
     * the specified {@link SzMeta}, {@link SzLinks} and the specified
     * {@link SzResolutionInfo}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzReevaluateResponseData} describing the data
     *             associated with the response.
     */
    public SzReevaluateResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzReevaluateResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
