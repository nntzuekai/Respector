package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDeleteRecordResponseDataImpl;
import com.senzing.api.model.impl.SzDeleteRecordResponseImpl;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * The response containing the optional resolution info of the record deletion.
 */
@JsonDeserialize(using= SzDeleteRecordResponseData.Factory.class)
public interface SzDeleteRecordResponseData {
  /**
   * Gets the {@link SzResolutionInfo} providing the information associated
   * with the deletion of the record.
   *
   * @return The {@link SzResolutionInfo} providing the information
   *         associated with the resolution of the record.
   */
  @JsonInclude(NON_NULL)
  SzResolutionInfo getInfo();

  /**
   * Sets the {@link SzResolutionInfo} providing the information associated
   * with the deletion of the record.
   *
   * @param info The {@link SzResolutionInfo} providing the information
   *             associated with the resolution of the record.
   */
  void setInfo(SzResolutionInfo info);

  /**
   * A {@link ModelProvider} for instances of
   * {@link SzDeleteRecordResponseData}.
   */
  interface Provider extends ModelProvider<SzDeleteRecordResponseData> {
    /**
     * Creates an uninitialized instance of {@link SzDeleteRecordResponseData}.
     */
    SzDeleteRecordResponseData create();

    /**
     * Creates an instance of {@link SzDeleteRecordResponseData} with the
     * specified {@link SzResolutionInfo}.
     *
     * @param info The {@link SzResolutionInfo} describing the associated
     *             resolution info for the operation.
     *
     * @return The {@link SzDeleteRecordResponseData} instance that was created.
     */
    SzDeleteRecordResponseData create(SzResolutionInfo info);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDeleteRecordResponseData} that produces instances of
   * {@link SzDeleteRecordResponseImpl}.
   */
  class DefaultProvider
      extends AbstractModelProvider<SzDeleteRecordResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDeleteRecordResponseData.class,
            SzDeleteRecordResponseDataImpl.class);
    }

    @Override
    public SzDeleteRecordResponseData create() {
      return new SzDeleteRecordResponseDataImpl();
    }

    @Override
    public SzDeleteRecordResponseData create(SzResolutionInfo info)
    {
      return new SzDeleteRecordResponseDataImpl(info);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzDeleteRecordResponseData}.
   */
  class Factory extends ModelFactory<SzDeleteRecordResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDeleteRecordResponseData.class);
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
     * Creates an uninitialized instance of {@link SzDeleteRecordResponseData}.
     */
    public SzDeleteRecordResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzDeleteRecordResponseData} with the
     * specified {@link SzResolutionInfo}.
     *
     * @param info The {@link SzResolutionInfo} describing the associated
     *             resolution info for the operation.
     *
     * @return The {@link SzDeleteRecordResponseData} instance that was created.
     */
    public SzDeleteRecordResponseData create(SzResolutionInfo info)
    {
      return this.getProvider().create(info);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
