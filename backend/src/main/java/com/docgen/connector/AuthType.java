package com.docgen.connector;

/**
 * Authentication strategy used when an {@link ApiConnector} calls an external API.
 */
public enum AuthType {
    NONE,
    BASIC,
    BEARER,
    API_KEY,
    OAUTH2
}
