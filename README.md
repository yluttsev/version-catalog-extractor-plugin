# Version Catalog Extractor

An IntelliJ IDEA plugin that detects literal dependency declarations in Gradle build files and extracts them into a Gradle Version Catalog (`libs.versions.toml`).

## What it does

The plugin scans Gradle build files for literal dependencies and reports the dependency notation as an inspection warning:

```kotlin
// Kotlin DSL
implementation("org.example:lib:1.2.3")
implementation("org.springframework.boot:spring-boot-starter-web")
```

```groovy
// Groovy DSL
implementation 'org.example:lib:1.2.3'
implementation 'org.springframework.boot:spring-boot-starter-web'
```

When found, it highlights the dependency notation with a warning and offers a quick fix via **Alt+Enter**.

## Example

Before:

```kotlin
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
}
```

After applying the quick fix:

```kotlin
dependencies {
    implementation(libs.retrofit)
}
```

`gradle/libs.versions.toml`:

```toml
[versions]
retrofit = "2.11.0"

[libraries]
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
```

## Quick fix behavior

Applying the fix triggers the following steps:

1. Locates `gradle/libs.versions.toml` in the project root. Creates the file if it does not exist.
2. Checks whether a library entry for the same `group:name` already exists in the catalog. If it does, reuses the existing alias instead of creating a duplicate.
3. If no entry exists, generates an alias from the dependency coordinates, adds a new entry to `[libraries]`, and creates a corresponding entry in `[versions]` only when the dependency declares a version.
4. Replaces the original dependency declaration with a catalog reference:

```kotlin
implementation(libs.some.library)
```

5. Triggers a Gradle project refresh so the IDE picks up the updated catalog.

Existing catalog entries are detected in both common TOML forms:

```toml
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
```

```toml
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
```

## Supported configurations

The inspection currently checks dependencies declared through these common Gradle configurations:

```text
implementation, api, compileOnly, runtimeOnly,
testImplementation, testCompileOnly, testRuntimeOnly,
kapt, ksp, annotationProcessor,
debugImplementation, releaseImplementation
```

## What is not flagged

- Dependencies already using a catalog alias: `implementation(libs.retrofit)`
- Dependencies with interpolated versions: `implementation("org.example:lib:$version")`
- Dependencies without a literal `group:name` or `group:name:version` notation

## Requirements

- IntelliJ IDEA 2025.1+
- Gradle project with Kotlin DSL or Groovy DSL

## Running the plugin locally

Use the **Run Plugin** run configuration. It launches a sandboxed IntelliJ IDEA instance with the plugin installed.

```
./gradlew runIde
```

## Running tests

```
./gradlew test
```
