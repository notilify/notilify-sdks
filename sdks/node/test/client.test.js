import assert from "node:assert/strict";
import test from "node:test";

import { Notilify, NotilifyError } from "../src/index.js";

test("sends the canonical message request", async () => {
  let captured;
  const client = new Notilify("key_test", {
    fetch: async (url, options) => {
      captured = { url, options };
      return new Response(JSON.stringify({ status: true, message: "Message Accepted", data: { id: "msg_1" } }), { status: 202 });
    }
  });

  const result = await client.sendMessage({
    from: "NOTILIFY",
    to: "+14155552671",
    message: "Your code is 482913",
    idempotencyKey: "send-1"
  });

  assert.equal(captured.url, "https://api.notilify.com/v1/message");
  assert.equal(captured.options.headers.Authorization, "Bearer key_test");
  assert.equal(captured.options.headers["Idempotency-Key"], "send-1");
  assert.deepEqual(JSON.parse(captured.options.body), {
    from: "NOTILIFY",
    to: "+14155552671",
    message: "Your code is 482913"
  });
  assert.equal(result.data.id, "msg_1");
});

test("throws a structured API error", async () => {
  const client = new Notilify("key_test", {
    fetch: async () => new Response(JSON.stringify({ status: false, message: "Invalid sender", data: { field: "from" } }), { status: 400 })
  });

  await assert.rejects(
    client.sendMessage({ from: "BAD", to: "+14155552671", message: "Hello" }),
    (error) => error instanceof NotilifyError && error.statusCode === 400 && error.data.field === "from"
  );
});
