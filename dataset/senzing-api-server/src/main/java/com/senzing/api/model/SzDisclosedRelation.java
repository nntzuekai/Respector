package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDisclosedRelationImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static com.senzing.api.model.SzRelationDirection.*;

/**
 * Describes a disclosed relationship between two entities.
 */
@JsonDeserialize(using=SzDisclosedRelation.Factory.class)
public interface SzDisclosedRelation {
  /**
   * Gets the domain for the disclosed relationship.
   *
   * @return The domain for the disclosed relationship.
   */
  String getDomain();

  /**
   * Sets the domain for the disclosed relationship.
   *
   * @param domain The domain for the disclosed relationship.
   */
  void setDomain(String domain);

  /**
   * Gets the {@link SzRelationDirection} describing the direction of
   * the relationship.  For example:
   * <ul>
   *   <li>{@link SzRelationDirection#INBOUND} -- Indicates that the
   *       relationship is from the second entity to the first entity.
   *   </li>
   *   <li>{@link SzRelationDirection#OUTBOUND} -- Indicates that the
   *       relationship is from the first entity to the second entity.
   *   </li>
   *   <li>{@link SzRelationDirection#BIDIRECTIONAL} -- Indicates that the
   *       relationship goes both ways between the two entities.
   *   </li>
   * </ul>
   *
   * @return The {@link SzRelationDirection} describing the direction of
   *         the relationship.
   */
  SzRelationDirection getDirection();

  /**
   * Sets the {@link SzRelationDirection} describing the direction of
   * the relationship.  For example:
   * <ul>
   *   <li>{@link SzRelationDirection#INBOUND} -- Indicates that the
   *       relationship is from the second entity to the first entity.
   *   </li>
   *   <li>{@link SzRelationDirection#OUTBOUND} -- Indicates that the
   *       relationship is from the first entity to the second entity.
   *   </li>
   *   <li>{@link SzRelationDirection#BIDIRECTIONAL} -- Indicates that the
   *       relationship goes both ways between the two entities.
   *   </li>
   * </ul>
   *
   * @param direction The {@link SzRelationDirection} describing the
   *                  direction of the relationship.
   */
  void setDirection(SzRelationDirection direction);

  /**
   * Returns the <b>unmodifiable</b> {@link Set} of relationship roles
   * associated with the first entity of the disclosed relationship.  This
   * {@link Set} will be empty if the {@linkplain #getDirection() direction} of
   * the relationship is {@link SzRelationDirection#INBOUND}.
   *
   * @return The <b>unmodifiable</b> {@link Set} of relationship roles
   *         associated with the first entity of the disclosed relationship.
   */
  @JsonInclude(NON_EMPTY)
  Set<String> getRoles1();

  /**
   * Adds a role to the {@link Set} of relationship roles associated with the
   * first entity of the disclosed relationship.
   *
   * @param role The role to add to the {@link Set} of relationship roles
   *             associated with the first entity of the disclosed relationship.
   */
  void addRole1(String role);

  /**
   * Removes the specified role from the {@link Set} of relationship roles
   * associated with the first entity of the disclosed relationship.
   *
   * @param role The role to remove from the {@link Set} of relationship roles
   *             associated with the first entity of the disclosed relationship.
   */
  void removeRole1(String role);

  /**
   * Sets the {@link Set} of relationship roles associated with the first
   * entity of the disclosed relationship to those roles in the specified
   * {@link Collection}.
   *
   * @param roles The {@link Collection} of roles to use for the {@link Set} of
   *              relationship roles associated with the first entity of the
   *              disclosed relationship.
   */
  void setRoles1(Collection<String> roles);

  /**
   * Removes all roles from the {@link Set} of relationship roles associated
   * with the first entity of the disclosed relationship.
   */
  void clearRoles1();

