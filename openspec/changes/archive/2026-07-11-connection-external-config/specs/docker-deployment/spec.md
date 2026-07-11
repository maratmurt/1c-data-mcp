## ADDED Requirements

### Requirement: External connection config via volume mount

The Docker deployment SHALL support mounting an external YAML file into the container to configure 1C connections without rebuilding the image.

#### Scenario: Compose mounts external config

- **WHEN** developer creates `docker/datamcp-local.yml` from `datamcp-local.yml.example`
- **AND** runs `docker compose run --rm mcp-server`
- **THEN** the container mounts the local file read-only at `/config/application.yml`
- **AND** `SPRING_CONFIG_ADDITIONAL_LOCATION` is set to `optional:file:/config/`
- **AND** connection settings from the mounted file are used at runtime

#### Scenario: Cursor docker run mounts external config

- **WHEN** developer configures Cursor using `.cursor/mcp.json.docker` reference
- **THEN** the documented `docker run` args include a volume mount for the external config file
- **AND** instructions explain setting an absolute host path for the mount

### Requirement: Example external config provided

The project SHALL provide `docker/datamcp-local.yml.example` documenting the external config format for connections.

#### Scenario: Developer copies example config

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions describe copying `datamcp-local.yml.example` to `datamcp-local.yml`
- **AND** the example file shows `datamcp.default-connection` and `datamcp.connections` with `host.docker.internal` URLs
- **AND** credentials use `${ONEC_USER}` and `${ONEC_PASSWORD}` placeholders

#### Scenario: Local config excluded from git

- **WHEN** developer creates `docker/datamcp-local.yml` with machine-specific URLs
- **THEN** the file is listed in `.gitignore` and is not committed to the repository

## MODIFIED Requirements

### Requirement: Docker profile provides host-reachable 1C URLs

The server SHALL support a Spring profile `docker` with default connection URLs using `host.docker.internal` to reach 1C HTTP services on the host machine. These defaults SHALL apply when no external config file overrides them.

#### Scenario: Docker profile resolves host 1C publication

- **WHEN** the server starts with `SPRING_PROFILES_ACTIVE=docker`
- **AND** no external config overrides connection URLs
- **THEN** configured connection URLs use `host.docker.internal` instead of `localhost`
- **AND** credentials are read from environment variables `ONEC_USER` and `ONEC_PASSWORD`

#### Scenario: Docker profile preserves multi-connection setup

- **WHEN** the server starts with profile `docker`
- **AND** no external config overrides connections
- **THEN** connections `ut` and `ut-copy` are configured with ports matching dev publication defaults (8081 and 9090)
- **AND** `datamcp.default-connection` remains `ut`

#### Scenario: External config overrides docker profile defaults

- **WHEN** the server starts with profile `docker`
- **AND** an external config file mounted at runtime defines different connection URLs or names
- **THEN** the server uses the external config values
- **AND** no image rebuild is required to change connection topology

### Requirement: Docker dev documentation covers full workflow

The project SHALL document the Docker dev workflow including build, run, external connection config, and Cursor MCP integration.

#### Scenario: Developer follows docker README

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions cover `docker build`, optional `docker compose up`, and Cursor `mcp.json` configuration using `docker run -i`
- **AND** instructions describe external connection config via `datamcp-local.yml` without image rebuild
- **AND** prerequisites reference `docs/deployment.md` for 1C extension setup
- **AND** an example env file (`.env.example`) lists required variables
