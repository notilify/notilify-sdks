package com.notilify;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.net.http.HttpClient;

public final class NotilifyClientTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/message", exchange -> {
            if (!"Bearer key_test".equals(exchange.getRequestHeaders().getFirst("Authorization"))) throw new AssertionError("Missing auth");
            if (!"send-1".equals(exchange.getRequestHeaders().getFirst("Idempotency-Key"))) throw new AssertionError("Missing idempotency key");
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            boolean invalidSender = requestBody.contains("\"from\":\"BAD\"");
            if (!invalidSender && !requestBody.contains("\"from\":\"NOTILIFY\"")) throw new AssertionError("Invalid request body");
            String json = invalidSender
                ? "{\"status\":false,\"message\":\"Invalid sender\",\"data\":{\"field\":\"from\"}}"
                : "{\"status\":true,\"message\":\"Message Accepted\",\"data\":{\"id\":\"msg_1\"}}";
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(invalidSender ? 400 : 202, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            NotilifyClient client = new NotilifyClient("key_test", baseUrl, Duration.ofSeconds(2), HttpClient.newHttpClient());
            NotilifyResponse response = client.sendMessage(new SendMessageRequest("NOTILIFY", "+14155552671", "Your code is 482913", "send-1"));
            if (response.getStatusCode() != 202 || !response.getBody().contains("msg_1")) throw new AssertionError("Unexpected response");
            try {
                client.sendMessage(new SendMessageRequest("BAD", "+14155552671", "Hello", "send-1"));
                throw new AssertionError("Expected API error");
            } catch (NotilifyException error) {
                if (error.getStatusCode() != 400 || !error.getResponseBody().contains("Invalid sender")) throw new AssertionError("Unexpected API error");
            }
        } finally {
            server.stop(0);
        }
    }
}
