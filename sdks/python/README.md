# notilify for Python

```python
import os
from notilify import Notilify

client = Notilify(os.environ["NOTILIFY_API_KEY"])
response = client.send_message(
    sender="NOTILIFY",
    to="+14155552671",
    message="Your verification code is 482913",
    idempotency_key="verification-482913",
)
```
