#!/usr/bin/env bash

set -euo pipefail

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
work_dir="$(mktemp -d "${TMPDIR:-/tmp}/notilify-sdk-checks.XXXXXX")"
trap 'rm -rf "$work_dir"' EXIT

cp -R "$repo_root/sdks" "$work_dir/sdks"
sdk_root="$work_dir/sdks"
mkdir -p "$work_dir/artifacts"

(
  cd "$sdk_root/node"
  npm test
  npm pack --dry-run
)

(
  cd "$sdk_root/python"
  PYTHONPATH=src python3 -m unittest discover -s tests -v
  python3 -m build --outdir "$work_dir/artifacts/python"
)

(
  cd "$sdk_root/php"
  composer validate --strict
  php -d zend.assertions=1 -d assert.exception=1 tests/client-test.php
)

(
  cd "$sdk_root/go"
  go test ./...
)

(
  cd "$sdk_root/ruby"
  ruby test/client_test.rb
  ruby_gem="$work_dir/artifacts/notilify.gem"
  ruby_gem_home="$work_dir/ruby-gems"
  gem build notilify.gemspec --output "$ruby_gem"
  GEM_HOME="$ruby_gem_home" GEM_PATH="$ruby_gem_home" gem install "$ruby_gem" --no-document
  GEM_HOME="$ruby_gem_home" GEM_PATH="$ruby_gem_home" ruby -e 'require "notilify"; abort "Notilify::Client is unavailable" unless defined?(Notilify::Client)'
)

(
  cd "$sdk_root/java"
  mvn --batch-mode -Prelease -Dgpg.skip=true package
  java -cp target/test-classes:target/classes com.notilify.NotilifyClientTest
)

dotnet run --project "$sdk_root/dotnet/tests/Notilify.ContractTests/Notilify.ContractTests.csproj"
dotnet pack "$sdk_root/dotnet/src/Notilify/Notilify.csproj" \
  --configuration Release \
  --output "$work_dir/artifacts/dotnet"

echo "All SDK checks passed."
