export interface NotilifyOptions {
  baseUrl?: string;
  timeoutMs?: number;
  fetch?: typeof fetch;
}

export interface SendMessageInput {
  from: string;
  to: string;
  message: string;
  idempotencyKey?: string;
}

export interface NotilifyResponse<T = Record<string, unknown>> {
  status: boolean;
  message: string;
  data: T;
}

export class NotilifyError extends Error {
  statusCode?: number;
  data?: unknown;
}

export class Notilify {
  constructor(apiKey: string, options?: NotilifyOptions);
  sendMessage<T = Record<string, unknown>>(input: SendMessageInput): Promise<NotilifyResponse<T>>;
}

export default Notilify;
