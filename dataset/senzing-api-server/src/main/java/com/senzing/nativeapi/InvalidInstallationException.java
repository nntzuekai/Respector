package com.senzing.nativeapi;

/**
 * This exception is thrown when the Senzing native installation is invalid.
 */
public class InvalidInstallationException extends RuntimeException {
  /**
   * Default constructor.
   */
  public InvalidInstallationException() {
  }

  /**
   * Constructs with the specified message.
   *
   * @param message The message for the exception.
   */
  public InvalidInstallationException(String message) {
    super(message);
  }

  /**
   * Constructs with the specified message and cause.
   *
   * @param message The message for the exception.
   * @param cause The underlying cause for the exception.
   */
  public InvalidInstallationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs with the specified cause.
   *
   * @param cause The underlying cause for the exception.
   */
  public InvalidInstallationException(Throwable cause) {
    super(cause);
  }
}
