package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.senzing.api.model.SzAttributeSearchResultType.*;

/**
 * Describes a search attribute result which extends the {@link
 * SzBaseRelatedEntity} to add the {@link SzAttributeSearchResultType} and
 * the {@link SzSearchFeatureScore} instances.
 */
@JsonDeserialize
public class SzAttributeSearchResultImpl extends SzBaseRelatedEntityImpl
    implements SzAttributeSearchResult
{
  /**
   * The search result type.
   */
  private SzAttributeSearchResultType resultType;

  /**
   * The best name score.
   */
  private Integer bestNameScore;

  /**
   * The {@link Map} of {@link String} feature type keys to {@link List}
   * values of {@link SzSearchFeatureScore} instances.
   */
  private Map<String, List<SzSearchFeatureScore>> featureScores;

  /**
   * The {@link Map} of {@link String} feature type keys to <b>unmodifiable</b>
   * {@link List} values of {@link SzSearchFeatureScore} instances.
   */
  private Map<String, List<SzSearchFeatureScore>> featureScoreViews;

  /**
   * The entities related to the resolved entity.
   */
  private List<SzRelatedEntity> relatedEntities;

  /**
   * Default constructor.
   */
  public SzAttributeSearchResultImpl() {
    this.resultType         = null;
    this.bestNameScore      = null;
    this.featureScores      = new LinkedHashMap<>();
    this.featureScoreViews  = new LinkedHashMap<>();
    this.relatedEntities    = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzAttributeSearchResultType getResultType() {
    return this.resultType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResultType(SzAttributeSearchResultType resultType) {
    this.resultType = resultType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getBestNameScore() {
    return this.bestNameScore;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setBestNameScore(Integer score) {
    this.bestNameScore = score;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, List<SzSearchFeatureScore>> getFeatureScores() {
    if (this.featureScoreViews.size() == 0) return null;
    return Collections.unmodifiableMap(this.featureScoreViews);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFeatureScore(SzSearchFeatureScore featureScore) {
    String featureType = featureScore.getFeatureType();
    List<SzSearchFeatureScore> list = this.featureScores.get(featureType);

    // check if the list does not exist
    if (list == null) {
      list = new LinkedList<>();
      List<SzSearchFeatureScore> listView = Collections.unmodifiableList(list);

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
  public void setFeatureScores(
      Map<String, List<SzSearchFeatureScore>> featureScores)
  {
    this.featureScores.clear();
    this.featureScoreViews.clear();
    if (featureScores == null) return;
    featureScores.entrySet().forEach(entry -> {
      String                      featureType = entry.getKey();
      List<SzSearchFeatureScore>  list        = entry.getValue();

      List<SzSearchFeatureScore> listCopy = new LinkedList<>();
      List<SzSearchFeatureScore> listView
          = Collections.unmodifiableList(listCopy);
      listCopy.addAll(list);

      this.featureScores.put(featureType, listCopy);
      this.featureScoreViews.put(featureType, listView);
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzRelatedEntity> getRelatedEntities() {
    return this.relatedEntities;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRelatedEntities(List<SzRelatedEntity> relatedEntities) {
    this.relatedEntities.clear();
    if (relatedEntities != null) {
      this.relatedEntities.addAll(relatedEntities);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRelatedEntity(SzRelatedEntity relatedEntity) {
    if (relatedEntity != null) {
      this.relatedEntities.add(relatedEntity);
    }
  }

  @Override
  public String toString() {
    return "SzAttributeSearchResult{" +
        super.toString() +
        ", resultType=" + this.resultType +
        ", bestNameScore=" + this.bestNameScore +
        ", featureScores=" + this.featureScores +
        ", relatedEntities=" + this.relatedEntities +
        '}';
  }
}
