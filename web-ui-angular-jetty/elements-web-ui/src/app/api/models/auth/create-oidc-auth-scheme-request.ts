/* tslint:disable */

/**
 * Represents a request to create an Auth Scheme for an Application.
 */
export interface CreateOidcAuthSchemeRequest {

  /**
   * A unique name used to identify the scheme within the instance of Elements.
   * When validating from an external source (e.g. Google or Apple SSO), must match the 'iss' property of the decoded JWT.
   */
  issuer: string;

  /**
   * A set of JWKs containing the keys required to validate JWT signatures.
   */
  keys: Array<JsonWebKey>;

  /**
   * The URL for the JWK data. Will attempt to refresh keys if the kid cannot be found in the collection.
   */
  keysUrl: string;

  /**
   * The JWK format. Defaults to application/json
   */
  mediaType: string;
}