  /**
   * Returns the <b>unmodifiable</b> {@link Set} of relationship roles
   * associated with the second entity of the disclosed relationship.  This
   * {@link Set} will be empty if the {@linkplain #getDirection() direction} of
   * the relationship is {@link SzRelationDirection#OUTBOUND}.
   *
   * @return The <b>unmodifiable</b> {@link Set} of relationship roles
   *         associated with the second entity of the disclosed relationship.
   */
  @JsonInclude(NON_EMPTY)
  Set<String> getRoles2();

  /**
   * Adds a role to the {@link Set} of relationship roles associated with the
   * second entity of the disclosed relationship.
   *
   * @param role The role to add to the {@link Set} of relationship roles
   *             associated with the second entity of the disclosed
   *             relationship.
   */
  void addRole2(String role);

  /**
   * Removes the specified role from the {@link Set} of relationship roles
   * associated with the second entity of the disclosed relationship.
   *
   * @param role The role to remove from the {@link Set} of relationship roles
   *             associated with the second entity of the disclosed
   *             relationship.
   */
  void removeRole2(String role);

  /**
   * Sets the {@link Set} of relationship roles associated with the second
   * entity of the disclosed relationship to those roles in the specified
   * {@link Collection}.
   *
   * @param roles The {@link Collection} of roles to use for the {@link Set} of
   *              relationship roles associated with the second entity of the
   *              disclosed relationship.
   */
  void setRoles2(Collection<String> roles);

  /**
   * Removes all roles from the {@link Set} of relationship roles associated
   * with the second entity of the disclosed relationship.
   */
  void clearRoles2();

  /**
   * Gets the <b>unmodifiable</b> {@link List} of {@link SzRelatedFeatures}
   * describing the disclosed relationship feature pairs that matched one
   * another to create the disclosed relationship.
   *
   * @return The <b>unmodifiable</b> {@link List} of {@link SzRelatedFeatures}
   *         describing the disclosed relationship feature pairs that matched
   *         one another to create the disclosed relationship.
   */
  @JsonInclude(NON_EMPTY)
  List<SzRelatedFeatures> getRelatedFeatures();

  /**
   * Adds an {@link SzRelatedFeatures} describing a pair of related features
   * to the list of related feature pairs.
   *
   * @param features The {@link SzRelatedFeatures} describing the pair of
   *                 related features to add to the list of related feature
   *                 pairs.
   */
  void addRelatedFeatures(SzRelatedFeatures features);

  /**
   * Removes the specified {@link SzRelatedFeatures} from the list of related
   * feature pairs.
   *
   * @param features The {@link SzRelatedFeatures} to remove from the list of
   *                 related feature pairs.
   */
  void removeRelatedFeatures(SzRelatedFeatures features);

  /**
   * Removes the {@link SzRelatedFeatures} element from the list of related
   * feature pairs having the specified index.
   *
   * @param index The index of {@link SzRelatedFeatures} to be removed from
   *              the list of related feature pairs.
   */
  void removeRelatedFeatures(int index);

  /**
   * Sets the list of related feature pairs to the specified {@link Collection}
   * of {@link SzRelatedFeatures} instances.
   *
   * @param featuresList The {@link Collection} of {@link SzRelatedFeatures}
   *                     instances to set as the related features.
   */
  void setRelatedFeatures(Collection<SzRelatedFeatures> featuresList);

  /**
   * Removes all related features from the list of {@link SzRelatedFeatures}
   * instances.
   */
  void clearRelatedFeatures();

