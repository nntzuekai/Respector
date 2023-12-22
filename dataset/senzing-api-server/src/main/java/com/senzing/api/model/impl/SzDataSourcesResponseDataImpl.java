package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Provides a default implementation of {@Link SzDataSourcesResponseData}.
 */
@JsonDeserialize
public class SzDataSourcesResponseDataImpl implements SzDataSourcesResponseData {
  /**
   * The map of {@link String} data source codes to {@link SzDataSource}
   * instances.
   */
  private Map<String, SzDataSource> dataSources;

  /**
   * Default constructor.
   */
  public SzDataSourcesResponseDataImpl() {
    this.dataSources = new LinkedHashMap<>();
  }

  /**
   * Constructs with the specified {@link Collection} of {@link SzDataSource}
   * instances describing the data sources for this instance.
   *
   * @param dataSources The {@link Collection} of {@link SzDataSource} instances
   *                    describing the data sources for this instance.
   */
  public SzDataSourcesResponseDataImpl(
      Collection<? extends SzDataSource> dataSources)
  {
    this.dataSources = new LinkedHashMap<>();
    if (dataSources != null) {
      for (SzDataSource dataSource: dataSources) {
        this.dataSources.put(dataSource.getDataSourceCode(), dataSource);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getDataSources() {
    Set<String> set = this.dataSources.keySet();
    return Collections.unmodifiableSet(set);
  }

  /**
   * Private setter for JSON deserialization.
   */
  @JsonProperty("dataSources")
  private void setDataSources(Set<String> dataSources) {
    Iterator<Map.Entry<String,SzDataSource>> iter
        = this.dataSources.entrySet().iterator();

    // remove entries in the map that are not in the specified set
    while (iter.hasNext()) {
      Map.Entry<String,SzDataSource> entry = iter.next();
      if (!dataSources.contains(entry.getKey())) {
        iter.remove();
      }
    }

    // add place-holder entries to the map for data sources in the set
    for (String dataSource: dataSources) {
      this.dataSources.put(dataSource, null);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, SzDataSource> getDataSourceDetails() {
    return Collections.unmodifiableMap(this.dataSources);
  }

  /**
   * Private setter for JSON deserialization.
   */
  @JsonProperty("dataSourceDetails")
  private void setDataSourceDetails(Map<String, SzDataSource> details)
  {
    this.setDataSources(details == null ? null : details.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addDataSource(SzDataSource dataSource) {
    Objects.requireNonNull(
        dataSource, "The specified data source cannot be null");

    this.dataSources.put(dataSource.getDataSourceCode(), dataSource);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataSources(Collection<? extends SzDataSource> dataSources) {
    this.dataSources.clear();
    if (dataSources != null) {
      for (SzDataSource dataSource : dataSources) {
        this.dataSources.put(dataSource.getDataSourceCode(), dataSource);
      }
    }
  }

}
