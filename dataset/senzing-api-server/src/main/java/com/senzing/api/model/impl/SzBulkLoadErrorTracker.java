package com.senzing.api.model.impl;

import com.senzing.api.model.SzBulkLoadError;
import com.senzing.api.model.SzError;

import java.util.*;

/**
 * A utility class for tracking bulk-load errors.
 */
public class SzBulkLoadErrorTracker {
  /**
   * The maximum number of tracked errors.
   */
  private static final int MAX_TRACKED_ERRORS = 1000;

  /**
   * The number of tracked errors to trim to when tracking.
   */
  private static final int TRACKED_ERROR_TRIM_COUNT = MAX_TRACKED_ERRORS / 2;

  /**
   * The maximum number of top errors to return.
   */
  private static final int TOP_ERROR_COUNT = 20;

  /**
   * Comparator touse for comparing tracked records.
   */
  private static final TrackedErrorComparator TRACKED_ERROR_COMPARATOR
      = new TrackedErrorComparator();

  /**
   * The {@link Map} of top errors.
   */
  private Map<SzError, TrackedError> topErrorMap;

  /**
   * Default constructor.
   */
  public SzBulkLoadErrorTracker() {
    this.topErrorMap = new LinkedHashMap<>();
  }

  /**
   * Tracks the specified error.
   *
   * @param error The {@link SzError} to track.
   */
  public void trackError(SzError error) {
    SzBulkLoadError loadError = this.getBulkLoadError(error);
    loadError.trackOccurrence();
    this.trimTrackedErrors();
  }

  /**
   * Gets the {@link SzBulkLoadError} for the specified {@link SzError}.
   * for the specified data source.
   *
   * @param error The {@link SzError} for which the {@link SzBulkLoadError}
   *               is being requested.
   * @return The {@link SzBulkLoadError} for the specified {@link SzError}.
   */
  private SzBulkLoadError getBulkLoadError(SzError error) {
    Objects.requireNonNull(error, "The error cannot be null");

    // get the analysis for that data source
    TrackedError trackedError = this.topErrorMap.get(error);

    // check if it does not yet exist
    if (trackedError == null) {
      // if not, create it and store it for later
      trackedError = new TrackedError(error);
      this.topErrorMap.put(error, trackedError);
    }

    // return the load error
    return trackedError.loadError;
  }

  /**
   * Trims the map of tracked errors to keep it from getting too large.
   * If not too many tracked errors then this method does nothing.
   */
  private void trimTrackedErrors() {
    if (this.topErrorMap.size() < MAX_TRACKED_ERRORS) return;
    int count = this.topErrorMap.size();
    List<TrackedError> trackedErrors = new ArrayList<>(count);
    trackedErrors.addAll(this.topErrorMap.values());
    Collections.sort(trackedErrors, TRACKED_ERROR_COMPARATOR);

    List<TrackedError> staleList
        = trackedErrors.subList(TRACKED_ERROR_TRIM_COUNT, trackedErrors.size());

    for (TrackedError te: staleList) {
      this.topErrorMap.remove(te.loadError.getError());
    }
  }

  /**
   * Gets the unmodifiable {@link List} of {@link SzBulkLoadError} instances
   * describing the top errors.
   *
   * @return The {@link List} of {@link SzBulkLoadError} instances describing
   *         the top errors.
   */
  public List<SzBulkLoadError> getTopErrors() {
    // get the number of errors
    int count = this.topErrorMap.size();

    // if none return an empty list
    if (count == 0) return Collections.emptyList();

    // sort the tracked errors
    List<TrackedError> trackedErrors = new ArrayList<>(count);
    trackedErrors.addAll(this.topErrorMap.values());
    Collections.sort(trackedErrors, TRACKED_ERROR_COMPARATOR);

    // determine the count to use for the return
    count = count > TOP_ERROR_COUNT ? TOP_ERROR_COUNT : count;

    // populate the list of SzBulkLoadError instances
    List<SzBulkLoadError> topErrors = new ArrayList<>(count);
    for (TrackedError trackedError : trackedErrors.subList(0, count)) {
      topErrors.add(trackedError.loadError);
    }
    return Collections.unmodifiableList(topErrors);
  }

  /**
   * Sets the {@link List} of {@link SzBulkLoadError} instances describing the
   * top errors.
   *
   * @param errors The list of top errors.
   */
  public void setTopErrors(Collection<SzBulkLoadError> errors) {
    this.topErrorMap.clear();
    if (errors != null) {
      for (SzBulkLoadError loadError: errors) {
        TrackedError trackedError = new TrackedError(loadError);
        this.topErrorMap.put(loadError.getError(), trackedError);
      }
    }
  }

  /**
   * Encapsulates an error being tracked for frequency to later determine the
   * "top error" list.  This needs a construction timestamp for sorting
   * purposes.
   */
  static class TrackedError {
    private SzBulkLoadError loadError;
    private long timestamp;
    private TrackedError(SzError error) {
      this(SzBulkLoadError.FACTORY.create(error));
    }
    private TrackedError(SzBulkLoadError error) {
      this.loadError = error;
      this.timestamp = System.currentTimeMillis();
    }
  }

  /**
   * Comparator to compare two {@link TrackedError} instances to sort by
   * highest occurrence and then by construction time and then by the underlying
   * {@link SzError} error code and mesasge.
   */
  static class TrackedErrorComparator
      implements Comparator<TrackedError>
  {
    public int compare(TrackedError e1, TrackedError e2) {
      // handle nulls
      if (e1 == null && e2 == null) return 0;
      if (e1 == null && e2 != null) return 1;

      // first sort by occurrence count
      int c1 = e1.loadError.getOccurrenceCount();
      int c2 = e2.loadError.getOccurrenceCount();
      int diff = c1 - c2;
      if (diff != 0) return diff;

      // then sort by timestamp of first occurrence
      if (e1.timestamp != e2.timestamp) {
        return (e1.timestamp < e2.timestamp) ? -1 : 1;
      }

      // then by error code
      String code1 = e1.loadError.getError().getCode();
      String code2 = e2.loadError.getError().getCode();
      diff = code1.compareTo(code2);
      if (diff != 0) return diff;

      // finally by error message
      String msg1 = e1.loadError.getError().getMessage();
      String msg2 = e2.loadError.getError().getMessage();
      return msg1.compareTo(msg2);
    }
  }

}
