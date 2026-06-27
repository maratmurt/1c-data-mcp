## 1. Spring Docker Profile

- [x] 1.1 Create `server/src/main/resources/application-docker.yml` with `host.docker.internal` URLs for `ut` (:8081) and `ut-copy` (:9090)
- [x] 1.2 Verify profile inherits STDIO MCP settings and `${ONEC_USER}` / `${ONEC_PASSWORD}` credential placeholders

## 2. Docker Build Artifacts

- [x] 2.1 Create `docker/Dockerfile` — multi-stage: Gradle `bootJar` → `eclipse-temurin:17-jre-alpine` with UTF-8 JVM flags
- [x] 2.2 Create `docker/.dockerignore` — exclude `build/`, `.gradle/`, `build/ib/`, git artifacts
- [x] 2.3 Create `docker/docker-compose.yml` — `SPRING_PROFILES_ACTIVE=docker`, env vars, `extra_hosts: host.docker.internal:host-gateway`
- [x] 2.4 Create `docker/.env.example` with `ONEC_USER` and `ONEC_PASSWORD`

## 3. Build Verification

- [x] 3.1 Run `docker build -t 1c-data-mcp-server .` from `docker/` — build succeeds without pre-built JAR on host
- [x] 3.2 Run `docker run -i --rm -e SPRING_PROFILES_ACTIVE=docker -e ONEC_USER -e ONEC_PASSWORD 1c-data-mcp-server` — process starts, STDIO ready (manual smoke with published 1C)

## 4. Cursor Integration

- [x] 4.1 Create `.cursor/mcp.json.docker` example with `command: docker`, `args: ["run", "-i", "--rm", ...]`, credential env passthrough
- [x] 4.2 Smoke in Cursor: `list_connections` → `ut` reachable; `metadata` and `find_objects` with Cyrillic query

## 5. Documentation

- [x] 5.1 Create `docker/README.md` — build, compose, Cursor setup, prerequisites link to `docs/deployment.md`
- [x] 5.2 Update `server/README.md` — Docker dev workflow section with link to `docker/README.md`
- [x] 5.3 Update `docs/deployment.md` — cross-link to Docker docs
- [x] 5.4 Update `docs/mvp-roadmap.md` — mark stage 5 complete when change is done

## 6. OpenSpec Archive

- [x] 6.1 Run `openspec archive docker` to merge delta specs into `openspec/specs/`
