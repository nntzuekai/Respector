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
import { PortalNotification } from './portalNotification';
import { Links } from './links';


export interface PortalNotificationsResponse { 
    /**
     * List of portal notifications.
     */
    data?: Array<PortalNotification>;
    /**
     * Map of Map of Object
     */
    metadata?: { [key: string]: { [key: string]: object; }; };
    links?: Links;
}

