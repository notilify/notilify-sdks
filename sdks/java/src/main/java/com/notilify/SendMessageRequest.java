package com.notilify;

public final class SendMessageRequest {
    private final String from;
    private final String to;
    private final String message;
    private final String idempotencyKey;

    public SendMessageRequest(String from, String to, String message) {
        this(from, to, message, null);
    }

    public SendMessageRequest(String from, String to, String message, String idempotencyKey) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.idempotencyKey = idempotencyKey;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getMessage() { return message; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
