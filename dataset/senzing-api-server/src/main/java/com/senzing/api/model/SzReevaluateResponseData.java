package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDeleteRecordResponseDataImpl;
import com.senzing.api.model.impl.SzDeleteRecordResponseImpl;
import com.senzing.api.model.impl.SzReevaluateResponseDataImpl;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * The response containing the optional resolution info of the reevaluation.
 */
@JsonDeserialize(using= SzReevaluateResponseData.Factory.class)
public interface SzReevaluateResponseData {
  /**
   * Gets the {@link SzResolutionInfo} providing the information associated
   * with the reevaluation.
   *
   * @return The {@link SzResolutionInfo} providing the information
   *         associated with the reevaluation.
   */
  @JsonInclude(NON_NULL)
  SzResolutionInfo getInfo();

  /**
   * Sets the {@link SzResolutionInfo} providing the information associated
   * with the reevaluation.
   *
   * @param info The {@link SzResolutionInfo} providing the information
   *             associated with the reevaluation.
   */
  void setInfo(SzResolutionInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzReevaluateResponseData}.
   */
  interface Provider extends ModelProvider<SzReevaluateResponseData> {
    /**
     * Creates an uninitialized instance of {@link SzReevaluateResponseData}.
     */
    SzReevaluateResponseData create();

    /**
     * Creates an instance of {@link SzReevaluateResponseData} with the
     * specified {@link SzResolutionInfo}.
     *
     * @param info The {@link SzResolutionInfo} describing the associated
     *             resolution info for the operation.
     *
     * @return The {@link SzReevaluateResponseData} instance that was created.
     */
    SzReevaluateResponseData create(SzResolutionInfo info);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzReevaluateResponseData} that produces instances of
   * {@link SzDeleteRecordResponseImpl}.
   */
  class DefaultProvider
      extends AbstractModelProvider<SzReevaluateResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzReevaluateResponseData.class,
            SzReevaluateResponseDataImpl.class);
    }

    @Override
    public SzReevaluateResponseData create() {
      return new SzReevaluateResponseDataImpl();
    }

    @Override
    public SzReevaluateResponseData create(SzResolutionInfo info)
    {
      return new SzReevaluateResponseDataImpl(info);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzReevaluateResponseData}.
   */
  class Factory extends ModelFactory<SzReevaluateResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzReevaluateResponseData.class);
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
     * Creates an uninitialized instance of {@link SzReevaluateResponseData}.
     */
    public SzReevaluateResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzReevaluateResponseData} with the
     * specified {@link SzResolutionInfo}.
     *
     * @param info The {@link SzResolutionInfo} describing the associated
     *             resolution info for the operation.
     *
     * @return The {@link SzReevaluateResponseData} instance that was created.
     */
    public SzReevaluateResponseData create(SzResolutionInfo info)
    {
      return this.getProvider().create(info);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
