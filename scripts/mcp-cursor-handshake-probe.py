#!/usr/bin/env python3
"""Simulate Cursor MCP handshake: POST initialize, GET SSE without Authorization, POST tools."""

import json
import os
import sys
import threading
import urllib.error
import urllib.request

DEFAULT_URL = "http://localhost:8090/mcp"
DEFAULT_TOKEN = "dev-token"


def main() -> int:
    url = os.environ.get("MCP_URL", DEFAULT_URL)
    token = os.environ.get("DATAMCP_TOKEN", DEFAULT_TOKEN)

    init_body = {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "initialize",
        "params": {
            "protocolVersion": "2025-11-25",
            "capabilities": {"elicitation": {"form": {}}},
            "clientInfo": {"name": "cursor-vscode", "version": "1.0.0"},
        },
    }
    post_headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
    }

    try:
        req = urllib.request.Request(
            url, data=json.dumps(init_body).encode(), method="POST", headers=post_headers
        )
        with urllib.request.urlopen(req, timeout=15) as response:
            session_id = response.headers.get("Mcp-Session-Id")
            init_raw = response.read().decode("utf-8")
        print("initialize ok, session:", session_id)

        get_result: dict = {}

        def open_sse_listener() -> None:
            headers = {
                "Accept": "text/event-stream",
                "Mcp-Session-Id": session_id,
            }
            request = urllib.request.Request(url, method="GET", headers=headers)
            try:
                with urllib.request.urlopen(request, timeout=15) as response:
                    get_result["status"] = response.status
                    get_result["ctype"] = response.headers.get("Content-Type")
                    get_result["chunk"] = response.read(300).decode("utf-8", errors="replace")
            except Exception as error:  # noqa: BLE001
                get_result["error"] = repr(error)

        listener = threading.Thread(target=open_sse_listener, daemon=True)
        listener.start()

        initialized = {"jsonrpc": "2.0", "method": "notifications/initialized", "params": {}}
        post_headers_with_session = dict(post_headers)
        post_headers_with_session["Mcp-Session-Id"] = session_id
        req = urllib.request.Request(
            url,
            data=json.dumps(initialized).encode(),
            method="POST",
            headers=post_headers_with_session,
        )
        with urllib.request.urlopen(req, timeout=15) as response:
            print("notifications/initialized:", response.status)

        listener.join(timeout=20)
        print("GET SSE (no Authorization):", get_result)

        tools_call = {
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/call",
            "params": {"name": "list_connections", "arguments": {}},
        }
        req = urllib.request.Request(
            url,
            data=json.dumps(tools_call).encode(),
            method="POST",
            headers=post_headers_with_session,
        )
        with urllib.request.urlopen(req, timeout=30) as response:
            raw = response.read().decode("utf-8")
        if raw.startswith("event:"):
            for line in raw.splitlines():
                if line.startswith("data:"):
                    raw = line[len("data:") :].strip()
                    break
        result = json.loads(raw)
        print("list_connections ok:", not result.get("result", {}).get("isError", True))
        return 0
    except urllib.error.HTTPError as error:
        print(f"HTTP {error.code}: {error.read().decode('utf-8', errors='replace')}", file=sys.stderr)
        return 1
    except Exception as error:  # noqa: BLE001
        print(str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
