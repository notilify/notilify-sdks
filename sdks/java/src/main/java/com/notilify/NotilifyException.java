package com.notilify;

/** Describes an unsuccessful Notilify API or transport request. */
public final class NotilifyException extends RuntimeException {
    /** HTTP status returned by Notilify, or {@code null} for transport failures. */
    private final Integer statusCode;
    /** Response body returned by Notilify, or {@code null} for transport failures. */
    private final String responseBody;

    /**
     * Creates an exception for a non-successful HTTP response.
     *
     * @param message a safe diagnostic message
     * @param statusCode the HTTP status code
     * @param responseBody the API response body
     */
    public NotilifyException(String message, Integer statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * Creates an exception for a transport failure.
     *
     * @param message a safe diagnostic message
     * @param cause the underlying transport error
     */
    public NotilifyException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.responseBody = null;
    }

    /**
     * Returns the response status.
     *
     * @return the HTTP status, or {@code null} when no response was received
     */
    public Integer getStatusCode() { return statusCode; }

    /**
     * Returns the diagnostic response body.
     *
     * @return the API response body, or {@code null} when no response was received
     */
    public String getResponseBody() { return responseBody; }
}
