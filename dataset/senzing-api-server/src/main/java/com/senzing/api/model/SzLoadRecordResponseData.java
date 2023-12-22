package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzLoadRecordResponseDataImpl;
import com.senzing.api.model.impl.SzLoadRecordResponseImpl;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * The response containing the record ID and optional resolution info of the
 * record that was loaded.
 */
@JsonDeserialize(using= SzLoadRecordResponseData.Factory.class)
public interface SzLoadRecordResponseData {
  /**
   * Gets the record ID of the record that was loaded.
   *
   * @return The record ID of the record that was loaded.
   */
  @JsonInclude(NON_NULL)
  String getRecordId();

  /**
   * Sets the record ID of the record that was loaded.
   *
   * @param recordId The record ID of the record that was loaded.
   */
  void setRecordId(String recordId);

  /**
   * Gets the {@link SzResolutionInfo} providing the information associated
   * with the resolution of the record.
   *
   * @return The {@link SzResolutionInfo} providing the information
   *         associated with the resolution of the record.
   */
  @JsonInclude(NON_NULL)
  SzResolutionInfo getInfo();

  /**
   * Sets the {@link SzResolutionInfo} providing the information associated
   * with the resolution of the record.
   *
   * @param info The {@link SzResolutionInfo} providing the information
   *             associated with the resolution of the record.
   */
  void setInfo(SzResolutionInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzLoadRecordResponseData}.
   */
  interface Provider extends ModelProvider<SzLoadRecordResponseData> {
    /**
     * Creates an uninitialized instance of {@link SzLoadRecordResponseData}.
     */
    SzLoadRecordResponseData create();

    /**
     * Creates an instance of {@link SzLoadRecordResponseData} with the
     * specified record ID and no resolution info.
     *
     * @param recordId The record ID of the record that was loaded.
     *
     * @return The {@link SzLoadRecordResponseData} instance that was created.
     */
    SzLoadRecordResponseData create(String recordId);

    /**
     * Creates an instance of {@link SzLoadRecordResponseData} with the
     * specified record ID and {@link SzResolutionInfo}.
     *
     * @param recordId The record ID of the record that was loaded.
     *
     * @param info The {@link SzResolutionInfo} describing the associated
     *             resolution info for the operation.
     *
     * @return The {@link SzLoadRecordResponseData} instance that was created.
     */
    SzLoadRecordResponseData create(String recordId, SzResolutionInfo info);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzLoadRecordResponseData} that produces instances of
   * {@link SzLoadRecordResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzLoadRecordResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzLoadRecordResponseData.class, SzLoadRecordResponseDataImpl.class);
    }

    @Override
    public SzLoadRecordResponseData create() {
      return new SzLoadRecordResponseDataImpl();
    }

    @Override
    public SzLoadRecordResponseData create(String recordId) {
      return new SzLoadRecordResponseDataImpl(recordId);
    }

    @Override
    public SzLoadRecordResponseData create(String           recordId,
                                           SzResolutionInfo info)
    {
      return new SzLoadRecordResponseDataImpl(recordId, info);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzLoadRecordResponseData}.
   */
  class Factory extends ModelFactory<SzLoadRecordResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzLoadRecordResponseData.class);
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
     * Creates an uninitialized instance of {@link SzLoadRecordResponseData}.
     */
    public SzLoadRecordResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzLoadRecordResponseData} with the
     * specified record ID and no resolution info.
     *
     * @param recordId The record ID of the record that was loaded.
     *
     * @return The {@link SzLoadRecordResponseData} instance that was created.
     */
    public SzLoadRecordResponseData create(String recordId) {
      return this.getProvider().create(recordId);
    }

    /**
     * Creates an instance of {@link SzLoadRecordResponseData} with the
     * specified record ID and {@link SzResolutionInfo}.
     *
     * @param recordId The record ID of the record that was loaded.
     *
     * @param info The {@link SzResolutionInfo} describing the associated
     *             resolution info for the operation.
     *
     * @return The {@link SzLoadRecordResponseData} instance that was created.
     */
    public SzLoadRecordResponseData create(String           recordId,
                                           SzResolutionInfo info)
    {
      return this.getProvider().create(recordId, info);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
