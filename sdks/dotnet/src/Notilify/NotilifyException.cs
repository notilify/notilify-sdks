namespace Notilify;

public sealed class NotilifyException : Exception
{
    public int? StatusCode { get; }
    public object? DataPayload { get; }

    public NotilifyException(string message, int? statusCode = null, object? data = null, Exception? inner = null)
        : base(message, inner)
    {
        StatusCode = statusCode;
        DataPayload = data;
    }
}
