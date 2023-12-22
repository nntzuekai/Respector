package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Provides a default implementation of {@link SzWhyMatchInfo}.
 */
@JsonDeserialize
public class SzWhyMatchInfoImpl implements SzWhyMatchInfo {
  /**
   * The why key indicating the components of the match (similar to the
   * match key).
   */
  private String whyKey;

  /**
   * The match level describing how the two records resolve against each other.
   */
  private SzMatchLevel matchLevel;

  /**
   * The resolution rule that triggered the match.
   */
  private String resolutionRule;

  /**
   * The {@link Map} of {@link String} feature type keys to {@link List}
   * values of {@link SzCandidateKey} instances.
   */
  private Map<String, List<SzCandidateKey>> candidateKeys;

  /**
   * The {@link Map} of {@link String} feature type keys to <b>unmodifiable</b>
   * {@link List} values of {@link SzCandidateKey} instances.
   */
  private Map<String, List<SzCandidateKey>> candidateKeyViews;

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
   * The {@link List} of {@link SzDisclosedRelation} instances.
   */
  private List<SzDisclosedRelation> disclosedRelations;

  /**
   * Default constructor.
   */
  public SzWhyMatchInfoImpl() {
    this.whyKey             = null;
    this.matchLevel         = null;
    this.resolutionRule     = null;
    this.candidateKeys      = new LinkedHashMap<>();
    this.candidateKeyViews  = new LinkedHashMap<>();
    this.featureScores      = new LinkedHashMap<>();
    this.featureScoreViews  = new LinkedHashMap<>();
    this.disclosedRelations = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getWhyKey() {
    return this.whyKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyKey(String whyKey) {
    this.whyKey = whyKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzMatchLevel getMatchLevel() {
    return this.matchLevel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMatchLevel(SzMatchLevel matchLevel) {
    this.matchLevel = matchLevel;
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
  public Map<String, List<SzCandidateKey>> getCandidateKeys() {
    return Collections.unmodifiableMap(this.candidateKeyViews);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addCandidateKey(SzCandidateKey candidateKey) {
    String                featureType = candidateKey.getFeatureType();
    List<SzCandidateKey>  list        = this.candidateKeys.get(featureType);

    // check if the list does not exist
    if (list == null) {
      list = new LinkedList<>();
      List<SzCandidateKey> listView = Collections.unmodifiableList(list);

      this.candidateKeys.put(featureType, list);
      this.candidateKeyViews.put(featureType, listView);
    }

    // add to the list
    list.add(candidateKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCandidateKeys(Map<String, List<SzCandidateKey>> candidateKeys)
  {
    this.candidateKeys.clear();
    this.candidateKeyViews.clear();
    if (candidateKeys == null) return;
    candidateKeys.entrySet().forEach(entry -> {
      String                featureType = entry.getKey();
      List<SzCandidateKey>  list        = entry.getValue();

      List<SzCandidateKey> listCopy = new LinkedList<>();
      List<SzCandidateKey> listView = Collections.unmodifiableList(listCopy);
      listCopy.addAll(list);

      this.candidateKeys.put(featureType, listCopy);
      this.candidateKeyViews.put(featureType, listView);
    });
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

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzDisclosedRelation> getDisclosedRelations() {
    return Collections.unmodifiableList(this.disclosedRelations);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDisclosedRelations(Collection<SzDisclosedRelation> relations) {
    this.disclosedRelations.clear();
    if (relations != null) {
      this.disclosedRelations.addAll(relations);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addDisclosedRelation(SzDisclosedRelation relation) {
    this.disclosedRelations.add(relation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearDisclosedRelations() {
    this.disclosedRelations.clear();
  }
}
