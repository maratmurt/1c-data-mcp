# Docker dev setup

Containerized Java MCP server for local development. The 1C infobase and Apache publication stay on the host — see [deployment.md](../docs/deployment.md) for extension setup.

## Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine 20.10+ (Linux)
- Published 1C base with `DataMcp` extension (`http://localhost:8081/datamcp`)
- Optional second publication, e.g. `reports` (`http://localhost:8081/reports`)

## Build

From repo root:

```bash
docker build -f docker/Dockerfile -t 1c-data-mcp-server .
```

From `docker/` directory:

```bash
docker build -f Dockerfile -t 1c-data-mcp-server ..
```

## Connection config

Connection URLs and the list of 1C bases are configured via an external YAML file — no image rebuild required.

```bash
cd docker
cp datamcp-local.yml.example datamcp-local.yml
# edit datamcp-local.yml — URLs, connection names, default-connection
```

The file is gitignored (`datamcp-local.yml`). Credentials stay in environment variables (`ONEC_USER`, `ONEC_PASSWORD`), referenced from the YAML via `${ONEC_USER:}` / `${ONEC_PASSWORD:}`.

If `datamcp-local.yml` is missing, the container falls back to baked defaults from `application-docker.yml` in the image (profile `docker`).

## Run with docker run

Interactive STDIO (for manual testing or Cursor):

```bash
docker run -i --rm \
  --add-host=host.docker.internal:host-gateway \
  -v "$(pwd)/datamcp-local.yml:/config/application.yml:ro" \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/ \
  -e ONEC_USER=datamcp \
  -e ONEC_PASSWORD=your-password \
  -e JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 \
  1c-data-mcp-server
```

PowerShell (run from `docker/` directory; adjust the `-v` host path):

```powershell
docker run -i --rm `
  --add-host=host.docker.internal:host-gateway `
  -v C:/Users/marat/PROJECTS/1c-data-mcp/docker/datamcp-local.yml:/config/application.yml:ro `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/ `
  -e ONEC_USER=datamcp `
  -e ONEC_PASSWORD=your-password `
  -e JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 `
  1c-data-mcp-server
```

On Windows, use an absolute host path for `-v` in Cursor — see `.cursor/mcp.json.docker`.

## Run with docker compose (HTTP only)

Compose only runs the Streamable HTTP server. STDIO must use `docker run -i --rm` above — putting STDIO in compose left orphan containers that never exited after clients disconnected.

```bash
cd docker
cp datamcp-local.yml.example datamcp-local.yml
cp .env.example .env
# edit datamcp-local.yml and .env
docker compose up --build -d
```

## Cursor integration

### HTTP (recommended)

1. Start HTTP server: `docker compose up --build -d` from `docker/` (see Streamable HTTP section below).
2. Copy `.cursor/mcp.json.streamable` to `.cursor/mcp.json` (or merge the `1c-data` entry).
3. Set `Authorization: Bearer <DATAMCP_TOKEN>` to match `docker/.env`.
4. Reload MCP servers in Cursor.

Do **not** copy `.cursor/mcp.json.docker` for HTTP mode — STDIO `docker run -i` spawns a new container on each MCP reconnect.

### STDIO via Docker

1. Build the image (see above).
2. Copy `datamcp-local.yml.example` to `datamcp-local.yml` and edit connection URLs.
3. Copy `.cursor/mcp.json.docker` to `.cursor/mcp.json`, or merge the `1c-data` server entry.
4. Update the `-v` path in `mcp.json` to your absolute path to `docker/datamcp-local.yml`.
5. Set `ONEC_USER` and `ONEC_PASSWORD` in the `env` section.
6. Reload MCP servers in Cursor.

## Configuration

| Variable | Description |
|----------|-------------|
| `ONEC_USER` | 1C username for Basic Auth |
| `ONEC_PASSWORD` | 1C password |
| `SPRING_PROFILES_ACTIVE` | Set to `docker` for container defaults |
| `SPRING_CONFIG_ADDITIONAL_LOCATION` | Set to `optional:file:/config/` to load mounted external YAML |

| File | Description |
|------|-------------|
| `docker/datamcp-local.yml` | Local connection config (copy from `datamcp-local.yml.example`) |
| Mount target | `/config/application.yml` inside container (read-only) |

Default connection URLs (when no external file is mounted, profile `docker`):

| Connection | URL |
|------------|-----|
| `ut` | `http://host.docker.internal:8081/datamcp` |

