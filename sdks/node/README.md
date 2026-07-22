# notilify for Node.js

```js
import Notilify from "notilify";

const notilify = new Notilify(process.env.NOTILIFY_API_KEY);
const response = await notilify.sendMessage({
  from: "NOTILIFY",
  to: "+14155552671",
  message: "Your verification code is 482913",
  idempotencyKey: "verification-482913"
});
```

Requires Node.js 18 or newer.
