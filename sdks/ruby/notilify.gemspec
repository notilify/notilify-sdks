Gem::Specification.new do |spec|
  spec.name = "notilify"
  spec.version = "0.1.0"
  spec.summary = "Official Ruby SDK for the Notilify transactional SMS API"
  spec.authors = ["Notilify"]
  spec.email = ["support@notilify.com"]
  spec.homepage = "https://notilify.com"
  spec.files = Dir["lib/**/*.rb"] + ["LICENSE", "README.md"]
  spec.require_paths = ["lib"]
  spec.required_ruby_version = ">= 3.0"
  spec.license = "MIT"
  spec.metadata = {
    "source_code_uri" => "https://github.com/codelinglabs/notilify-sdks/tree/main/sdks/ruby",
    "bug_tracker_uri" => "https://github.com/codelinglabs/notilify-sdks/issues",
    "rubygems_mfa_required" => "true"
  }
end
