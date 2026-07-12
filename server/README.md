# 1C Data MCP Server

Java MCP server (STDIO) for read-only access to 1C databases via the `DataMcp` HTTP extension.

## Requirements

- Java 17+
- Gradle 8.x (or use system `gradle`)
- Published 1C infobase with `DataMcp` extension installed

## Build

```bash
cd server
gradle bootJar
```

Output: `server/build/libs/1c-data-mcp-server.jar`

## Configuration

Edit `src/main/resources/application.yml` or override via environment variables:

| Variable | Description |
|----------|-------------|
| `ONEC_USER` | 1C username for Basic Auth |
| `ONEC_PASSWORD` | 1C password |
| `DATAMCP_TOKEN` | Bearer token for Streamable HTTP profile (`streamable`) |

```yaml
datamcp:
  default-connection: ut
  cache:
    metadata-ttl-minutes: 30
  connections:
    - name: ut
      url: http://localhost:8081/datamcp
      username: ${ONEC_USER:}
      password: ${ONEC_PASSWORD:}
    - name: reports
      url: http://localhost:8081/reports
      username: ${ONEC_USER:}
      password: ${ONEC_PASSWORD:}
```

For Docker, use `docker/datamcp-local.yml` (see [docker/README.md](../docker/README.md)) — same format with `host.docker.internal` URLs.

The AI agent calls `list_connections` to discover configured bases, then passes `connection` to other tools. Omitted `connection` always resolves to `default-connection` (not the last used name).

## Run (STDIO)

```bash
set ONEC_USER=datamcp
set ONEC_PASSWORD=1
java -jar build/libs/1c-data-mcp-server.jar
```

## Run (Streamable HTTP, LAN)

For remote AI agents (Codex, Claude Desktop, ChatGPT, Cursor) over the network:

```bash
set ONEC_USER=datamcp
set ONEC_PASSWORD=1
set DATAMCP_TOKEN=your-secret-token
set SPRING_PROFILES_ACTIVE=streamable
java -jar build/libs/1c-data-mcp-server.jar
```

MCP endpoint: `http://<host-ip>:8090/mcp`.

- **POST** requests require `Authorization: Bearer <DATAMCP_TOKEN>`.
- **GET** SSE (after `initialize`) may omit Bearer if `Mcp-Session-Id` is set — Cursor sends auth only on POST.
- `keep-alive-interval: 5s` in `application-streamable.yml` keeps the SSE listener alive.

Codex example: [docs/codex-config.example.toml](../docs/codex-config.example.toml)

Cursor HTTP: copy [.cursor/mcp.json.streamable](../.cursor/mcp.json.streamable) to `.cursor/mcp.json` and set matching token. Do **not** use `.cursor/mcp.json.docker` for HTTP — STDIO `docker run -i` spawns orphan containers.

Smoke scripts:

```bash
set DATAMCP_TOKEN=your-secret-token
python scripts/mcp-streamable-probe.py
python scripts/mcp-cursor-handshake-probe.py
```

## Cursor integration

**HTTP (recommended):** start the HTTP container (`docker compose up` from `docker/`) or local JAR with profile `streamable`. Use `.cursor/mcp.json` with `url` + Bearer header (see `.cursor/mcp.json.streamable`).

**STDIO via Docker:** copy `.cursor/mcp.json.docker`, set absolute `-v` path and credentials, use `docker run -i`. Prefer HTTP for a single long-lived container without orphan STDIO instances.

Reload MCP servers in Cursor after config or container changes.

## Docker dev setup

Optional containerized workflow — no local JDK required after image build:

```bash
docker build -f docker/Dockerfile -t 1c-data-mcp-server .
```

See [docker/README.md](../docker/README.md) for `docker run`, `docker compose`, and Cursor configuration (`.cursor/mcp.json.docker`).

## MCP tools

| Tool | Description |
|------|-------------|
| `list_connections` | Returns configured connections with ping reachability |
| `metadata` | Configuration summary: name, version, object counts by type |
| `find_objects` | Search metadata objects by substring in name/synonym (cached index) |
| `describe_object` | Structural description of an object, e.g. `Catalog.Номенклатура` |
| `execute_query` | Execute read-only 1C query (must include `ПЕРВЫЕ N`) |

All tools accept optional `connection` parameter (defaults to `datamcp.default-connection`).

## Query limits

`execute_query` validates query text on Java (max length, forbidden tokens) and in 1C (SecuritySvc: `ВЫБРАТЬ` only, `ПЕРВЫЕ N` required). Configure via `datamcp.query`:

```yaml
datamcp:
  query:
    max-length: 10000
    max-rows: 1000
    timeout-seconds: 30
```

Audit logs record query SHA-256 hash only (not full query text).

## Metadata cache

`find_objects` builds an in-memory flat index from 1C on first use per connection. TTL is controlled by `datamcp.cache.metadata-ttl-minutes` (default 30). `describe_object` always calls 1C directly.

## 1C extension

See `../src/cfe/DataMcp/` and project `docs/deployment.md`.

## Tests

### Unit tests (no 1C required)

```bash
cd server
gradle test --tests com.onec.datamcp.ConnectionServiceTest
gradle test --tests com.onec.datamcp.MetadataServiceCacheTest
```

### Integration tests (published base required)

Test profile `application-test.yml` defines `ut` (reachable) and `unreachable` (dead port).

```bash
set ONEC_INTEGRATION=true
set ONEC_USER=datamcp
set ONEC_PASSWORD=1
gradle test --tests com.onec.datamcp.ConnectionIntegrationTest
gradle test --tests com.onec.datamcp.MetadataIntegrationTest
gradle test --tests com.onec.datamcp.QueryIntegrationTest
```

### Multi-connection smoke (optional, second publication)

Publish a second infobase on the same Apache port under a different path (e.g. `reports` at `/reports` on :8081). For legacy configs without extension support, embed the `DataMcp` HTTP service objects in the main configuration. See `docs/deployment.md`.

```bash
set ONEC_MULTI_CONNECTION=true
gradle test --tests com.onec.datamcp.MetadataMultiConnectionIntegrationTest
```

Local MCP config example (not committed; Docker: copy from `docker/datamcp-local.yml.example`):

```yaml
connections:
  - name: reports
    url: http://localhost:8081/reports
    username: ${ONEC_USER:}
    password: ${ONEC_PASSWORD:}
```
