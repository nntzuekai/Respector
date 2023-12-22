package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzErrorResponseImpl;
import com.senzing.g2.engine.G2Fallible;
import java.util.List;

/**
 * Extends {@link SzBasicResponse} to create a response for when errors occur.
 */
@JsonDeserialize(using=SzErrorResponse.Factory.class)
public interface SzErrorResponse extends SzBasicResponse {
  /**
   * Add an error to this instance.
   *
   * @param error The non-null {@link SzError} describing the failure.
   */
  void addError(SzError error);

  /**
   * Returns an unmodifiable view of the errors associated with this instance.
   *
   * @return The {@link List} of {@link SzError} instances for the associated
   *         errors.
   */
  List<SzError> getErrors();

  /**
   * A {@link ModelProvider} for instances of {@link SzErrorResponse}.
   */
  interface Provider extends ModelProvider<SzErrorResponse> {
    /**
     * Constructs with the specified HTTP method and self link.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     */
    SzErrorResponse create(SzMeta meta, SzLinks links);

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstError The {@link SzError} describing the first error.
     */
    SzErrorResponse create(SzMeta   meta,
                           SzLinks  links,
                           SzError  firstError);

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error message.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstError The error message for the first error.
     */
    SzErrorResponse create(SzMeta meta, SzLinks links, String firstError);

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstError The {@link Throwable} describing the first error.
     */
    SzErrorResponse create(SzMeta meta, SzLinks links, Throwable firstError);

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstErrorFallible The {@link G2Fallible} from which to extract the
     *                           error code and exception message.
     */
    SzErrorResponse create(SzMeta     meta,
                           SzLinks    links,
                           G2Fallible firstErrorFallible);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzErrorResponse} that produces instances of
   * {@link SzErrorResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzErrorResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzErrorResponse.class, SzErrorResponseImpl.class);
    }

    @Override
    public SzErrorResponse create(SzMeta meta, SzLinks links) {
      return new SzErrorResponseImpl(meta, links);
    }

    @Override
    public SzErrorResponse create(SzMeta       meta,
                                  SzLinks      links,
                                  SzError      firstError) {
      return new SzErrorResponseImpl(meta, links, firstError);
    }

    @Override
    public SzErrorResponse create(SzMeta  meta,
                                  SzLinks links,
                                  String  firstError)
    {
      return new SzErrorResponseImpl(meta, links, firstError);
    }

    @Override
    public SzErrorResponse create(SzMeta       meta,
                                  SzLinks      links,
                                  Throwable    firstError)
    {
      return new SzErrorResponseImpl(meta, links, firstError);
    }

    @Override
    public SzErrorResponse create(SzMeta     meta,
                                  SzLinks    links,
                                  G2Fallible firstErrorFallible)
    {
      return new SzErrorResponseImpl(meta, links, firstErrorFallible);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzErrorResponse}.
   */
  class Factory extends ModelFactory<SzErrorResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzErrorResponse.class);
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
     * Constructs with the specified HTTP method and self link.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     */
    public SzErrorResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstError The {@link SzError} describing the first error.
     */
    public SzErrorResponse create(SzMeta       meta,
                                  SzLinks      links,
                                  SzError      firstError)
    {
      return this.getProvider().create(meta, links, firstError);
    }

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error message.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstError The error message for the first error.
     */
    public SzErrorResponse create(SzMeta  meta,
                                  SzLinks links,
                                  String  firstError)
    {
      return this.getProvider().create(meta, links, firstError);
    }

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstError The {@link Throwable} describing the first error.
     */
    public SzErrorResponse create(SzMeta       meta,
                                  SzLinks      links,
                                  Throwable    firstError)
    {
      return this.getProvider().create(meta, links, firstError);
    }

    /**
     * Constructs with the specified HTTP method and self link and the first
     * error.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param firstErrorFallible The {@link G2Fallible} from which to extract the
     *                           error code and exception message.
     */
    public SzErrorResponse create(SzMeta     meta,
                                  SzLinks    links,
                                  G2Fallible firstErrorFallible)
    {
      return this.getProvider().create(meta, links, firstErrorFallible);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
