package com.notilify;

public final class NotilifyResponse {
    private final int statusCode;
    private final String body;

    public NotilifyResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
}
