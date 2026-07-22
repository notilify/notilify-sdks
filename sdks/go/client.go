package notilify

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const defaultBaseURL = "https://api.notilify.com"

type Client struct {
	apiKey  string
	baseURL string
	http    *http.Client
}

type Option func(*Client)

func WithBaseURL(baseURL string) Option { return func(client *Client) { client.baseURL = strings.TrimRight(baseURL, "/") } }
func WithHTTPClient(httpClient *http.Client) Option { return func(client *Client) { client.http = httpClient } }

func New(apiKey string, options ...Option) (*Client, error) {
	if strings.TrimSpace(apiKey) == "" { return nil, errors.New("a Notilify API key is required") }
	client := &Client{apiKey: apiKey, baseURL: defaultBaseURL, http: &http.Client{Timeout: 10 * time.Second}}
	for _, option := range options { option(client) }
	return client, nil
}

type SendMessageInput struct {
	From           string `json:"from"`
	To             string `json:"to"`
	Message        string `json:"message"`
	IdempotencyKey string `json:"-"`
}

type Response struct {
	Status  bool            `json:"status"`
	Message string          `json:"message"`
	Data    json.RawMessage `json:"data"`
}

type APIError struct {
	StatusCode int
	Message    string
	Data       json.RawMessage
}

func (error *APIError) Error() string { return error.Message }

func (client *Client) SendMessage(ctx context.Context, input SendMessageInput) (*Response, error) {
	if err := validateMessage(input); err != nil { return nil, err }
	body, err := json.Marshal(input)
	if err != nil { return nil, err }
	request, err := http.NewRequestWithContext(ctx, http.MethodPost, client.baseURL+"/v1/message", bytes.NewReader(body))
	if err != nil { return nil, err }
	request.Header.Set("Authorization", "Bearer "+client.apiKey)
	request.Header.Set("Content-Type", "application/json")
	if input.IdempotencyKey != "" { request.Header.Set("Idempotency-Key", input.IdempotencyKey) }

	response, err := client.http.Do(request)
	if err != nil { return nil, fmt.Errorf("Notilify request failed: %w", err) }
	defer response.Body.Close()
	responseBody, err := io.ReadAll(response.Body)
	if err != nil { return nil, fmt.Errorf("read Notilify response: %w", err) }
	var decoded Response
	decodeError := json.Unmarshal(responseBody, &decoded)
	if response.StatusCode < 200 || response.StatusCode >= 300 {
		message := decoded.Message
		if message == "" { message = fmt.Sprintf("Notilify request failed with HTTP %d", response.StatusCode) }
		return nil, &APIError{StatusCode: response.StatusCode, Message: message, Data: decoded.Data}
	}
	if decodeError != nil { return nil, fmt.Errorf("decode Notilify response: %w", decodeError) }
	return &decoded, nil
}

func validateMessage(input SendMessageInput) error {
	if strings.TrimSpace(input.From) == "" { return errors.New("from is required") }
	if strings.TrimSpace(input.To) == "" { return errors.New("to is required") }
	if strings.TrimSpace(input.Message) == "" { return errors.New("message is required") }
	if len([]rune(input.Message)) > 160 { return errors.New("message must be 160 characters or fewer") }
	return nil
}
