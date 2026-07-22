import io
import json
import unittest
from unittest.mock import patch
import urllib.error

from notilify import Notilify, NotilifyError


class Response:
    def __init__(self, body: dict):
        self.body = json.dumps(body).encode()

    def __enter__(self):
        return self

    def __exit__(self, *_args):
        return False

    def read(self):
        return self.body


class NotilifyClientTest(unittest.TestCase):
    @patch("urllib.request.urlopen")
    def test_sends_canonical_message(self, urlopen):
        urlopen.return_value = Response({"status": True, "message": "Message Accepted", "data": {"id": "msg_1"}})
        client = Notilify("key_test")

        result = client.send_message(sender="NOTILIFY", to="+14155552671", message="Your code is 482913", idempotency_key="send-1")

        request = urlopen.call_args.args[0]
        self.assertEqual(request.full_url, "https://api.notilify.com/v1/message")
        self.assertEqual(request.headers["Authorization"], "Bearer key_test")
        self.assertEqual(request.headers["Idempotency-key"], "send-1")
        self.assertEqual(json.loads(request.data)["from"], "NOTILIFY")
        self.assertEqual(result["data"]["id"], "msg_1")

    @patch("urllib.request.urlopen")
    def test_raises_structured_error(self, urlopen):
        body = io.BytesIO(json.dumps({"status": False, "message": "Invalid sender", "data": {"field": "from"}}).encode())
        urlopen.side_effect = urllib.error.HTTPError("url", 400, "Bad Request", {}, body)

        with self.assertRaises(NotilifyError) as context:
            Notilify("key_test").send_message(sender="BAD", to="+14155552671", message="Hello")

        self.assertEqual(context.exception.status_code, 400)
        self.assertEqual(context.exception.data["field"], "from")


if __name__ == "__main__":
    unittest.main()
