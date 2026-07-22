# notilify for Java

```xml
<dependency>
  <groupId>com.notilify</groupId>
  <artifactId>notilify</artifactId>
  <version>0.1.0</version>
</dependency>
```

```java
import com.notilify.NotilifyClient;
import com.notilify.NotilifyResponse;
import com.notilify.SendMessageRequest;

NotilifyClient client = new NotilifyClient(System.getenv("NOTILIFY_API_KEY"));
NotilifyResponse response = client.sendMessage(new SendMessageRequest(
    "NOTILIFY", "+14155552671", "Your verification code is 482913", "verification-482913"
));
```

Requires Java 11 or newer. Responses expose the HTTP status and raw JSON body without adding a JSON-library dependency.
