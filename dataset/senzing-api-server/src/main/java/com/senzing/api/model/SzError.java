package com.senzing.api.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzErrorImpl;
import com.senzing.g2.engine.G2Fallible;

/**
 * Describes an error that occurred.
 */
@JsonDeserialize(using=SzError.Factory.class)
public interface SzError {
  /**
   * Gets the associated error code (if any).
   *
   * @return The associated error code, or <tt>null</tt> if none associated.
   */
  String getCode();

  /**
   * Sets the associated error code.  Set to <tt>null</tt> if none.
   *
   * @param code The associated error code or <tt>null</tt> if none.
   */
  void setCode(String code);

  /**
   * Gets the associated message for the error.
   *
   * @return The associated error message.
   */
  String getMessage();

  /**
   * Sets the associated message for the error.
   *
   * @param message The message to associate.
   */
  void setMessage(String message);

  /**
   * A {@link ModelProvider} for instances of {@link SzError}.
   */
  interface Provider extends ModelProvider<SzError> {
    /**
     * Creates an instance with no arguments.
     */
    SzError create();

    /**
     * Constructs an instance with the specified error message.
     *
     * @param message The message to associate.
     */
    SzError create(String message);

    /**
     * Constructs an instance with the specified error code and error message.
     *
     * @param code The error code to associate.
     *
     * @param message The message to associate.
     */
    SzError create(String code, String message);

    /**
     * Constructs an instances with the specified error code and error
     * message.
     *
     * @param t The {@link Throwable} that triggered the error.
     */
    SzError create(Throwable t);

    /**
     * Constructs with the last exception information from the specified
     * {@link G2Fallible}.
     *
     * @param f The {@link G2Fallible} to get the error information from.
     */
    SzError create(G2Fallible f);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzError} that produces instances of {@link SzErrorImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzError>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzError.class, SzErrorImpl.class);
    }

    @Override
    public SzError create() {
      return new SzErrorImpl();
    }

    @Override
    public SzError create(String message) {
      return new SzErrorImpl(message);
    }

    @Override
    public SzError create(String code, String message) {
      return new SzErrorImpl(code, message);
    }

    @Override
    public SzError create(Throwable t) {
      return new SzErrorImpl(t);
    }

    @Override
    public SzError create(G2Fallible f) {
      return new SzErrorImpl(f);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzError}.
   */
  class Factory extends ModelFactory<SzError, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzError.class);
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
     * Creates an instance with no arguments.
     */
    public SzError create() {
      return this.getProvider().create();
    }

    /**
     * Constructs an instance with the specified error message.
     *
     * @param message The message to associate.
     */
    public SzError create(String message) {
      return this.getProvider().create(message);
    }

    /**
     * Constructs an instance with the specified error code and error message.
     *
     * @param code The error code to associate.
     *
     * @param message The message to associate.
     */
    public SzError create(String code, String message) {
      return this.getProvider().create(code, message);
    }

    /**
     * Constructs an instances with the specified error code and error
     * message.
     *
     * @param t The {@link Throwable} that triggered the error.
     */
    public SzError create(Throwable t) {
      return this.getProvider().create(t);
    }

    /**
     * Constructs with the last exception information from the specified
     * {@link G2Fallible}.
     *
     * @param f The {@link G2Fallible} to get the error information from.
     */
    public SzError create(G2Fallible f) {
      return this.getProvider().create(f);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
