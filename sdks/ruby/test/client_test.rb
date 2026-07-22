require "minitest/autorun"
require_relative "../lib/notilify"

Response = Struct.new(:code, :body)

class ClientTest < Minitest::Test
  def test_sends_canonical_message
    captured = nil
    transport = lambda do |uri, request|
      captured = [uri, request]
      Response.new("202", '{"status":true,"message":"Message Accepted","data":{"id":"msg_1"}}')
    end
    client = Notilify::Client.new("key_test", transport: transport)

    result = client.send_message(from: "NOTILIFY", to: "+14155552671", message: "Your code is 482913", idempotency_key: "send-1")

    assert_equal "/v1/message", captured[0].path
    assert_equal "Bearer key_test", captured[1]["Authorization"]
    assert_equal "send-1", captured[1]["Idempotency-Key"]
    assert_equal "msg_1", result.dig("data", "id")
  end

  def test_raises_structured_error
    transport = ->(*) { Response.new("400", '{"status":false,"message":"Invalid sender","data":{"field":"from"}}') }
    error = assert_raises(Notilify::Error) do
      Notilify::Client.new("key_test", transport: transport).send_message(from: "BAD", to: "+14155552671", message: "Hello")
    end
    assert_equal 400, error.status_code
    assert_equal "from", error.data["field"]
  end
end
