## ADDED Requirements

### Requirement: Docker image is buildable from project root

The project SHALL provide a multi-stage Dockerfile that produces a runnable container image with the MCP server JAR and JRE 17.

#### Scenario: Successful docker build

- **WHEN** developer runs `docker build` from the `docker/` directory
- **THEN** the build completes without requiring a pre-built JAR on the host
- **AND** the resulting image contains the executable MCP server JAR
- **AND** the image uses Java 17 JRE as runtime

### Requirement: Docker profile provides host-reachable 1C URLs

The server SHALL support a Spring profile `docker` with baked connection URLs using `host.docker.internal` to reach 1C HTTP services on the host machine.

#### Scenario: Docker profile resolves host 1C publication

- **WHEN** the server starts with `SPRING_PROFILES_ACTIVE=docker`
- **THEN** configured connection URLs use `host.docker.internal` instead of `localhost`
- **AND** credentials are read from environment variables `ONEC_USER` and `ONEC_PASSWORD`

#### Scenario: Docker profile preserves multi-connection setup

- **WHEN** the server starts with profile `docker`
- **THEN** connections `ut` and `ut-copy` are configured with ports matching dev publication defaults (8081 and 9090)
- **AND** `datamcp.default-connection` remains `ut`

### Requirement: Docker compose supports local dev credentials

The project SHALL provide a `docker-compose.yml` that passes credentials via environment variables without baking secrets into the image.

#### Scenario: Compose starts with env vars

- **WHEN** developer runs `docker compose up` with `ONEC_USER` and `ONEC_PASSWORD` set (via `.env` or shell)
- **THEN** the MCP server container starts with profile `docker`
- **AND** credentials are available to the application at runtime
- **AND** `host.docker.internal` is resolvable inside the container

### Requirement: Docker dev documentation covers full workflow

The project SHALL document the Docker dev workflow including build, run, and Cursor MCP integration.

#### Scenario: Developer follows docker README

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions cover `docker build`, optional `docker compose up`, and Cursor `mcp.json` configuration using `docker run -i`
- **AND** prerequisites reference `docs/deployment.md` for 1C extension setup
- **AND** an example env file (`.env.example`) lists required variables

### Requirement: Docker build uses local-only workflow

The Docker deployment capability SHALL NOT require external container registry publish for MVP.

#### Scenario: No registry dependency

- **WHEN** developer completes Docker setup
- **THEN** all build and run steps use local `docker build` and `docker run` only
- **AND** no CI registry push is required for the documented workflow
