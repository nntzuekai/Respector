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
import { HttpMethod } from './httpMethod';


/**
 * Request logged by the API Gateway
 */
export interface Request { 
    method?: HttpMethod;
    /**
     * List of String List
     */
    headers?: { [key: string]: Array<string>; };
    uri?: string;
    body?: string;
}

