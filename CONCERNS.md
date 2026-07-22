# Concerns

- 2026-07-22: The initial SDK release implements the canonical single-message `POST /v1/message` path only. Bulk sends, message retrieval, sender IDs, phone numbers, webhooks, and usage APIs remain outside the first release and should be added from the live OpenAPI contract rather than inferred.
- 2026-07-22: PHP, Go, Java, .NET, Composer, and Maven toolchains are not installed on the current machine. Their source and package metadata require CI execution on the supported runtimes before publishing.
- 2026-07-22: The Java SDK deliberately returns the successful response as raw JSON to avoid imposing a JSON library. Revisit typed response parsing if Java users prefer stronger models over a dependency-free client.
- 2026-07-22: None of the packages has been claimed or published in npm, PyPI, Packagist, RubyGems, Maven Central, NuGet, or a public Go module path. Package-name availability and publisher ownership must be confirmed before the first release.
