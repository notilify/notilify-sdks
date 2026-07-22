<?php

declare(strict_types=1);

namespace Notilify;

final class Client
{
    public function __construct(
        private readonly string $apiKey,
        private readonly string $baseUrl = 'https://api.notilify.com',
        private readonly int $timeoutSeconds = 10,
        private readonly ?\Closure $transport = null,
    ) {
        if (trim($apiKey) === '') {
            throw new \InvalidArgumentException('A Notilify API key is required');
        }
    }

    public function sendMessage(
        string $from,
        string $to,
        string $message,
        ?string $idempotencyKey = null,
    ): array {
        self::validateMessage($from, $to, $message);
        $headers = [
            'Authorization: Bearer ' . $this->apiKey,
            'Content-Type: application/json',
        ];
        if ($idempotencyKey !== null && $idempotencyKey !== '') {
            $headers[] = 'Idempotency-Key: ' . $idempotencyKey;
        }

        $url = rtrim($this->baseUrl, '/') . '/v1/message';
        $payload = json_encode(['from' => $from, 'to' => $to, 'message' => $message], JSON_THROW_ON_ERROR);
        [$statusCode, $rawBody] = $this->transport
            ? ($this->transport)($url, $headers, $payload, $this->timeoutSeconds)
            : $this->performRequest($url, $headers, $payload);
        $body = self::parseBody($rawBody);

        if ($statusCode < 200 || $statusCode >= 300) {
            $apiMessage = is_array($body) && is_string($body['message'] ?? null) ? $body['message'] : null;
            $data = is_array($body) ? ($body['data'] ?? null) : $body;
            throw new NotilifyException($apiMessage ?? "Notilify request failed with HTTP {$statusCode}", $statusCode, $data);
        }
        if (!is_array($body)) {
            throw new NotilifyException('Notilify returned an invalid JSON response', $statusCode, $body);
        }
        return $body;
    }

    private function performRequest(string $url, array $headers, string $payload): array
    {
        $curl = curl_init($url);
        curl_setopt_array($curl, [
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => $payload,
            CURLOPT_HTTPHEADER => $headers,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_TIMEOUT => $this->timeoutSeconds,
        ]);
        $rawBody = curl_exec($curl);
        if ($rawBody === false) {
            $error = curl_error($curl);
            curl_close($curl);
            throw new NotilifyException('Notilify request failed: ' . $error);
        }
        $statusCode = curl_getinfo($curl, CURLINFO_RESPONSE_CODE);
        curl_close($curl);
        return [$statusCode, $rawBody];
    }

    private static function validateMessage(string $from, string $to, string $message): void
    {
        foreach (['from' => $from, 'to' => $to, 'message' => $message] as $name => $value) {
            if (trim($value) === '') throw new \InvalidArgumentException("{$name} is required");
        }
        if (mb_strlen($message) > 160) throw new \InvalidArgumentException('message must be 160 characters or fewer');
    }

    private static function parseBody(string $rawBody): mixed
    {
        if ($rawBody === '') return null;
        try { return json_decode($rawBody, true, flags: JSON_THROW_ON_ERROR); }
        catch (\JsonException) { return $rawBody; }
    }
}
