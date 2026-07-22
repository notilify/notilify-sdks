using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;

namespace Notilify;

public sealed class NotilifyClient
{
    private readonly HttpClient _httpClient;
    private readonly JsonSerializerOptions _jsonOptions = new(JsonSerializerDefaults.Web);

    public NotilifyClient(string apiKey, HttpClient? httpClient = null, string baseUrl = "https://api.notilify.com", TimeSpan? timeout = null)
    {
        if (string.IsNullOrWhiteSpace(apiKey)) throw new ArgumentException("A Notilify API key is required", nameof(apiKey));
        _httpClient = httpClient ?? new HttpClient();
        _httpClient.BaseAddress ??= new Uri(baseUrl.TrimEnd('/') + "/");
        _httpClient.Timeout = timeout ?? TimeSpan.FromSeconds(10);
        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", apiKey);
    }

    public async Task<NotilifyResponse> SendMessageAsync(SendMessageRequest input, CancellationToken cancellationToken = default)
    {
        ValidateMessage(input);
        using var request = new HttpRequestMessage(HttpMethod.Post, "v1/message");
        if (!string.IsNullOrWhiteSpace(input.IdempotencyKey)) request.Headers.Add("Idempotency-Key", input.IdempotencyKey);
        var payload = new { from = input.From, to = input.To, message = input.Message };
        request.Content = new StringContent(JsonSerializer.Serialize(payload, _jsonOptions), Encoding.UTF8, "application/json");

        try
        {
            using var response = await _httpClient.SendAsync(request, cancellationToken).ConfigureAwait(false);
            var body = await response.Content.ReadAsStringAsync(cancellationToken).ConfigureAwait(false);
            if (!response.IsSuccessStatusCode)
            {
                var error = DeserializeError(body);
                throw new NotilifyException(error.Message ?? $"Notilify request failed with HTTP {(int)response.StatusCode}", (int)response.StatusCode, error.Data ?? body);
            }
            return JsonSerializer.Deserialize<NotilifyResponse>(body, _jsonOptions)
                ?? throw new NotilifyException("Notilify returned an empty JSON response", (int)response.StatusCode, body);
        }
        catch (NotilifyException) { throw; }
        catch (Exception error) when (error is HttpRequestException or TaskCanceledException)
        {
            throw new NotilifyException($"Notilify request failed: {error.Message}", inner: error);
        }
    }

    private static void ValidateMessage(SendMessageRequest input)
    {
        ArgumentNullException.ThrowIfNull(input);
        if (string.IsNullOrWhiteSpace(input.From)) throw new ArgumentException("from is required", nameof(input));
        if (string.IsNullOrWhiteSpace(input.To)) throw new ArgumentException("to is required", nameof(input));
        if (string.IsNullOrWhiteSpace(input.Message)) throw new ArgumentException("message is required", nameof(input));
        if (input.Message.EnumerateRunes().Count() > 160) throw new ArgumentException("message must be 160 characters or fewer", nameof(input));
    }

    private static (string? Message, object? Data) DeserializeError(string body)
    {
        try
        {
            using var document = JsonDocument.Parse(body);
            var root = document.RootElement;
            var message = root.TryGetProperty("message", out var messageElement) ? messageElement.GetString() : null;
            object? data = root.TryGetProperty("data", out var dataElement) ? dataElement.Clone() : null;
            return (message, data);
        }
        catch (JsonException) { return (null, null); }
    }
}
