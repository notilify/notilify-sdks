const DEFAULT_BASE_URL = "https://api.notilify.com";

export class NotilifyError extends Error {
  constructor(message, { statusCode, data, cause } = {}) {
    super(message, { cause });
    this.name = "NotilifyError";
    this.statusCode = statusCode;
    this.data = data;
  }
}

export class Notilify {
  constructor(apiKey, options = {}) {
    if (!apiKey?.trim()) throw new TypeError("A Notilify API key is required");
    this.apiKey = apiKey;
    this.baseUrl = (options.baseUrl ?? DEFAULT_BASE_URL).replace(/\/$/, "");
    this.timeoutMs = options.timeoutMs ?? 10_000;
    this.fetch = options.fetch ?? globalThis.fetch;
    if (typeof this.fetch !== "function") throw new TypeError("A fetch implementation is required");
  }

  async sendMessage(input) {
    validateMessage(input);
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.timeoutMs);
    const headers = {
      Authorization: `Bearer ${this.apiKey}`,
      "Content-Type": "application/json"
    };
    if (input.idempotencyKey) headers["Idempotency-Key"] = input.idempotencyKey;

    try {
      const response = await this.fetch(`${this.baseUrl}/v1/message`, {
        method: "POST",
        headers,
        body: JSON.stringify({ from: input.from, to: input.to, message: input.message }),
        signal: controller.signal
      });
      const body = await parseBody(response);
      if (!response.ok) {
        throw new NotilifyError(body?.message ?? `Notilify request failed with HTTP ${response.status}`, {
          statusCode: response.status,
          data: body?.data ?? body
        });
      }
      return body;
    } catch (error) {
      if (error instanceof NotilifyError) throw error;
      const message = error?.name === "AbortError" ? `Notilify request timed out after ${this.timeoutMs}ms` : error?.message;
      throw new NotilifyError(message ?? "Notilify request failed", { cause: error });
    } finally {
      clearTimeout(timeout);
    }
  }
}

function validateMessage(input) {
  if (!input || typeof input !== "object") throw new TypeError("Message input is required");
  for (const field of ["from", "to", "message"]) {
    if (typeof input[field] !== "string" || !input[field].trim()) throw new TypeError(`${field} is required`);
  }
  if ([...input.message].length > 160) throw new TypeError("message must be 160 characters or fewer");
}

async function parseBody(response) {
  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

export default Notilify;
