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

## Run with docker compose

```bash
cd docker
cp datamcp-local.yml.example datamcp-local.yml
cp .env.example .env
# edit datamcp-local.yml and .env
docker compose build
docker compose run --rm mcp-server
```

`docker compose run --rm` attaches stdin for STDIO MCP. Use for manual smoke before switching Cursor to Docker.

## Cursor integration

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
2. `docker compose run --rm mcp-server` — process starts, waits on stdin
3. In Cursor with `mcp.json.docker`: call `list_connections` → `ut` and `reports` appear with URLs from `datamcp-local.yml`
4. `find_objects` with `connection=ut` and query `номенклатур` — Cyrillic results confirm UTF-8
5. `metadata` with `connection=reports` — returns СистемаКомпоновкиДанных summary

Without `datamcp-local.yml`: container starts with baked `application-docker.yml` defaults (single `ut` connection).

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `reachable: false` for all connections | Check Apache is running on host; verify `host.docker.internal` resolves (add `--add-host=host.docker.internal:host-gateway`) |
| Garbled Cyrillic in MCP responses | Ensure `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8` is set |
| Cursor shows no tools | Use `docker run -i` (interactive stdin); check container logs on stderr |
| Config changes not applied | Restart container; verify `-v` path and `SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/` |
