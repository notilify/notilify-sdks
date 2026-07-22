# notilify for PHP

```php
use Notilify\Client;

$client = new Client(getenv('NOTILIFY_API_KEY'));
$response = $client->sendMessage(
    from: 'NOTILIFY',
    to: '+14155552671',
    message: 'Your verification code is 482913',
    idempotencyKey: 'verification-482913',
);
```
