## 1. Test Configuration

- [x] 1.1 Create `server/src/test/resources/application-test.yml` with connections `ut` (localhost:8081) and `unreachable` (localhost:59999)
- [x] 1.2 Add `@ActiveProfiles("test")` to new integration test classes

## 2. Unit Tests

- [x] 2.1 Create `ConnectionServiceTest` — `resolveConnection("ut")` returns `ut`
- [x] 2.2 Test `resolveConnection(null)` returns `default-connection`
- [x] 2.3 Test `resolveConnection("unknown")` throws `IllegalArgumentException`
- [x] 2.4 Test `resolveConnection` with blank default-connection and no explicit name throws

## 3. Integration Tests

- [x] 3.1 Create `ConnectionIntegrationTest` with `@EnabledIfEnvironmentVariable(named = "ONEC_INTEGRATION", matches = "true")`
- [x] 3.2 Test `listConnections()` returns `ut` (reachable) and `unreachable` (not reachable) with correct `default` flag
- [x] 3.3 Test `metadataService.getSummary("unknown")` or `resolveConnection` path fails before HTTP
- [x] 3.4 Extend `MetadataIntegrationTest` or add `MetadataMultiConnectionTest`: cache isolation between `ut` and second connection (requires `ut-copy` publication or mock — document prerequisite)

## 4. Runtime Verification (gap fixes)

- [x] 4.1 Review `ConnectionService` and `DataMcpTools` — fix any gaps found by new tests (expected: minimal or no changes)
- [x] 4.2 Run `gradle test` — unit tests pass without 1C; integration tests pass with `ONEC_INTEGRATION=true` and published `ut`

## 5. Optional Smoke (second publication)

- [x] 5.1 Publish `build/ib` as `datamcp2` on port 9090 (document in `docs/deployment.md`)
- [x] 5.2 Add `ut-copy` to local `application.yml` (example only, not committed with secrets)
- [x] 5.3 MCP e2e: `list_connections` → two reachable entries; `metadata connection=ut-copy`; `execute_query connection=ut-copy`

## 6. Documentation

- [x] 6.1 Update `server/README.md` — multi-connection config example, test instructions (`ONEC_INTEGRATION`, `application-test.yml`)
- [x] 6.2 Update `docs/deployment.md` — second publication for multi-connection smoke
- [x] 6.3 Update `docs/mvp-roadmap.md` — mark stage 4 complete when change is done

## 7. OpenSpec Archive

- [x] 7.1 Run `openspec archive multi-connection` to merge delta specs into `openspec/specs/`
