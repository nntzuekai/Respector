package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBulkLoadErrorImpl;

/**
 * Describes an error that occurred.
 */
@JsonDeserialize(using=SzBulkLoadError.Factory.class)
public interface SzBulkLoadError {
  /**
   * Gets the associated {@link SzError} describing the error.
   *
   * @return The associated {@link SzError} describing the error.
   */
  SzError getError();

  /**
   * Sets the associated {@link SzError} describing the error.
   *
   * @param error The {@link SzError} describing the error.
   */
  void setError(SzError error);

  /**
   * Gets the number of times the error occurred.
   *
   * @return The number of times the error occurred.
   */
  int getOccurrenceCount();

  /**
   * Sets the number of times the error occurred.
   *
   * @param count The number of times the error occurred.
   */
  void setOccurrenceCount(int count);

  /**
   * Increments the occurrence count and returns the new occurrence count.
   *
   * @return The new occurrence count.
   */
  int trackOccurrence();

  /**
   * A {@link ModelProvider} for instances of {@link SzBulkLoadError}.
   */
  interface Provider extends ModelProvider<SzBulkLoadError> {
    /**
     * Constructs an instance with no parameters.
     */
    SzBulkLoadError create();

    /**
     * Constructs an instance with the specified {@link SzError} describing the
     * error that occurred and an occurrence count of zero (0).
     *
     * @param error The {@link SzError} describing the error that occurred.
     */
    SzBulkLoadError create(SzError error);

    /**
     * Constructs with the specified {@link SzError} and the specified
     * occurrence count.
     *
     * @param error The {@link SzError} describing the error that occurred.
     *
     * @param occurrenceCount The number of times the error occurred.
     */
    SzBulkLoadError create(SzError error, int occurrenceCount);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBulkLoadError} that produces instances of {@link SzBulkLoadErrorImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzBulkLoadError>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzBulkLoadError.class, SzBulkLoadErrorImpl.class);
    }

    @Override
    public SzBulkLoadError create() {
      return new SzBulkLoadErrorImpl();
    }

    @Override
    public SzBulkLoadError create(SzError error) {
      return new SzBulkLoadErrorImpl(error);
    }

    @Override
    public SzBulkLoadError create(SzError error, int occurrenceCount) {
      return new SzBulkLoadErrorImpl(error, occurrenceCount);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzBulkLoadError}.
   */
  class Factory extends ModelFactory<SzBulkLoadError, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzBulkLoadError.class);
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
     * Constructs an instance with no parameters.
     */
    public SzBulkLoadError create() {
      return this.getProvider().create();
    }

    /**
     * Constructs an instance with the specified {@link SzError} describing the
     * error that occurred and an occurrence count of zero (0).
     *
     * @param error The {@link SzError} describing the error that occurred.
     */
    public SzBulkLoadError create(SzError error) {
      return this.getProvider().create(error);
    }

    /**
     * Constructs with the specified {@link SzError} and the specified
     * occurrence count.
     *
     * @param error The {@link SzError} describing the error that occurred.
     *
     * @param occurrenceCount The number of times the error occurred.
     */
    public SzBulkLoadError create(SzError error, int occurrenceCount) {
      return this.getProvider().create(error, occurrenceCount);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
