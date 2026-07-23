# Releasing Notilify SDKs

Releases use local, free tooling and each ecosystem's public registry. Never place registry tokens, signing keys, or passphrases in this repository.

## Before publishing

1. Update only the package version being released.
2. Review its public API for semantic-versioning compatibility.
3. Run `./scripts/check-sdks.sh` from the repository root.
4. Inspect the generated package and confirm it contains implementation files, README, license, and metadata only.
5. Confirm the registry account, package name, version, repository URL, and support URL.
6. Commit and tag only after explicit approval.
7. Publish only after a separate explicit approval.
8. Install the public version in a clean temporary project before marking it published.

## Registry commands

Run commands from the relevant SDK directory unless noted otherwise.

| SDK | Build or inspect | Publish |
| --- | --- | --- |
| Node.js | `npm test && npm pack --dry-run` | `npm publish` |
| Python | `python3 -m build` | `python3 -m twine upload dist/*` |
| PHP | `composer validate --strict` | Push an approved tag to `notilify/notilify-php`; Packagist reads that repository. |
| Go | `go test ./...` | From the repository root, push `sdks/go/vX.Y.Z`. |
| Ruby | `gem build notilify.gemspec` | `gem push notilify-X.Y.Z.gem` |
| Java | `mvn --batch-mode -Prelease clean deploy` | Publish the validated deployment in Maven Central. |
| .NET | `dotnet pack src/Notilify/Notilify.csproj --configuration Release` | `dotnet nuget push <package> --source https://api.nuget.org/v3/index.json` |

After publication, verify the exact public version from npm, PyPI, Packagist, the Go proxy, RubyGems, Maven Central, or NuGet. A successful upload is not enough; the package must install from its public registry.
