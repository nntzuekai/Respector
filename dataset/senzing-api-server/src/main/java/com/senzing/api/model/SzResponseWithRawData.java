package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzResponseWithRawDataImpl;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Extends the {@link SzBasicResponse} to add the optional raw data section
 * that is common to most responses that leverage the native Senzing APIs.
 *
 */
@JsonDeserialize(using=SzResponseWithRawData.Factory.class)
public interface SzResponseWithRawData extends SzBasicResponse {
  /**
   * Returns the raw data associated with this response.
   *
   * @return The raw data associated with this response.
   */
  @JsonInclude(NON_NULL)
  Object getRawData();

  /**
   * Sets the raw data associated with this response.
   *
   * @param rawData The raw data associated with this response.
   */
  void setRawData(Object rawData);

  /**
   * A {@link ModelProvider} for instances of {@link SzResponseWithRawData}.
   */
  interface Provider extends ModelProvider<SzResponseWithRawData> {
    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     */
    SzResponseWithRawData create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and object representing the raw data response from the engine.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param rawData The raw data to associate with the response.
     */
    SzResponseWithRawData create(SzMeta meta, SzLinks links, String rawData);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzResponseWithRawData} that produces instances of
   * {@link SzResponseWithRawDataImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzResponseWithRawData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzResponseWithRawData.class, SzResponseWithRawDataImpl.class);
    }

    @Override
    public SzResponseWithRawData create(SzMeta meta, SzLinks links) {
      return new SzResponseWithRawDataImpl(meta, links);
    }

    @Override
    public SzResponseWithRawData create(SzMeta  meta,
                                        SzLinks links,
                                        String  rawData)
    {
      return new SzResponseWithRawDataImpl(meta, links, rawData);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzResponseWithRawData}.
   */
  class Factory extends ModelFactory<SzResponseWithRawData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzResponseWithRawData.class);
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
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     */
    public SzResponseWithRawData create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and object representing the raw data response from the engine.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param rawData The raw data to associate with the response.
     */
    public SzResponseWithRawData create(SzMeta  meta,
                                        SzLinks links,
                                        String  rawData)
    {
      return this.getProvider().create(meta, links, rawData);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
