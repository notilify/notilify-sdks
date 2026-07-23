# SDK Capability TODO

The seven SDKs currently share one stable public capability: sending a single
SMS through `POST /v1/message`. The items below are planned extensions, not
features available in the published packages today.

Only implement an item after confirming its request and response contract
against the live [Notilify OpenAPI document](https://docs.notilify.com/openapi.json).

## Message operations

- [ ] Add message listing, lookup, and pagination.
- [ ] Add bulk SMS sending with explicit idempotency handling.
- [ ] Return useful typed success responses where each language can do so
      without unnecessary runtime dependencies.

## Messaging resources

- [ ] Add sender ID listing, creation, and retrieval.
- [ ] Add purchased-number listing, availability, lookup, and purchasing.
- [ ] Add webhook creation, listing, updating, deletion, delivery history, and
      test sends.

## Account information

- [ ] Add read-only pricing and country-rate retrieval.
- [ ] Decide explicitly whether API-key management, authentication, payments,
      and other account operations belong in the server SDKs before exposing
      them.

## Shared quality

- [ ] Keep method behavior, validation, errors, pagination, and idempotency
      consistent across all seven languages.
- [ ] Add contract tests and copy-ready documentation for every new operation.
- [ ] Preserve existing public APIs and follow semantic versioning for every
      package release.

An item is complete only when its implementation, tests, and public
documentation are aligned across every affected SDK.
