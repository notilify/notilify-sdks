# Contributing

Keep public behavior consistent across every SDK. When changing authentication, request fields, error behavior, or response handling:

1. verify the contract against `https://docs.notilify.com/openapi.json`
2. update every affected language implementation
3. add or update the matching language tests
4. update the root support table or README examples when public behavior changes

Never commit real API keys, phone numbers, or message recipient data.
