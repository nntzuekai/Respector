/**
 * Gravitee.io Portal Rest API
 * API dedicated to the devportal part of Gravitee
 *
 * Contact: contact@graviteesource.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface ConfigurationOAuth2Authentication { 
    /**
     * client Id if an OAuth2 Identity Provider is used
     */
    clientId?: string;
    /**
     * name of the OAuth2 provider if used
     */
    name?: string;
    /**
     * color to display for an OAuth2 Identity Provider if used
     */
    color?: string;
    /**
     * Authorization endpoint of an OAuth2 Identity Provider if used
     */
    authorizationEndpoint?: string;
    /**
     * User logout endpoint of an OAuth2 Identity Provider if used
     */
    userLogoutEndpoint?: string;
    /**
     * List of scopes of an OAuth2 Identity Provider if used
     */
    scope?: Array<string>;
}

