# notilify for Go

```bash
go get github.com/codelinglabs/notilify-sdks/sdks/go
```

```go
client, err := notilify.New(os.Getenv("NOTILIFY_API_KEY"))
if err != nil { log.Fatal(err) }

response, err := client.SendMessage(context.Background(), notilify.SendMessageInput{
    From: "NOTILIFY", To: "+14155552671",
    Message: "Your verification code is 482913",
    IdempotencyKey: "verification-482913",
})
```
