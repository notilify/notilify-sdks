<?php

declare(strict_types=1);

require __DIR__ . '/../src/NotilifyException.php';
require __DIR__ . '/../src/Client.php';

use Notilify\Client;

try {
    new Client('');
    throw new RuntimeException('Expected empty API key validation to fail');
} catch (InvalidArgumentException $error) {
    assert($error->getMessage() === 'A Notilify API key is required');
}

$captured = null;
$transport = function (string $url, array $headers, string $payload, int $timeout) use (&$captured): array {
    $captured = compact('url', 'headers', 'payload', 'timeout');
    return [202, '{"status":true,"message":"Message Accepted","data":{"id":"msg_1"}}'];
};
$client = new Client('key_test', transport: $transport);
$response = $client->sendMessage('NOTILIFY', '+14155552671', 'Your code is 482913', 'send-1');

assert($captured['url'] === 'https://api.notilify.com/v1/message');
assert(in_array('Authorization: Bearer key_test', $captured['headers'], true));
assert(in_array('Idempotency-Key: send-1', $captured['headers'], true));
assert(json_decode($captured['payload'], true)['from'] === 'NOTILIFY');
assert($response['data']['id'] === 'msg_1');

$errorClient = new Client('key_test', transport: fn (): array => [400, '{"status":false,"message":"Invalid sender","data":{"field":"from"}}']);
try {
    $errorClient->sendMessage('BAD', '+14155552671', 'Hello');
    throw new RuntimeException('Expected API error');
} catch (Notilify\NotilifyException $error) {
    assert($error->statusCode === 400);
    assert($error->data['field'] === 'from');
}
