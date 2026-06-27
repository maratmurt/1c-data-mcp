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
```

## Run (STDIO)

```bash
set ONEC_USER=datamcp
set ONEC_PASSWORD=1
java -jar build/libs/1c-data-mcp-server.jar
```

## Cursor integration

`.cursor/mcp.json` is preconfigured. Set `ONEC_USER` and `ONEC_PASSWORD` in the `env` section, then reload MCP servers in Cursor.

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

Integration tests require a running published base:

```bash
set ONEC_USER=datamcp
set ONEC_PASSWORD=1
gradle test --tests com.onec.datamcp.MetadataIntegrationTest
gradle test --tests com.onec.datamcp.QueryIntegrationTest
```
