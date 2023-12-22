package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzOpenApiSpecResponseImpl;
import com.senzing.api.model.impl.SzResponseWithRawDataImpl;

import javax.json.JsonObject;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Extends the {@link SzBasicResponse} to describe a response containing an
 * Open API specification.
 */
@JsonDeserialize(using= SzOpenApiSpecResponse.Factory.class)
public interface SzOpenApiSpecResponse extends SzBasicResponse {
  /**
   * Returns the Open API Specification in JSON format.
   *
   * @return The the Open API Specification in JSON format.
   */
  @JsonInclude(NON_NULL)
  Object getData();

  /**
   * Sets the Open API Specification in JSON format.
   *
   * @param openApiSpec The the Open API Specification in JSON format.
   */
  void setData(Object openApiSpec);

  /**
   * A {@link ModelProvider} for instances of {@link SzOpenApiSpecResponse}.
   */
  interface Provider extends ModelProvider<SzOpenApiSpecResponse> {
    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     */
    SzOpenApiSpecResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and object representing the Open API Spec.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param openApiSpec The JSON Open API spec to associate with the response.
     */
    SzOpenApiSpecResponse create(SzMeta   meta,
                                 SzLinks  links,
                                 Object   openApiSpec);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzOpenApiSpecResponse} that produces instances of
   * {@link SzResponseWithRawDataImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzOpenApiSpecResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzOpenApiSpecResponse.class, SzOpenApiSpecResponseImpl.class);
    }

    @Override
    public SzOpenApiSpecResponse create(SzMeta meta, SzLinks links) {
      return new SzOpenApiSpecResponseImpl(meta, links);
    }

    @Override
    public SzOpenApiSpecResponse create(SzMeta  meta,
                                        SzLinks links,
                                        Object  openApiSpec)
    {
      return new SzOpenApiSpecResponseImpl(meta, links, openApiSpec);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzOpenApiSpecResponse}.
   */
  class Factory extends ModelFactory<SzOpenApiSpecResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzOpenApiSpecResponse.class);
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
    public SzOpenApiSpecResponse create(SzMeta meta, SzLinks links) {
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
     * @param openApiSpec The {@link Object} describing the Open API
     *                    specification.
     */
    public SzOpenApiSpecResponse create(SzMeta  meta,
                                        SzLinks links,
                                        Object  openApiSpec)
    {
      return this.getProvider().create(meta, links, openApiSpec);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
