package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;
import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.util.*;

/**
 * Provides a default implementation of {@link SzDataSourceDescriptors}.
 */
@JsonDeserialize
public class SzDataSourceDescriptorsImpl implements SzDataSourceDescriptors {
  /**
   * The {@link List} of {@link SzDataSourceDescriptor} instances.
   */
  private List<SzDataSourceDescriptor> descriptors;

  /**
   * Constructs with no {@link SzDataSourceDescriptor} instances.
   */
  public SzDataSourceDescriptorsImpl() throws NullPointerException
  {
    this.descriptors = Collections.emptyList();
  }

  /**
   * Constructs with a single {@link SzDataSourceDescriptor} instance.
   *
   * @param identifier The single non-null {@link SzDataSourceDescriptor} instance.
   *
   * @throws NullPointerException If the specified parameter is null.
   */
  public SzDataSourceDescriptorsImpl(SzDataSourceDescriptor identifier)
      throws NullPointerException
  {
    Objects.requireNonNull(identifier, "Identifier cannot be null.");
    this.descriptors = Collections.singletonList(identifier);
  }

  /**
   * Constructs with the specified {@link Collection} of {@link
   * SzDataSourceDescriptor} instances.  The specified {@link Collection} will be
   * copied.
   *
   * @param descriptors The non-null {@link Collection} of {@link
   *                    SzDataSourceDescriptor} instances.
   *
   * @throws NullPointerException If the specified parameter is null.
   */
  public SzDataSourceDescriptorsImpl(
      Collection<? extends SzDataSourceDescriptor> descriptors)
    throws NullPointerException
  {
    Objects.requireNonNull(descriptors, "Identifiers cannot be null.");
    this.descriptors = Collections.unmodifiableList(
        new ArrayList<>(descriptors));
  }

  /**
   * Private constructor to use when the collection of {@link
   * SzDataSourceDescriptor} instances may not need to be copied.
   *
   * @param descriptors The {@link List} of {@link SzDataSourceDescriptor}
   *                    instances.
   *
   * @param copy <tt>true</tt> if the specified list should be copied or
   *             used directly.
   */
  private SzDataSourceDescriptorsImpl(List<SzDataSourceDescriptor>  descriptors,
                                      boolean                       copy)
  {
    if (copy) {
      if (descriptors == null || descriptors.size() == 0) {
        this.descriptors = Collections.emptyList();
      } else {
        this.descriptors = Collections.unmodifiableList(
            new ArrayList<>(descriptors));
      }
    } else {
      this.descriptors = descriptors;
    }
  }

  /**
   * Checks if all the {@link SzDataSourceDescriptor} instances contained are of the
   * same type (e.g.: either {@link SzEntityId} or {@link SzRecordId}).
   *
   * @return <tt>true</tt> if the {@link SzDataSourceDescriptor} instances are
   *         of the same type otherwise <tt>false</tt>.
   */
  public boolean isHomogeneous() {
    Class<? extends SzDataSourceDescriptor> c = null;
    for (SzDataSourceDescriptor i : this.descriptors) {
      if (c == null) {
        c = i.getClass();
        continue;
      }
      if (c != i.getClass()) return false;
    }
    return true;
  }

  /**
   * Checks if there are no entity descriptors specified for this instance.
   *
   * @return <tt>true</tt> if no entity descriptors are specified, otherwise
   *         <tt>false</tt>.
   */
  public boolean isEmpty() {
    return (this.descriptors == null || this.descriptors.size() == 0);
  }

  /**
   * Returns the number of entity descriptors.
   *
   * @return The number of entity descriptors.
   */
  public int getCount() {
    return (this.descriptors == null ? 0 : this.descriptors.size());
  }

  /**
   * Returns the unmodifiable {@link List} of {@link SzDataSourceDescriptor}
   * instances that were specified.
   *
   * @return The unmodifiable {@link List} of {@link SzDataSourceDescriptor}
   *         instances that were specified.
   */
  public List<SzDataSourceDescriptor> getDescriptors() {
    return this.descriptors;
  }

  /**
   * Overridden to convert the {@link SzDataSourceDescriptorsImpl} instance to a JSON
   * array string.
   *
   * @return The JSON array string representation of this instance.
   *
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    String prefix = "";
    for (SzDataSourceDescriptor identifier : this.getDescriptors()) {
      sb.append(prefix).append(identifier.toString());
      prefix = ",";
    }
    sb.append("]");
    return sb.toString();
  }
}
