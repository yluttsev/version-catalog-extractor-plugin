# Version Catalog Extractor

An IntelliJ IDEA plugin that detects literal dependency and plugin declarations in Gradle build files and extracts them into a Gradle Version Catalog (`libs.versions.toml`).

## What it does

The plugin scans Gradle build files for literal dependency declarations and versioned Kotlin DSL plugin declarations. When it finds a supported declaration, it reports an inspection warning and offers a quick fix via **Alt+Enter**.

Supported dependency examples:

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

Supported plugin example:

```kotlin
plugins {
    id("org.springframework.boot") version "3.5.0"
}
```

For dependencies, the plugin highlights the dependency notation. For plugins, it highlights the plugin declaration.

## Dependency example

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

Versionless dependencies are added without a version entry:

```toml
[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
```

## Plugin example

Before:

```kotlin
plugins {
    id("org.springframework.boot") version "3.5.0"
}
```

After applying the quick fix:

```kotlin
plugins {
    alias(libs.plugins.spring.boot)
}
```

`gradle/libs.versions.toml`:

```toml
[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }

[versions]
spring-boot = "3.5.0"
```

## Dependency quick fix behavior

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

## Plugin quick fix behavior

Applying the plugin fix triggers the following steps:

1. Locates `gradle/libs.versions.toml` in the project root. Creates the file if it does not exist.
2. Checks whether a plugin entry for the same plugin id already exists in the catalog. If it does, reuses the existing alias instead of creating a duplicate.
3. If no entry exists, generates an alias from the plugin id, adds a new entry to `[plugins]`, and creates a corresponding entry in `[versions]`.
4. Replaces the original plugin declaration with a catalog plugin alias.
5. Triggers a Gradle project refresh so the IDE picks up the updated catalog.

## Supported configurations

The inspection currently checks dependencies declared through these common Gradle configurations:

```text
implementation, api, compileOnly, runtimeOnly,
testImplementation, testCompileOnly, testRuntimeOnly,
kapt, ksp, annotationProcessor,
debugImplementation, releaseImplementation
```

## Supported plugin declarations

The plugin currently checks versioned Kotlin DSL plugin declarations using `id(...)`:

```kotlin
id("org.springframework.boot") version "3.5.0"
id("com.google.devtools.ksp") version "2.1.0-1.0.29"
```

## What is not flagged

- Dependencies already using a catalog alias: `implementation(libs.retrofit)`
- Dependencies with interpolated versions: `implementation("org.example:lib:$version")`
- Dependencies without a literal `group:name` or `group:name:version` notation
- Wrapped dependencies, such as `implementation(platform("org.junit:junit-bom:5.10.0"))`
- Plugin aliases already using a catalog alias: `alias(libs.plugins.spring.boot)`
- Core Gradle plugins: `id("java")`, `id("application")`, `id("java-library")`
- Kotlin plugin shorthand: `kotlin("jvm") version "2.1.0"`
- Plugin declarations without an explicit version
- Plugin declarations outside a Kotlin DSL `plugins {}` block
- Groovy DSL plugin declarations

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
