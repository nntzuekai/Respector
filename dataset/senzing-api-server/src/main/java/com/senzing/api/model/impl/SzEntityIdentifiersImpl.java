package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityId;
import com.senzing.api.model.SzEntityIdentifier;
import com.senzing.api.model.SzEntityIdentifiers;
import com.senzing.api.model.SzRecordId;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;

/**
 * Used to represent a {@link List} of zero or more {@link SzEntityIdentifier}
 * instances.
 *
 */
@JsonDeserialize
public class SzEntityIdentifiersImpl implements SzEntityIdentifiers {
  /**
   * The {@link List} of {@link SzEntityIdentifier} instances.
   */
  private List<SzEntityIdentifier> identifiers;

  /**
   * Constructs with no {@link SzEntityIdentifier} instances.
   */
  public SzEntityIdentifiersImpl() throws NullPointerException
  {
    this.identifiers = Collections.emptyList();
  }

  /**
   * Constructs with a single {@link SzEntityIdentifier} instance.
   *
   * @param identifier The single non-null {@link SzEntityIdentifier} instance.
   *
   * @throws NullPointerException If the specified parameter is null.
   */
  public SzEntityIdentifiersImpl(SzEntityIdentifier identifier)
      throws NullPointerException
  {
    Objects.requireNonNull(identifier, "Identifier cannot be null.");
    this.identifiers = Collections.singletonList(identifier);
  }

  /**
   * Constructs with the specified {@link Collection} of {@link
   * SzEntityIdentifier} instances.  The specified {@link Collection} will be
   * copied.
   *
   * @param identifiers The non-null {@link Collection} of {@link
   *                    SzEntityIdentifier} instances.
   *
   * @throws NullPointerException If the specified parameter is null.
   */
  public SzEntityIdentifiersImpl(
      Collection<? extends SzEntityIdentifier> identifiers)
    throws NullPointerException
  {
    Objects.requireNonNull(identifiers, "Identifiers cannot be null.");
    this.identifiers = Collections.unmodifiableList(
        new ArrayList<>(identifiers));
  }

  /**
   * Returns the unmodifiable {@link List} of {@link SzEntityIdentifier}
   * instances that were specified.
   *
   * @return The unmodifiable {@link List} of {@link SzEntityIdentifier}
   *         instances that were specified.
   */
  public List<SzEntityIdentifier> getIdentifiers() {
    return this.identifiers;
  }

  /**
   * Overridden to convert the {@link SzEntityIdentifiersImpl} instance to a JSON
   * array string.
   *
   * @return The JSON array string representation of this instance.
   *
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    String prefix = "";
    for (SzEntityIdentifier identifier : this.getIdentifiers()) {
      sb.append(prefix).append(identifier.toString());
      prefix = ",";
    }
    sb.append("]");
    return sb.toString();
  }
}
