import json
import urllib.error
import urllib.request
from typing import Any, Dict, Optional


class NotilifyError(Exception):
    def __init__(self, message: str, status_code: Optional[int] = None, data: Any = None):
        super().__init__(message)
        self.status_code = status_code
        self.data = data


class Notilify:
    def __init__(self, api_key: str, *, base_url: str = "https://api.notilify.com", timeout: float = 10.0):
        if not isinstance(api_key, str) or not api_key.strip():
            raise ValueError("A Notilify API key is required")
        self.api_key = api_key
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    def send_message(
        self,
        *,
        sender: str,
        to: str,
        message: str,
        idempotency_key: Optional[str] = None,
    ) -> Dict[str, Any]:
        self._validate_message(sender, to, message)
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }
        if idempotency_key:
            headers["Idempotency-Key"] = idempotency_key
        request = urllib.request.Request(
            f"{self.base_url}/v1/message",
            data=json.dumps({"from": sender, "to": to, "message": message}).encode("utf-8"),
            headers=headers,
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                return self._parse_body(response.read())
        except urllib.error.HTTPError as error:
            body = self._parse_body(error.read())
            api_message = body.get("message") if isinstance(body, dict) else None
            data = body.get("data") if isinstance(body, dict) else body
            raise NotilifyError(api_message or f"Notilify request failed with HTTP {error.code}", error.code, data) from error
        except urllib.error.URLError as error:
            raise NotilifyError(f"Notilify request failed: {error.reason}") from error

    @staticmethod
    def _validate_message(sender: str, to: str, message: str) -> None:
        for name, value in (("sender", sender), ("to", to), ("message", message)):
            if not isinstance(value, str) or not value.strip():
                raise ValueError(f"{name} is required")
        if len(message) > 160:
            raise ValueError("message must be 160 characters or fewer")

    @staticmethod
    def _parse_body(raw: bytes) -> Any:
        if not raw:
            return None
        text = raw.decode("utf-8")
        try:
            return json.loads(text)
        except json.JSONDecodeError:
            return text
