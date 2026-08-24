# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Spring Boot 3 / Java 21 REST wrapper around the [SignService Integration API](https://github.com/idsec-solutions/signservice-integration-api). It contains almost no signature logic of its own — the actual signing work lives in the external `signservice-integration-impl`, `-xml` and `-pdf` artifacts (`se.idsec.signservice.integration`, version pinned by `sign.integration.version` in `pom.xml`). This repo wires those beans, exposes them over HTTP, and adds authentication, policy-based authorization and Redis-backed caching.

Consequence: when a change involves how a signature request/response is actually built or validated, the code is *not* here — it is in `signservice-integration-impl`. What is here is configuration, wiring, transport and access control.

## Commands

```bash
mvn clean install                 # build
mvn test                          # run tests
mvn test -Dtest=SignServiceIntegrationApplicationTest   # single test
mvn spring-boot:run -Dspring-boot.run.profiles=sandbox  # run locally
mvn jib:dockerBuild@local         # build local Docker image (execution phase is 'none', so invoke explicitly)
```

The test suite is a single Spring context-load test under the `sandbox` profile. It is effectively a wiring smoke test — if bean configuration breaks, this is what catches it, so run it after touching anything in `config/`.

`mvn enforcer` runs `dependencyConvergence` on every build. Adding a dependency that pulls a conflicting transitive version will fail the build; pin the override in `<dependencyManagement>` (see the `cryptacular` entry for the pattern).

## Configuration model

Configuration is the dominant concern in this codebase, and it is layered:

1. `application.properties` — defaults, plus `application.config.prefix` (default `classpath:`), which every other resource reference is built on top of. In production this is set to a `file:///...` directory so keys and property files come from outside the jar.
2. Profile files (`application-sandbox.properties`, `application-edusign-test.properties`) — override the prefix to point at the corresponding classpath subdirectory (`sandbox/`, `edusign-test/`), which then redirects the policy file, user file and credentials.
3. `policy-configuration.properties` — loaded via `@PropertySource("${signservice.integration.policy-configuration-resource}")` on `SignServiceIntegrationConfiguration`. Binds into a `Map<String, DefaultIntegrationServiceConfiguration>` under the `signservice.config` prefix, one entry per policy name.
4. `signservice-users.properties` — `signservice.user.<id>.{roles,password,policies}`, bound by `UsersConfiguration` into an in-memory `UserDetailsService`.

Because policies and users are property-driven, adding a policy or user is a properties change, not a code change. `docs/configuration.md` is the reference documentation for every supported property and should be updated alongside any new property.

Credentials (`signservice.credentials[n].*`) are built by `SignatureCredentialsConfiguration` using the `credentials-support` library. `NameToSigningCredentialConverter` is what lets a policy refer to a credential by its `name` string (`signservice.config.<policy>.signing-credential=TestMySignature`) — a Spring `Converter` resolving name → `PkiCredential`. `SignServiceIntegrationConfiguration` `@DependsOn` these beans plus `SignServiceInitializer` for ordering.

## Authorization

Two layers, both in `config/SecurityConfiguration.java` and `security/`:

- HTTP Basic + coarse role check in the filter chain: endpoints require `ROLE_USER` or `ROLE_ADMIN`; `/v1/version` and actuator endpoints are open. Sessions are stateless, CSRF disabled.
- Per-policy check via `@PreAuthorize("@evaluator.hasPermission(authentication, #policy, 'use')")` on controller methods. `PolicyPermissionEvaluator` / `AccessControlUtils` grant access if the user holds `ROLE_ADMIN` or the `POLICY_<name>` authority. Authorities are synthesized from the user properties file: roles become `ROLE_<UPPER>`, policies become `POLICY_<lower>`.

Any new policy-scoped endpoint needs both the filter-chain matcher and the `@PreAuthorize` — neither alone is sufficient.

## Caching

`CacheConfiguration` chooses between in-memory and Redis implementations based on `spring.redis.enabled` (note: this is a custom property, not the Spring Boot one; the actual connection settings use `spring.data.redis.*`). Two caches: signature state (`IntegrationServiceStateCache`) and document cache (`DocumentCache`), each with its own max-age and cleanup interval. `CacheCleanupService` runs the eviction sweeps.

Redis is what makes the service clusterable — with in-memory caches, the `create` and `process` calls of a signature flow must hit the same instance.

## Errors

`SignServiceIntegrationExceptionHandler` translates `SignServiceIntegrationException` and `SignResponseErrorStatusException` into the API's `SignServiceIntegrationErrorBody` (error code, validation errors, DSS status codes). `CustomErrorAttributes` shapes the fallback Spring error body to the same form. Clients depend on this shape — do not change field names casually.

## Deployment

Two paths: the `Dockerfile` (jar + `scripts/start.sh`, expects config mounted at `/etc/signservice`) and the jib-maven-plugin. `scripts/local-deploy.sh` brings up Redis plus the service in Docker for local testing. The JVM flags `-Dorg.apache.xml.security.ignoreLineBreaks=true` and `--add-opens java.base/java.lang=ALL-UNNAMED` are required — XML signature output and library reflection break without them.

## Conventions

- Lombok is used throughout (`@Slf4j`, `@Setter`, `@Getter`, builders).
- Every source file carries the Apache 2.0 header with the `2020-2026 IDsec Solutions AB` copyright line; copy it into new files.
- Javadoc on public/package types and beans is the norm here, including `package-info.java` per package.
- Two-space indent, `final` on parameters and locals.
- The version in `pom.xml` is hand-maintained and mirrored to clients via `/v1/version`; `docs/release-notes.md` is updated per release.