Example connections in `datamcp-local.yml.example` (recommended for Docker/Cursor):

| Connection | URL |
|------------|-----|
| `ut` | `http://host.docker.internal:8081/datamcp` |
| `reports` | `http://host.docker.internal:8081/reports` |

To add or change connections, edit `docker/datamcp-local.yml` and restart the container.

## Smoke test

With 1C published and credentials set:

1. `cp datamcp-local.yml.example datamcp-local.yml` — configure connections
2. `docker run -i --rm ...` (see above) or Cursor with `mcp.json.docker` — process starts, waits on stdin
3. In Cursor with `mcp.json.docker`: call `list_connections` → `ut` and `reports` appear with URLs from `datamcp-local.yml`
4. `find_objects` with `connection=ut` and query `номенклатур` — Cyrillic results confirm UTF-8
5. `metadata` with `connection=reports` — returns СистемаКомпоновкиДанных summary

Without `datamcp-local.yml`: container starts with baked `application-docker.yml` defaults (single `ut` connection).

## Streamable HTTP mode (LAN)

For remote MCP clients (Codex, Claude Desktop) without STDIO:

```bash
cd docker
cp .env.example .env
# set ONEC_USER, ONEC_PASSWORD, DATAMCP_TOKEN in .env
docker compose up --build
```

Endpoint: `http://<host-ip>:8090/mcp` with header `Authorization: Bearer <DATAMCP_TOKEN>`.

Alternative `docker run`:

```bash
docker run --rm -p 8090:8090 \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=streamable,docker \
  -e DATAMCP_TOKEN=your-secret-token \
  -e ONEC_USER=datamcp \
  -e ONEC_PASSWORD=your-password \
  -e JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 \
  1c-data-mcp-server
```

Smoke:

```bash
set DATAMCP_TOKEN=your-secret-token
python ../scripts/mcp-streamable-probe.py
python ../scripts/mcp-cursor-handshake-probe.py
```

Codex setup: [codex-config.example.toml](../docs/codex-config.example.toml)

Cursor HTTP: [.cursor/mcp.json.streamable](../.cursor/mcp.json.streamable)

| Variable | Description |
|----------|-------------|
| `DATAMCP_TOKEN` | Bearer token for MCP HTTP clients |

Container: `1c-data-mcp-http`, `restart: unless-stopped`, port `8090`.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `reachable: false` for all connections | Check Apache is running on host; verify `host.docker.internal` resolves (add `--add-host=host.docker.internal:host-gateway`) |
| Garbled Cyrillic in MCP responses | Ensure `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8` is set |
| Cursor shows no tools (STDIO) | Use `docker run -i` (interactive stdin); check container logs on stderr |
| Cursor `MCP HTTP exchange failed` / `Not connected` | Use HTTP `.cursor/mcp.json.streamable`, not `mcp.json.docker`; reload MCP after `docker compose up`; verify `DATAMCP_TOKEN` matches on server and client |
| HTTP 401 from MCP clients | Set matching `DATAMCP_TOKEN` on server and client (`bearer_token_env_var` in Codex) |
| Multiple orphan `1c-data-mcp-server` containers | Stop STDIO containers; use HTTP compose only; remove `docker run -i` from `mcp.json` |
| LAN client cannot connect | Open Windows Firewall for TCP 8090; verify server binds `0.0.0.0` |
| Config changes not applied | Restart container; verify `-v` path and `SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/` |
