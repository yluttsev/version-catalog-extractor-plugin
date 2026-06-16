<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Version Catalog Extractor Changelog

## [Unreleased]

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
