## ADDED Requirements

### Requirement: External YAML config overrides connections at runtime

The MCP server SHALL support loading connection configuration from an external YAML file specified via Spring Boot `spring.config.additional-location`, overriding classpath defaults for the `datamcp` prefix.

#### Scenario: External file overrides baked connections

- **WHEN** `SPRING_CONFIG_ADDITIONAL_LOCATION` points to a directory containing `application.yml` with `datamcp.connections`
- **AND** the server starts successfully
- **THEN** the server uses connection settings from the external file
- **AND** classpath defaults for `datamcp.connections` are overridden

#### Scenario: External file sets default connection

- **WHEN** external config sets `datamcp.default-connection` to a connection name present in `datamcp.connections`
- **THEN** `list_connections` marks that connection with `"default": true`

#### Scenario: No external file uses classpath defaults

- **WHEN** `SPRING_CONFIG_ADDITIONAL_LOCATION` uses the `optional:` prefix
- **AND** no external config file is present at the specified location
- **THEN** the server starts using classpath connection configuration (e.g. profile `docker` defaults)
- **AND** no error is raised for missing external file

#### Scenario: External config references environment credentials

- **WHEN** external YAML references `${ONEC_USER}` and `${ONEC_PASSWORD}` for connection credentials
- **AND** those environment variables are set at runtime
- **THEN** the server uses environment variable values for HTTP Basic Auth to 1C
