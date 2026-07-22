using System.Text.Json;
using System.Text.Json.Serialization;

namespace Notilify;

public sealed record SendMessageRequest(
    [property: JsonPropertyName("from")] string From,
    [property: JsonPropertyName("to")] string To,
    [property: JsonPropertyName("message")] string Message,
    string? IdempotencyKey = null
);

public sealed record NotilifyResponse(
    [property: JsonPropertyName("status")] bool Status,
    [property: JsonPropertyName("message")] string Message,
    [property: JsonPropertyName("data")] JsonElement Data
);
