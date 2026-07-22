package com.notilify;

/** Fields required to submit one transactional message. */
public final class SendMessageRequest {
    private final String from;
    private final String to;
    private final String message;
    private final String idempotencyKey;

    /**
     * Creates a request without an idempotency key.
     *
     * @param from the approved sender name or number
     * @param to the destination phone number in E.164 format
     * @param message the message body, up to 160 Unicode code points
     */
    public SendMessageRequest(String from, String to, String message) {
        this(from, to, message, null);
    }

    /**
     * Creates a request with an optional idempotency key.
     *
     * @param from the approved sender name or number
     * @param to the destination phone number in E.164 format
     * @param message the message body, up to 160 Unicode code points
     * @param idempotencyKey a key that prevents duplicate submissions, or {@code null}
     */
    public SendMessageRequest(String from, String to, String message, String idempotencyKey) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.idempotencyKey = idempotencyKey;
    }

    /**
     * Returns the sender.
     *
     * @return the approved sender name or number
     */
    public String getFrom() { return from; }

    /**
     * Returns the recipient.
     *
     * @return the destination phone number
     */
    public String getTo() { return to; }

    /**
     * Returns the message text.
     *
     * @return the message body
     */
    public String getMessage() { return message; }

    /**
     * Returns the duplicate-prevention key.
     *
     * @return the idempotency key, or {@code null} when omitted
     */
    public String getIdempotencyKey() { return idempotencyKey; }
}
