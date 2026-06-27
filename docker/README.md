# Docker dev setup

Containerized Java MCP server for local development. The 1C infobase and Apache publication stay on the host — see [deployment.md](../docs/deployment.md) for extension setup.

## Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine 20.10+ (Linux)
- Published 1C base with `DataMcp` extension (`http://localhost:8081/datamcp`)
- Optional second publication for `ut-copy` (`http://localhost:9090/datamcp2`)

## Build

From repo root:

```bash
docker build -f docker/Dockerfile -t 1c-data-mcp-server .
```

From `docker/` directory:

```bash
docker build -f Dockerfile -t 1c-data-mcp-server ..
```

## Run with docker run

Interactive STDIO (for manual testing or Cursor):

```bash
docker run -i --rm \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e ONEC_USER=datamcp \
  -e ONEC_PASSWORD=your-password \
  -e JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 \
  1c-data-mcp-server
```

PowerShell:

```powershell
docker run -i --rm `
  --add-host=host.docker.internal:host-gateway `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e ONEC_USER=datamcp `
  -e ONEC_PASSWORD=your-password `
  -e JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 `
  1c-data-mcp-server
```

## Run with docker compose

```bash
cd docker
cp .env.example .env
# edit .env — set ONEC_PASSWORD
docker compose build
docker compose run --rm mcp-server
```

`docker compose run --rm` attaches stdin for STDIO MCP. Use for manual smoke before switching Cursor to Docker.

## Cursor integration

1. Build the image (see above).
2. Copy `.cursor/mcp.json.docker` to `.cursor/mcp.json`, or merge the `1c-data` server entry.
3. Set `ONEC_USER` and `ONEC_PASSWORD` in the `env` section.
4. Reload MCP servers in Cursor.

The Docker profile (`application-docker.yml`) uses `host.docker.internal` instead of `localhost` to reach 1C on the host.

## Configuration

| Variable | Description |
|----------|-------------|
| `ONEC_USER` | 1C username for Basic Auth |
| `ONEC_PASSWORD` | 1C password |
| `SPRING_PROFILES_ACTIVE` | Set to `docker` for container URLs |

Baked connection URLs (profile `docker`):

| Connection | URL |
|------------|-----|
| `ut` | `http://host.docker.internal:8081/datamcp` |
| `ut-copy` | `http://host.docker.internal:9090/datamcp2` |

To use different ports, edit `server/src/main/resources/application-docker.yml` and rebuild the image.

## Smoke test

With 1C published and credentials set:

1. `docker compose run --rm mcp-server` — process starts, waits on stdin
2. In Cursor with `mcp.json.docker`: call `list_connections` → `ut` shows `reachable: true`
3. `find_objects` with query `номенклатур` — Cyrillic results confirm UTF-8

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `reachable: false` for all connections | Check Apache is running on host; verify `host.docker.internal` resolves (add `--add-host=host.docker.internal:host-gateway`) |
| Garbled Cyrillic in MCP responses | Ensure `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8` is set |
| Cursor shows no tools | Use `docker run -i` (interactive stdin); check container logs on stderr |