  /**
   * A {@link ModelProvider} for instances of {@link SzDisclosedRelation}.
   */
  interface Provider extends ModelProvider<SzDisclosedRelation> {
    /**
     * Creates a new instance of {@link SzDisclosedRelation}.
     *
     * @return The new instance of {@link SzDisclosedRelation}
     */
    SzDisclosedRelation create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDisclosedRelation} that produces instances of {@link
   * SzDisclosedRelationImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDisclosedRelation>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDisclosedRelation.class, SzDisclosedRelationImpl.class);
    }

    @Override
    public SzDisclosedRelation create() {
      return new SzDisclosedRelationImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzDisclosedRelation}.
   */
  class Factory extends ModelFactory<SzDisclosedRelation, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDisclosedRelation.class);
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
     * Creates a new instance of {@link SzDisclosedRelation}.
     * @return The new instance of {@link SzDisclosedRelation}.
     */
    public SzDisclosedRelation create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzDisclosedRelation} list.
   *
   * @param jsonObject The {@link JsonObject} describing the features using
   *                   the native API JSON format.
   *
   * @param whyKey The why key associated with the disclosed relations.
   *
   * @return The created instance of {@link SzRelatedFeatures}.
   */
  static List<SzDisclosedRelation> parseDisclosedRelationships(
      JsonObject jsonObject, String whyKey)
  {
    if (jsonObject == null) return null;
    Map<String, Map<SzRelationDirection, Set<String>>> whyKeyMap
        = parseWhyKey(whyKey);

    List<String> sortedDomains = new ArrayList<>(whyKeyMap.size());
    sortedDomains.addAll(whyKeyMap.keySet());
    sortedDomains.sort((c1, c2) -> (c2.length() - c1.length()));

    Map<String, List<SzRelatedFeatures>> featuresMap = new LinkedHashMap<>();

    // loop through the feature types
    jsonObject.keySet().forEach(featureType -> {
      // get the array for the feature type
      JsonArray jsonArray = jsonObject.getJsonArray(featureType);

      // loop through the array for the feature type
      for (JsonObject jsonObj: jsonArray.getValuesAs(JsonObject.class)) {
        // get the domain
        String domain = JsonUtilities.getString(jsonObj, "DOMAIN");

        // parse the feature pair
        SzRelatedFeatures features
            = SzRelatedFeatures.parseRelatedFeatures(jsonObj, featureType);

        // get the feature value
        String featureValue = features.getFeature1().getFeatureValue();

        // check if the domain was not found
        if (domain == null) {
          for (String dom : sortedDomains) {
            String prefix = dom + " ";
            if (featureValue.startsWith(prefix)) {
              domain = dom;
              break;
            }
          }
        }
        if (domain == null) domain = "";

        // get the list for the domain
        List<SzRelatedFeatures> list = featuresMap.get(domain);
        if (list == null) {
          list = new LinkedList<>();
          featuresMap.put(domain, list);
        }

        // add the features to the list for the domain
        list.add(features);
      }
    });

    // create the result list
    List<SzDisclosedRelation> result = new LinkedList<>();

    // loop through the feature pair groups
    featuresMap.forEach((key, list) -> {
      SzDisclosedRelation relationship = SzDisclosedRelation.FACTORY.create();

      String domain = key;

      relationship.setDomain(domain);

      SzRelationDirection dir = null;
      for (SzRelatedFeatures features : list) {
        SzScoredFeature feature1 = features.getFeature1();
        SzScoredFeature feature2 = features.getFeature2();

        String featureType1 = feature1.getFeatureType();

        // check the feature type to get the direction
        switch (featureType1) {
          case "REL_LINK":
            dir = BIDIRECTIONAL;
            break;
          case "REL_POINTER":
            dir = (dir == null) ? OUTBOUND : dir.and(OUTBOUND);
            break;
          case "REL_ANCHOR":
            dir = (dir == null) ? INBOUND : dir.and(INBOUND);
            break;
        }

        // set the direction
        relationship.setDirection(dir);

        // check the usage types
        String usageType1 = feature1.getUsageType();
        String usageType2 = feature2.getUsageType();

        // add the roles
        if (usageType1 != null && usageType1.trim().length() > 0) {
          relationship.addRole1(usageType1);
        }
        if (usageType2 != null && usageType2.trim().length() > 0) {
          relationship.addRole2(usageType2);
        }

        // add the features pair
        relationship.addRelatedFeatures(features);
      }

      // finally check the roles that were not added from the why key
      Map<SzRelationDirection,Set<String>> rolesMap = whyKeyMap.get(domain);
      if (rolesMap != null) {
        // check for missing inbound roles
        Set<String> roles1 = rolesMap.get(INBOUND);
        Set<String> currentRoles1 = relationship.getRoles1();
        // check the inbound roles
        if (roles1 != null) {
          roles1.forEach(role -> {
            if (!currentRoles1.contains(role)) {
              relationship.addRole1(role);
            }
          });
        }

        // check for missing outbound roles
        Set<String> roles2 = rolesMap.get(OUTBOUND);
        Set<String> currentRoles2 = relationship.getRoles2();
        if (roles2 != null) {
          roles2.forEach(role -> {
            if (!currentRoles2.contains(role)) {
              relationship.addRole2(role);
            }
          });
        }
      }

      // add to the result list
      result.add(relationship);
    });

    return result;
  }

  /**
   * Parses the why key.
   */
  private static Map<String, Map<SzRelationDirection, Set<String>>>
    parseWhyKey(String whyKey)
  {
    // check for missing why key
    if (whyKey == null || whyKey.trim().length() == 0) {
      return Collections.emptyMap();
    }

    Map<String, Map<SzRelationDirection, Set<String>>> map
        = new LinkedHashMap<>();

    String                  domain    = null;
    String                  role      = null;
    SzRelationDirection direction = null;
    StringBuilder           sb        = new StringBuilder();
    char[]                  keyChars  = whyKey.toCharArray();

    for (char c : keyChars) {
      // check if we completed a domain
      if ((c == '+' || c == '-') && domain == null) {
        domain = sb.toString();
        if (domain.trim().length() > 0) {
          map.put(domain, new LinkedHashMap<>());
        }
        domain    = null;
        role      = null;
        direction = null;
        sb.delete(0, sb.length());
        continue;
      }

      // check if starting inbound roles
      if (c == '(') {
        domain = sb.toString();
        if (domain.trim().length() > 0) {
          map.put(domain, new LinkedHashMap<>());
        }
        direction = INBOUND;
        role      = null;
        sb.delete(0, sb.length());
        continue;
      }

      // check if transitioning to next role
      if (c == ',') {
        role = sb.toString();
        addRole(map, domain, direction, role);
        role = null;
        sb.delete(0, sb.length());
        continue;
      }

      // check if transitioning to outbound roles
      if (c == ':') {
        role = sb.toString();
        if (role.trim().length() > 0) {
          addRole(map, domain, direction, role);
        }
        direction = OUTBOUND;
        role = null;
        sb.delete(0, sb.length());
        continue;
      }

      // check for role conclusion
      if (c == ')') {
        role = sb.toString();
        if (role.trim().length() > 0) {
          addRole(map, domain, direction, role);
        }
        direction = null;
        role = null;
        domain = null;
        sb.delete(0, sb.length());
        continue;
      }

      // append the character to the string builder
      sb.append(c);
    }

    // check the final state if we are processing a role
    if (domain != null && direction != null) {
      role = sb.toString();
      if (role.trim().length() > 0) {
        addRole(map, domain, direction, role);
      }
    } else if (domain == null) {
      domain = sb.toString();
      if (domain.trim().length() > 0) {
        map.put(domain, new LinkedHashMap<>());
      }
    }

    // return the map
    return map;
  }

  private static void addRole(
      Map<String, Map<SzRelationDirection, Set<String>>>  map,
      String                                              domain,
      SzRelationDirection                                 direction,
      String                                              role)
  {
    Map<SzRelationDirection, Set<String>> roleMap
        = map.get(domain);

    Set<String> roleSet = roleMap.get(direction);
    if (roleSet == null) {
      roleSet = new LinkedHashSet<>();
      roleMap.put(direction, roleSet);
    }
    roleSet.add(role);
  }
}
