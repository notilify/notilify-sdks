package com.notilify;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Sends transactional messages through the Notilify API. */
public final class NotilifyClient {
    private static final String DEFAULT_BASE_URL = "https://api.notilify.com";
    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final HttpClient httpClient;

    /**
     * Creates a client using the production API and a ten-second timeout.
     *
     * @param apiKey a server-side Notilify API key
     */
    public NotilifyClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, Duration.ofSeconds(10), HttpClient.newHttpClient());
    }

    /**
     * Creates a client with explicit transport settings.
     *
     * @param apiKey a server-side Notilify API key
     * @param baseUrl the API base URL
     * @param timeout the request timeout
     * @param httpClient the HTTP client used to send requests
     */
    public NotilifyClient(String apiKey, String baseUrl, Duration timeout, HttpClient httpClient) {
        if (apiKey == null || apiKey.trim().isEmpty()) throw new IllegalArgumentException("A Notilify API key is required");
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeout = timeout;
        this.httpClient = httpClient;
    }

    /**
     * Submits one message to Notilify.
     *
     * @param input the message fields and optional idempotency key
     * @return the accepted API response
     * @throws NotilifyException when the request fails or the API returns a non-2xx response
     */
    public NotilifyResponse sendMessage(SendMessageRequest input) {
        validateMessage(input);
        String json = String.format(
            "{\"from\":\"%s\",\"to\":\"%s\",\"message\":\"%s\"}",
            escapeJson(input.getFrom()), escapeJson(input.getTo()), escapeJson(input.getMessage())
        );
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/message"))
            .timeout(timeout)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));
        if (input.getIdempotencyKey() != null && !input.getIdempotencyKey().isEmpty()) {
            builder.header("Idempotency-Key", input.getIdempotencyKey());
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new NotilifyException(
                    "Notilify request failed with HTTP " + response.statusCode(),
                    response.statusCode(),
                    response.body()
                );
            }
            return new NotilifyResponse(response.statusCode(), response.body());
        } catch (IOException error) {
            throw new NotilifyException("Notilify request failed: " + error.getMessage(), error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new NotilifyException("Notilify request was interrupted", error);
        }
    }

    private static void validateMessage(SendMessageRequest input) {
        if (input == null) throw new IllegalArgumentException("Message input is required");
        requireText("from", input.getFrom());
        requireText("to", input.getTo());
        requireText("message", input.getMessage());
        if (input.getMessage().codePointCount(0, input.getMessage().length()) > 160) {
            throw new IllegalArgumentException("message must be 160 characters or fewer");
        }
    }

    private static void requireText(String name, String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " is required");
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
