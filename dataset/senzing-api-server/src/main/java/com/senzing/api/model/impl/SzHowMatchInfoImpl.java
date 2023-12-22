package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Provides a default implementation of {@link SzWhyMatchInfo}.
 */
@JsonDeserialize
public class SzHowMatchInfoImpl implements SzHowMatchInfo {
  /**
   * The match key indicating the components of the match.
   */
  private String matchKey;

  /**
   * The resolution rule that triggered the match.
   */
  private String resolutionRule;

  /**
   * The {@link Map} of {@link String} feature type keys to {@link List}
   * values of {@link SzFeatureScore} instances.
   */
  private Map<String, List<SzFeatureScore>> featureScores;

  /**
   * The {@link Map} of {@link String} feature type keys to <b>unmodifiable</b>
   * {@link List} values of {@link SzFeatureScore} instances.
   */
  private Map<String, List<SzFeatureScore>> featureScoreViews;

  /**
   * Default constructor.
   */
  public SzHowMatchInfoImpl() {
    this.matchKey           = null;
    this.resolutionRule     = null;
    this.featureScores      = new LinkedHashMap<>();
    this.featureScoreViews  = new LinkedHashMap<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMatchKey() {
    return this.matchKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMatchKey(String matchKey) {
    this.matchKey = matchKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getResolutionRule() {
    return this.resolutionRule;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResolutionRule(String resolutionRule) {
    this.resolutionRule = resolutionRule;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, List<SzFeatureScore>> getFeatureScores() {
    return Collections.unmodifiableMap(this.featureScoreViews);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFeatureScore(SzFeatureScore featureScore) {
    String                featureType = featureScore.getFeatureType();
    List<SzFeatureScore>  list        = this.featureScores.get(featureType);

    // check if the list does not exist
    if (list == null) {
      list = new LinkedList<>();
      List<SzFeatureScore> listView = Collections.unmodifiableList(list);

      this.featureScores.put(featureType, list);
      this.featureScoreViews.put(featureType, listView);
    }

    // add to the list
    list.add(featureScore);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatureScores(Map<String, List<SzFeatureScore>> featureScores)
  {
    this.featureScores.clear();
    this.featureScoreViews.clear();
    if (featureScores == null) return;
    featureScores.entrySet().forEach(entry -> {
      String                featureType = entry.getKey();
      List<SzFeatureScore>  list        = entry.getValue();

      List<SzFeatureScore> listCopy = new LinkedList<>();
      List<SzFeatureScore> listView = Collections.unmodifiableList(listCopy);
      listCopy.addAll(list);

      this.featureScores.put(featureType, listCopy);
      this.featureScoreViews.put(featureType, listView);
    });
  }
}
