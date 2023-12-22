package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityIdImpl;

import java.util.Objects;

/**
 * Describes an entity ID to identify an entity.
 */
@JsonDeserialize(using=SzEntityId.Factory.class)
public interface SzEntityId extends SzEntityIdentifier {
  /**
   * Return the entity ID identifying the entity.
   *
   * @return The entity ID identifying the entity.
   */
  long getValue();

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityId}.
   */
  interface Provider extends ModelProvider<SzEntityId> {
    /**
     * Creates a new instance of {@link SzEntityId} with the specified
     * entity ID.
     *
     * @param entityId The entity ID for the new instance.
     *
     * @return The new instance of {@link SzEntityId}
     */
    SzEntityId create(long entityId);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityFeature} that produces instances of {@link SzEntityIdImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityId>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityId.class, SzEntityIdImpl.class);
    }

    @Override
    public SzEntityId create(long entityId) {
      return new SzEntityIdImpl(entityId);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzEntityId}.
   */
  class Factory extends ModelFactory<SzEntityId, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityId.class);
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
     * Creates a new instance of {@link SzEntityId}.
     * @return The new instance of {@link SzEntityId}.
     */
    public SzEntityId create(long entityId)
    {
      return this.getProvider().create(entityId);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses text as the entity ID (same format as a long integer).
   *
   * @param text The to parse.
   *
   * @return The {@link SzEntityId} that was created.
   */
  static SzEntityId valueOf(String text) {
    Long id = Long.valueOf(text);
    return SzEntityId.FACTORY.create(id);
  }
}
