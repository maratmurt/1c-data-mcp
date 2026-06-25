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
  connections:
    - name: ut
      url: http://localhost:8080/ut
      username: ${ONEC_USER:}
      password: ${ONEC_PASSWORD:}
```

## Run (STDIO)

```bash
set ONEC_USER=Admin
set ONEC_PASSWORD=
java -jar build/libs/1c-data-mcp-server.jar
```

## Cursor integration

`.cursor/mcp.json` is preconfigured. Set `ONEC_USER` and `ONEC_PASSWORD` in the `env` section, then reload MCP servers in Cursor.

## MCP tools

| Tool | Description |
|------|-------------|
| `list_connections` | Returns configured connections with ping reachability |

## 1C extension

See `../src/cfe/DataMcp/` and project `docs/deployment.md`.
