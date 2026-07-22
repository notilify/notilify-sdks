package notilify

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestSendMessage(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, request *http.Request) {
		if request.Header.Get("Authorization") != "Bearer key_test" { t.Error("missing bearer authentication") }
		if request.Header.Get("Idempotency-Key") != "send-1" { t.Error("missing idempotency key") }
		var input SendMessageInput
		if err := json.NewDecoder(request.Body).Decode(&input); err != nil { t.Fatal(err) }
		if input.From != "NOTILIFY" { t.Errorf("unexpected sender: %s", input.From) }
		writer.WriteHeader(http.StatusAccepted)
		_, _ = writer.Write([]byte(`{"status":true,"message":"Message Accepted","data":{"id":"msg_1"}}`))
	}))
	defer server.Close()
	client, _ := New("key_test", WithBaseURL(server.URL))

	response, err := client.SendMessage(context.Background(), SendMessageInput{From: "NOTILIFY", To: "+14155552671", Message: "Your code is 482913", IdempotencyKey: "send-1"})

	if err != nil { t.Fatal(err) }
	if response.Message != "Message Accepted" { t.Errorf("unexpected message: %s", response.Message) }
}

func TestAPIError(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, _ *http.Request) {
		writer.WriteHeader(http.StatusBadRequest)
		_, _ = writer.Write([]byte(`{"status":false,"message":"Invalid sender","data":{"field":"from"}}`))
	}))
	defer server.Close()
	client, _ := New("key_test", WithBaseURL(server.URL))

	_, err := client.SendMessage(context.Background(), SendMessageInput{From: "BAD", To: "+14155552671", Message: "Hello"})
	var apiError *APIError
	if !errors.As(err, &apiError) || apiError.StatusCode != 400 { t.Fatalf("expected APIError, got %v", err) }
}
