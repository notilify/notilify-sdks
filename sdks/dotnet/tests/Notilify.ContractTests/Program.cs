using System.Net;
using System.Text;
using Notilify;

HttpRequestMessage? captured = null;
string? capturedBody = null;
var handler = new StubHandler(async request =>
{
    captured = request;
    capturedBody = await request.Content!.ReadAsStringAsync();
    return new HttpResponseMessage(HttpStatusCode.Accepted)
    {
        Content = new StringContent("""{"status":true,"message":"Message Accepted","data":{"id":"msg_1"}}""", Encoding.UTF8, "application/json")
    };
});
var http = new HttpClient(handler) { BaseAddress = new Uri("https://api.notilify.com/") };
var client = new NotilifyClient("key_test", http);

var response = await client.SendMessageAsync(new SendMessageRequest("NOTILIFY", "+14155552671", "Your code is 482913", "send-1"));

Assert(captured!.RequestUri!.ToString() == "https://api.notilify.com/v1/message", "Unexpected request URL");
Assert(captured.Headers.Authorization!.ToString() == "Bearer key_test", "Missing bearer authentication");
Assert(captured.Headers.GetValues("Idempotency-Key").Single() == "send-1", "Missing idempotency key");
Assert(capturedBody!.Contains("\"from\":\"NOTILIFY\""), "Unexpected request body");
Assert(response.Data.GetProperty("id").GetString() == "msg_1", "Unexpected response data");

var errorHandler = new StubHandler(_ => Task.FromResult(new HttpResponseMessage(HttpStatusCode.BadRequest)
{
    Content = new StringContent("""{"status":false,"message":"Invalid sender","data":{"field":"from"}}""", Encoding.UTF8, "application/json")
}));
var errorClient = new NotilifyClient("key_test", new HttpClient(errorHandler) { BaseAddress = new Uri("https://api.notilify.com/") });
try
{
    await errorClient.SendMessageAsync(new SendMessageRequest("BAD", "+14155552671", "Hello"));
    throw new InvalidOperationException("Expected API error");
}
catch (NotilifyException error)
{
    Assert(error.StatusCode == 400, "Unexpected API error status");
    Assert(error.Message == "Invalid sender", "Unexpected API error message");
}

static void Assert(bool condition, string message)
{
    if (!condition) throw new InvalidOperationException(message);
}

sealed class StubHandler(Func<HttpRequestMessage, Task<HttpResponseMessage>> send) : HttpMessageHandler
{
    protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken) => send(request);
}
