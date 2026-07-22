package com.notilify;

public final class NotilifyException extends RuntimeException {
    private final Integer statusCode;
    private final String responseBody;

    public NotilifyException(String message, Integer statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public NotilifyException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.responseBody = null;
    }

    public Integer getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
}
