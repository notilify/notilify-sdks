#!/usr/bin/env bash

set -euo pipefail

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
work_dir="$(mktemp -d "${TMPDIR:-/tmp}/notilify-sdk-checks.XXXXXX")"
trap 'rm -rf "$work_dir"' EXIT
export npm_config_cache="$work_dir/npm-cache"

cp -R "$repo_root/sdks" "$work_dir/sdks"
sdk_root="$work_dir/sdks"
mkdir -p "$work_dir/artifacts"

python_bin="$(command -v python3)"
if ! "$python_bin" -c 'import build' >/dev/null 2>&1; then
  python_tools="$work_dir/python-tools"
  "$python_bin" -m venv "$python_tools"
  "$python_tools/bin/python" -m pip install --quiet build
  python_bin="$python_tools/bin/python"
fi

ruby_bin=''
for candidate in "$(command -v ruby)" /opt/homebrew/opt/ruby/bin/ruby /usr/local/opt/ruby/bin/ruby; do
  if [ -x "$candidate" ] &&
    "$candidate" -e 'exit Gem::Version.new(RUBY_VERSION) >= Gem::Version.new("3.0") ? 0 : 1'; then
    ruby_bin="$candidate"
    break
  fi
done

if [ -z "$ruby_bin" ]; then
  echo "Ruby 3.0 or newer is required to validate the Ruby SDK." >&2
  exit 1
fi
ruby_gem_bin="$(dirname "$ruby_bin")/gem"

java_bin=''
for candidate in /opt/homebrew/opt/openjdk/bin/java /usr/local/opt/openjdk/bin/java "$(command -v java)"; do
  if [ -x "$candidate" ] && "$candidate" -version >/dev/null 2>&1; then
    java_bin="$candidate"
    break
  fi
done

if [ -z "$java_bin" ]; then
  echo "A working Java runtime is required to validate the Java SDK." >&2
  exit 1
fi

(
  cd "$sdk_root/node"
  npm test
  npm pack --dry-run
)

(
  cd "$sdk_root/python"
  PYTHONPATH=src "$python_bin" -m unittest discover -s tests -v
  "$python_bin" -m build --outdir "$work_dir/artifacts/python"
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
  "$ruby_bin" test/client_test.rb
  ruby_gem="$work_dir/artifacts/notilify.gem"
  ruby_gem_home="$work_dir/ruby-gems"
  "$ruby_gem_bin" build notilify.gemspec --output "$ruby_gem"
  GEM_HOME="$ruby_gem_home" GEM_PATH="$ruby_gem_home" \
    "$ruby_gem_bin" install "$ruby_gem" --no-document
  GEM_HOME="$ruby_gem_home" GEM_PATH="$ruby_gem_home" \
    "$ruby_bin" -e 'require "notilify"; abort "Notilify::Client is unavailable" unless defined?(Notilify::Client)'
)

(
  cd "$sdk_root/java"
  mvn --batch-mode -Prelease -Dgpg.skip=true package
  "$java_bin" -cp target/test-classes:target/classes com.notilify.NotilifyClientTest
)

dotnet run --project "$sdk_root/dotnet/tests/Notilify.ContractTests/Notilify.ContractTests.csproj"
dotnet pack "$sdk_root/dotnet/src/Notilify/Notilify.csproj" \
  --configuration Release \
  --output "$work_dir/artifacts/dotnet"

echo "All SDK checks passed."
