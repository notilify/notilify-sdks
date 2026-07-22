package com.notilify;

/** Contains an accepted Notilify API response. */
public final class NotilifyResponse {
    private final int statusCode;
    private final String body;

    /**
     * Creates a response value.
     *
     * @param statusCode the successful HTTP status
     * @param body the raw JSON response body
     */
    public NotilifyResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    /**
     * Returns the response status.
     *
     * @return the successful HTTP status
     */
    public int getStatusCode() { return statusCode; }

    /**
     * Returns the response payload.
     *
     * @return the raw JSON response body
     */
    public String getBody() { return body; }
}
