require "json"
require "net/http"
require "uri"

module Notilify
  class Error < StandardError
    attr_reader :status_code, :data

    def initialize(message, status_code: nil, data: nil)
      super(message)
      @status_code = status_code
      @data = data
    end
  end

  class Client
    def initialize(api_key, base_url: "https://api.notilify.com", timeout: 10, transport: nil)
      raise ArgumentError, "A Notilify API key is required" if api_key.to_s.strip.empty?
      @api_key = api_key
      @base_url = base_url.sub(%r{/$}, "")
      @timeout = timeout
      @transport = transport
    end

    def send_message(from:, to:, message:, idempotency_key: nil)
      validate_message(from, to, message)
      uri = URI("#{@base_url}/v1/message")
      request = Net::HTTP::Post.new(uri)
      request["Authorization"] = "Bearer #{@api_key}"
      request["Content-Type"] = "application/json"
      request["Idempotency-Key"] = idempotency_key if idempotency_key && !idempotency_key.empty?
      request.body = { from: from, to: to, message: message }.to_json
      response = @transport ? @transport.call(uri, request) : perform(uri, request)
      body = parse_body(response.body)
      unless response.code.to_i.between?(200, 299)
        api_message = body.is_a?(Hash) ? body["message"] : nil
        data = body.is_a?(Hash) ? body["data"] : body
        raise Error.new(api_message || "Notilify request failed with HTTP #{response.code}", status_code: response.code.to_i, data: data)
      end
      body
    rescue ArgumentError, Error
      raise
    rescue StandardError => error
      raise Error, "Notilify request failed: #{error.message}"
    end

    private

    def perform(uri, request)
      Net::HTTP.start(uri.hostname, uri.port, use_ssl: uri.scheme == "https", open_timeout: @timeout, read_timeout: @timeout) { |http| http.request(request) }
    end

    def validate_message(from, to, message)
      { "from" => from, "to" => to, "message" => message }.each do |name, value|
        raise ArgumentError, "#{name} is required" unless value.is_a?(String) && !value.strip.empty?
      end
      raise ArgumentError, "message must be 160 characters or fewer" if message.length > 160
    end

    def parse_body(raw)
      return nil if raw.nil? || raw.empty?
      JSON.parse(raw)
    rescue JSON::ParserError
      raw
    end
  end
end
