# Docker Deployment

Containerized dev setup for the Java MCP server (STDIO transport, local build only).

## Requirements

### Requirement: Docker image is buildable from project root

The project SHALL provide a multi-stage Dockerfile that produces a runnable container image with the MCP server JAR and JRE 17.

#### Scenario: Successful docker build

- **WHEN** developer runs `docker build` from the `docker/` directory
- **THEN** the build completes without requiring a pre-built JAR on the host
- **AND** the resulting image contains the executable MCP server JAR
- **AND** the image uses Java 17 JRE as runtime

### Requirement: Docker profile provides host-reachable 1C URLs

The server SHALL support a Spring profile `docker` with default connection URLs using `host.docker.internal` to reach 1C HTTP services on the host machine. These defaults SHALL apply when no external config file overrides them.

#### Scenario: Docker profile resolves host 1C publication

- **WHEN** the server starts with `SPRING_PROFILES_ACTIVE=docker`
- **AND** no external config overrides connection URLs
- **THEN** configured connection URLs use `host.docker.internal` instead of `localhost`
- **AND** credentials are read from environment variables `ONEC_USER` and `ONEC_PASSWORD`

#### Scenario: Docker profile provides single baked default connection

- **WHEN** the server starts with profile `docker`
- **AND** no external config overrides connections
- **THEN** connection `ut` is configured at `http://host.docker.internal:8081/datamcp`
- **AND** `datamcp.default-connection` remains `ut`

#### Scenario: External config overrides docker profile defaults

- **WHEN** the server starts with profile `docker`
- **AND** an external config file mounted at runtime defines different connection URLs or names
- **THEN** the server uses the external config values
- **AND** no image rebuild is required to change connection topology

### Requirement: Docker compose supports local dev credentials

The project SHALL provide a `docker-compose.yml` that passes credentials via environment variables without baking secrets into the image.

#### Scenario: Compose starts with env vars

- **WHEN** developer runs `docker compose up` with `ONEC_USER` and `ONEC_PASSWORD` set (via `.env` or shell)
- **THEN** the MCP server container starts with profile `docker`
- **AND** credentials are available to the application at runtime
- **AND** `host.docker.internal` is resolvable inside the container

### Requirement: External connection config via volume mount

The Docker deployment SHALL support mounting an external YAML file into the container to configure 1C connections without rebuilding the image.

#### Scenario: Compose mounts external config

- **WHEN** developer creates `docker/datamcp-local.yml` from `datamcp-local.yml.example`
- **AND** runs `docker compose up`
- **THEN** the container mounts the local file read-only at `/config/application.yml`
- **AND** `SPRING_CONFIG_ADDITIONAL_LOCATION` is set to `optional:file:/config/`
- **AND** connection settings from the mounted file are used at runtime

#### Scenario: STDIO uses docker run not compose

- **WHEN** developer runs MCP over STDIO
- **THEN** the documented command is `docker run -i --rm` (with optional `--name 1c-data-mcp-stdio`)
- **AND** compose does not define a STDIO service (avoids orphan stdin containers)

#### Scenario: Cursor docker run mounts external config

- **WHEN** developer configures Cursor using `.cursor/mcp.json.docker` reference
- **THEN** the documented `docker run` args include a volume mount for the external config file
- **AND** instructions explain setting an absolute host path for the mount

### Requirement: Example external config provided

The project SHALL provide `docker/datamcp-local.yml.example` documenting the external config format for connections.

#### Scenario: Developer copies example config

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions describe copying `datamcp-local.yml.example` to `datamcp-local.yml`
- **AND** the example file lists connections `ut` and `reports` with `host.docker.internal` URLs on port 8081
- **AND** credentials use `${ONEC_USER}` and `${ONEC_PASSWORD}` placeholders

#### Scenario: Local config excluded from git

- **WHEN** developer creates `docker/datamcp-local.yml` with machine-specific URLs
- **THEN** the file is listed in `.gitignore` and is not committed to the repository

### Requirement: Docker dev documentation covers full workflow

The project SHALL document the Docker dev workflow including build, run, external connection config, and Cursor MCP integration.

#### Scenario: Developer follows docker README

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions cover `docker build`, optional `docker compose up`, and Cursor `mcp.json` configuration using `docker run -i`
- **AND** instructions describe external connection config via `datamcp-local.yml` without image rebuild
- **AND** prerequisites reference `docs/deployment.md` for 1C extension setup
- **AND** an example env file (`.env.example`) lists required variables

### Requirement: Docker build uses local-only workflow

The Docker deployment capability SHALL NOT require external container registry publish for MVP.

#### Scenario: No registry dependency

- **WHEN** developer completes Docker setup
- **THEN** all build and run steps use local `docker build` and `docker run` only
- **AND** no CI registry push is required for the documented workflow

### Requirement: Docker supports Streamable HTTP mode

The Docker deployment SHALL support running the MCP server in Streamable HTTP mode on port 8090 without requiring stdin (`-i`).

#### Scenario: Docker run with HTTP port

- **WHEN** developer runs `docker run -p 8090:8090 -e SPRING_PROFILES_ACTIVE=streamable,docker -e DATAMCP_TOKEN -e ONEC_USER -e ONEC_PASSWORD 1c-data-mcp-server`
- **THEN** the container starts the streamable MCP server on port 8090
- **AND** the MCP endpoint is reachable at `http://<host>:8090/mcp`
- **AND** stdin is not required

#### Scenario: Compose HTTP service

- **WHEN** developer runs `docker compose up` from `docker/` directory
- **THEN** the HTTP MCP service `mcp-server-http` starts with port 8090 published
- **AND** `DATAMCP_TOKEN`, `ONEC_USER`, and `ONEC_PASSWORD` are passed via environment variables
- **AND** `host.docker.internal` resolves for 1C connections
- **AND** the service uses `restart: unless-stopped`
- **AND** no STDIO service is defined in compose (STDIO uses `docker run -i` only)

#### Scenario: Compose does not include STDIO service

- **WHEN** developer runs `docker compose up`
- **THEN** only the HTTP MCP container starts
- **AND** orphan STDIO containers are not created by compose

### Requirement: Docker HTTP documentation covers LAN access

The Docker documentation SHALL describe Streamable HTTP mode alongside existing STDIO workflow.

#### Scenario: Developer reads docker README for HTTP mode

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions cover `docker compose up` and `docker run -p 8090:8090`
- **AND** instructions list required env vars including `DATAMCP_TOKEN`
- **AND** instructions note that HTTP mode enables LAN access from remote MCP clients
- **AND** instructions document Cursor HTTP config (`.cursor/mcp.json` with `url`, not STDIO `docker run -i`)
- **AND** existing STDIO documentation remains unchanged
