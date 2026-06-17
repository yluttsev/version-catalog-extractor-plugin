<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Version Catalog Extractor Changelog

## [Unreleased]

## [1.1.0] - 2026-06-17

### Added
- Support for extracting versionless library dependencies, such as `implementation("org.springframework.boot:spring-boot-starter-web")`
- Whole dependency notation highlighting instead of highlighting only the version segment
- Version catalog library entries without `version.ref` when the original dependency does not declare a version
- Kotlin DSL plugin inspection for versioned plugin declarations, such as `id("org.springframework.boot") version "3.5.0"`
- Quick fix to extract versioned Kotlin DSL plugins into `[plugins]` aliases and matching `[versions]` entries
- Reuse of existing version catalog plugin aliases when the same plugin id is already present
- Filtering for Gradle core plugins so declarations like `id("java")`, `id("application")`, and `id("java-library")` are not reported

## [1.0.0] - 2026-06-16

### Added
- Inspection for hardcoded dependency versions in Kotlin DSL (`.gradle.kts`) and Groovy DSL (`.gradle`) files
- Quick fix to extract hardcoded versions into `gradle/libs.versions.toml`
- Automatic creation of `libs.versions.toml` if it does not exist
- Reuse of existing catalog aliases when the same `group:name` is already present
- Alias generation from dependency coordinates with conflict resolution
- Replacement of the original dependency declaration with a version catalog reference
- Gradle project refresh after applying the quick fix
- Support for common Gradle configurations: `implementation`, `api`, `compileOnly`, `runtimeOnly`, `testImplementation`, `testCompileOnly`, `testRuntimeOnly`, `kapt`, `ksp`, `annotationProcessor`, `debugImplementation`, `releaseImplementation`
