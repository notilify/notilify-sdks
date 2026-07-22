<?php

declare(strict_types=1);

namespace Notilify;

use RuntimeException;

final class NotilifyException extends RuntimeException
{
    public function __construct(
        string $message,
        public readonly ?int $statusCode = null,
        public readonly mixed $data = null,
    ) {
        parent::__construct($message);
    }
}
