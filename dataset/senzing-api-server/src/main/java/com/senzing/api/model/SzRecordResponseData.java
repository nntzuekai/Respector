package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzRecordResponseDataImpl;
import com.senzing.api.model.impl.SzRecordResponseImpl;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize(using= SzRecordResponseData.Factory.class)
public interface SzRecordResponseData {
  /**
   * Gets the {@link SzEntityRecord} describing the record.
   *
   * @return The {@link SzEntityRecord} describing the record.
   */
  SzEntityRecord getRecord();

  /**
   * Sets the {@link SzEntityRecord} describing the record.
   *
   * @param record The {@link SzEntityRecord} describing the record.
   */
  void setRecord(SzEntityRecord record);

  /**
   * A {@link ModelProvider} for instances of {@link SzRecordResponseData}.
   */
  interface Provider extends ModelProvider<SzRecordResponseData> {
    /**
     * Creates an uninitialized instance of {@link SzRecordResponseData}
     * with no data.
     */
    SzRecordResponseData create();

    /**
     * Creates an instance of {@link SzRecordResponseData} with the specified
     * {@link SzEntityRecord} describing the record.
     *
     * @param record The {@link SzEntityRecord} describing the record.
     */
    SzRecordResponseData create(SzEntityRecord record);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzRecordResponseData} that produces instances of
   * {@link SzRecordResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzRecordResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzRecordResponseData.class, SzRecordResponseDataImpl.class);
    }

    @Override
    public SzRecordResponseData create() {
      return new SzRecordResponseDataImpl();
    }

    @Override
    public SzRecordResponseData create(SzEntityRecord record)
    {
      return new SzRecordResponseDataImpl(record);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzRecordResponseData}.
   */
  class Factory extends ModelFactory<SzRecordResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzRecordResponseData.class);
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
     * Creates an uninitialized instance of {@link SzRecordResponseData}
     * with no data.
     */
    public SzRecordResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzRecordResponseData} with the specified
     * {@link SzEntityRecord} describing the record.
     *
     * @param record The {@link SzEntityRecord} describing the record.
     */
    public SzRecordResponseData create(SzEntityRecord record) {
      return this.getProvider().create(record);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
