# Notilify for .NET

```bash
dotnet add package Notilify --version 0.1.0
```

```csharp
using Notilify;

var client = new NotilifyClient(Environment.GetEnvironmentVariable("NOTILIFY_API_KEY")!);
var response = await client.SendMessageAsync(new SendMessageRequest(
    "NOTILIFY", "+14155552671", "Your verification code is 482913", "verification-482913"
));
```

Targets .NET 8.
