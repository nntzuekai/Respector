package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDisclosedRelation;
import com.senzing.api.model.SzRelatedFeatures;
import com.senzing.api.model.SzRelationDirection;
import com.senzing.api.model.SzScoredFeature;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.senzing.api.model.SzRelationDirection.*;

/**
 * Describes a disclosed relationship between two entities.
 */
@JsonDeserialize
public class SzDisclosedRelationImpl implements SzDisclosedRelation {
  /**
   * The domain for the relationship.
   */
  private String domain;

  /**
   * The direction for the relationship.
   */
  private SzRelationDirection direction;

  /**
   * The relationship roles associated with the first entity.
   */
  private Set<String> roles1;

  /**
   * The relationship roles associated with the second entity.
   */
  private Set<String> roles2;

  /**
   * The {@link List} of {@link SzRelatedFeatures} describing the disclosed
   * relationship features with the same domain that matched to create the
   * relationship.
   */
  private List<SzRelatedFeatures> relatedFeatures;

  /**
   * Default constructor.
   */
  public SzDisclosedRelationImpl() {
    this.domain           = null;
    this.direction        = null;
    this.roles1           = new LinkedHashSet<>();
    this.roles2           = new LinkedHashSet<>();
    this.relatedFeatures  = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDomain() {
    return this.domain;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzRelationDirection getDirection() {
    return this.direction;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDirection(SzRelationDirection direction) {
    this.direction = direction;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getRoles1() {
    return Collections.unmodifiableSet(this.roles1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRole1(String role) {
    this.roles1.add(role);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRole1(String role) {
    this.roles1.remove(role);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRoles1(Collection<String> roles) {
    this.roles1.clear();
    if (roles != null) {
      this.roles1.addAll(roles);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearRoles1() {
    this.roles1.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getRoles2() {
    return Collections.unmodifiableSet(this.roles2);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRole2(String role) {
    this.roles2.add(role);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRole2(String role) {
    this.roles2.remove(role);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRoles2(Collection<String> roles) {
    this.roles2.clear();
    if (roles != null) {
      this.roles2.addAll(roles);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearRoles2() {
    this.roles2.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzRelatedFeatures> getRelatedFeatures() {
    return Collections.unmodifiableList(this.relatedFeatures);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRelatedFeatures(SzRelatedFeatures features) {
    this.relatedFeatures.add(features);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRelatedFeatures(SzRelatedFeatures features) {
    this.relatedFeatures.remove(features);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRelatedFeatures(int index) {
    this.relatedFeatures.remove(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRelatedFeatures(Collection<SzRelatedFeatures> featuresList) {
    this.relatedFeatures.clear();
    if (featuresList != null) {
      this.relatedFeatures.addAll(featuresList);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearRelatedFeatures() {
    this.relatedFeatures.clear();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    SzDisclosedRelationImpl that = (SzDisclosedRelationImpl) object;
    return Objects.equals(this.getDomain(), that.getDomain()) &&
        this.getDirection() == that.getDirection() &&
        Objects.equals(this.getRoles1(), that.getRoles1()) &&
        Objects.equals(this.getRoles2(), that.getRoles2()) &&
        Objects.equals(this.getRelatedFeatures(), that.getRelatedFeatures());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getDomain(),
                        this.getDirection(),
                        this.getRoles1(),
                        this.getRoles2(),
                        this.getRelatedFeatures());
  }
}
