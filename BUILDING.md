# RoboWled Build

This directory contains the Gradle build configuration for RoboWled.

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Publishing

### Local Maven Repository

Publish to `build/maven/` for testing:

```bash
./gradlew publishMavenPublicationToLocalRepository
```

### GitHub Packages

Publish to GitHub Packages (requires authentication):

```bash
export GITHUB_TOKEN=your_token
export GITHUB_ACTOR=your_username
./gradlew publishMavenPublicationToGitHubPackagesRepository
```

## Using RoboWled in Your Robot Project

Add the following to your robot project's `build.gradle`:

```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/Team9567/RoboWled')
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
    // Or use GitHub Pages Maven repo (no auth required)
    maven {
        url = uri('https://team9567.github.io/RoboWled/releases/')
    }
}

dependencies {
    implementation 'com.github.team9567:robowled:VERSION'
}
```

Replace `VERSION` with the desired version number.
