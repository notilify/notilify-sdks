# notilify for Ruby

```bash
gem install notilify
```

```ruby
require "notilify"

client = Notilify::Client.new(ENV.fetch("NOTILIFY_API_KEY"))
response = client.send_message(
  from: "NOTILIFY", to: "+14155552671",
  message: "Your verification code is 482913",
  idempotency_key: "verification-482913"
)
```

Requires Ruby 3.0 or newer.
