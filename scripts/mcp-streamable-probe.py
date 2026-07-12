#!/usr/bin/env python3
"""Probe MCP Streamable HTTP server: initialize + list_connections."""

import json
import os
import sys
import urllib.error
import urllib.request

DEFAULT_URL = "http://localhost:8090/mcp"
DEFAULT_TOKEN = "dev-token"


def post_mcp(url: str, token: str, payload: dict, session_id: str | None = None) -> tuple[dict, str | None]:
    body = json.dumps(payload).encode("utf-8")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
    }
    if session_id:
        headers["Mcp-Session-Id"] = session_id

    request = urllib.request.Request(url, data=body, method="POST", headers=headers)
    with urllib.request.urlopen(request, timeout=30) as response:
        next_session = response.headers.get("Mcp-Session-Id", session_id)
        raw = response.read().decode("utf-8")
    if raw.startswith("event:"):
        for line in raw.splitlines():
            if line.startswith("data:"):
                raw = line[len("data:") :].strip()
                break
    return json.loads(raw), next_session


def main() -> int:
    url = os.environ.get("MCP_URL", DEFAULT_URL)
    token = os.environ.get("DATAMCP_TOKEN", DEFAULT_TOKEN)

    initialize = {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "initialize",
        "params": {
            "protocolVersion": "2025-11-25",
            "capabilities": {},
            "clientInfo": {"name": "streamable-probe", "version": "1.0"},
        },
    }

    try:
        init_response, session_id = post_mcp(url, token, initialize)
        print(json.dumps(init_response, ensure_ascii=False, indent=2))

        tools_list, session_id = post_mcp(
            url,
            token,
            {"jsonrpc": "2.0", "id": 2, "method": "tools/list", "params": {}},
            session_id,
        )
        print(json.dumps(tools_list, ensure_ascii=False, indent=2))

        list_connections, _ = post_mcp(
            url,
            token,
            {
                "jsonrpc": "2.0",
                "id": 3,
                "method": "tools/call",
                "params": {"name": "list_connections", "arguments": {}},
            },
            session_id,
        )
        print(json.dumps(list_connections, ensure_ascii=False, indent=2))
        return 0
    except urllib.error.HTTPError as error:
        print(f"HTTP {error.code}: {error.read().decode('utf-8', errors='replace')}", file=sys.stderr)
        return 1
    except Exception as error:  # noqa: BLE001
        print(str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
