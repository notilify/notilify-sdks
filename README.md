# Notilify SDKs

Official, dependency-light clients for the [Notilify API](https://docs.notilify.com). Every package is named `notilify` in its language ecosystem and shares the same first-release contract.

This repository is open source under the [MIT License](LICENSE). Contributions and security reports are welcome; review [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and [SECURITY.md](SECURITY.md) before participating.

## SDKs

| Language | Package | Status | Directory |
| --- | --- | --- | --- |
| Node.js | [`notilify`](https://www.npmjs.com/package/notilify) | Published | [`sdks/node`](sdks/node) |
| Python | `notilify` | Registry release pending | [`sdks/python`](sdks/python) |
| PHP | `notilify/notilify` | Registry release pending | [`sdks/php`](sdks/php) |
| Go | `github.com/codelinglabs/notilify-sdks/sdks/go` | Module tag pending | [`sdks/go`](sdks/go) |
| Ruby | `notilify` | Registry release pending | [`sdks/ruby`](sdks/ruby) |
| Java | `com.notilify:notilify` | Registry release pending | [`sdks/java`](sdks/java) |
| .NET | `Notilify` | Registry release pending | [`sdks/dotnet`](sdks/dotnet) |

## Shared contract

Each client:

- authenticates with `Authorization: Bearer <api-key>`
- sends one SMS through `POST /v1/message`
- accepts `from`, `to`, `message`, and an optional idempotency key
- defaults to `https://api.notilify.com`
- supports a custom base URL and timeout for testing and controlled environments
- throws a structured Notilify error for non-2xx responses

```text
Client(apiKey, options)
  .sendMessage({ from, to, message, idempotencyKey })
```

API keys must remain on a trusted server. Do not embed them in browser or mobile application bundles.

## Versioning

SDKs begin at `0.1.0` while the common API and packaging are validated. Language packages can be released independently, but behavior changes should remain aligned across every implementation.

## Validation

Run `./scripts/check-sdks.sh` locally. It exercises each language's request, authentication, idempotency, response, and error checks, then validates or builds the npm, Python, Composer, RubyGems, Maven, and NuGet packages using free local toolchains. It does not require hosted CI or an organization billing plan.
