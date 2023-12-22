package com.senzing.api.model;

/**
 * An interface for providing instance of a model class.  This is the mechanism
 * for swapping out the actual runtime class with another.
 *
 * @param <T>
 */
public interface ModelProvider<T> {
  /**
   * Gets the interface class provided.
   *
   * @return The interface class provided.
   */
  Class<T> getInterfaceClass();

  /**
   * Gets the runtime class instantiated that implements the interface class.
   *
   * @return The runtime class instantiated that implements the runtime class.
   */
  Class<? extends T> getRuntimeClass();
}
