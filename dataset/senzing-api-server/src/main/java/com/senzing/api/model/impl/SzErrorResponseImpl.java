package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Fallible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a default implementation of {@link SzErrorResponseImpl}.
 */
@JsonDeserialize
public class SzErrorResponseImpl extends SzBasicResponseImpl
  implements SzErrorResponse
{
  /**
   * The list of errors.
   */
  private List<SzError> errors;

  /**
   * Package-private default constructor.
   */
  protected SzErrorResponseImpl() {
    this.errors = null;
  }

  /**
   * Constructs with the specified HTTP method and self link.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   */
  public SzErrorResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, (SzError) null);
  }

  /**
   * Constructs with the specified HTTP method and self link and the first
   * error.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param firstError The {@link SzError} describing the first error.
   */
  public SzErrorResponseImpl(SzMeta       meta,
                             SzLinks      links,
                             SzError      firstError)
  {
    super(meta, links);
    this.errors = new LinkedList<>();
    if (firstError != null) this.errors.add(firstError);
  }

  /**
   * Constructs with the specified HTTP method and self link and the first
   * error message.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param firstError The error message for the first error.
   */
  public SzErrorResponseImpl(SzMeta meta, SzLinks links, String firstError)
  {
    this(meta, links,
         firstError != null ? SzError.FACTORY.create(firstError) : null);
  }

  /**
   * Constructs with the specified HTTP method and self link and the first
   * error.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param firstError The {@link Throwable} describing the first error.
   */
  public SzErrorResponseImpl(SzMeta       meta,
                             SzLinks      links,
                             Throwable    firstError)
  {
    this(meta, links,
         firstError != null ? SzError.FACTORY.create(firstError) : null);
  }

  /**
   * Constructs with the specified HTTP method and self link and the first
   * error.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param firstErrorFallible The {@link G2Fallible} from which to extract the
   *                           error code and exception message.
   */
  public SzErrorResponseImpl(SzMeta     meta,
                             SzLinks    links,
                             G2Fallible firstErrorFallible)
  {
    this(meta,
         links,
         ((firstErrorFallible != null)
             ? SzError.FACTORY.create(firstErrorFallible) : null));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addError(SzError error) {
    if (error == null) return;
    this.errors.add(error);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzError> getErrors() {
    return Collections.unmodifiableList(this.errors);
  }

  /**
   * Private method to set the errors during JSON deserialization.
   *
   * @param errors The {@link List} of {@link SzError} instances.
   */
  private void setErrors(List<SzError> errors) {
    this.errors = (errors == null) ? null : new ArrayList<>(errors);
  }
}
