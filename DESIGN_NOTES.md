# Design notes

## Java response handling

The Java SDK returns successful responses as raw JSON alongside the HTTP status. This keeps the SDK dependency-light and lets applications use their existing JSON library. Consumers are responsible for parsing the response body.

A future typed-response API would require an explicit compatibility design before replacing or changing the current response type.
