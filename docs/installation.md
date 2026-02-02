# Installation

## Adding RoboWled to Your Robot Project

Add RoboWled as a Maven dependency in your robot project's `build.gradle` file.

### Step 1: Add the Repository

In your `build.gradle`, add the RoboWled Maven repository to the `repositories` block:

```groovy
repositories {
    mavenCentral()
    
    // Add RoboWled repository
    maven { url 'https://team9567.github.io/RoboWled/' }
}
```

### Step 2: Add the Dependency

In the `dependencies` block, add RoboWled:

```groovy
dependencies {
    // ... your other dependencies ...
    
    implementation 'com.github.team9567:robowled:VERSION'
}
```

Replace `VERSION` with the desired version number (e.g., `1.0.0`).

### Complete Example

Here's what your `build.gradle` might look like after adding RoboWled:

```groovy
plugins {
    id "java"
    id "edu.wpi.first.GradleRIO" version "2026.2.1"
}

repositories {
    mavenCentral()
    maven { url 'https://team9567.github.io/RoboWled/' }
}

dependencies {
    implementation wpi.java.deps.wpilib()
    implementation wpi.java.vendor.java()
    
    // RoboWled
    implementation 'com.github.team9567:robowled:1.0.0'
    
    // ... rest of your dependencies
}
```

### Alternative: GitHub Packages

You can also use GitHub Packages (requires authentication):

```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/Team9567/RoboWled')
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

## Hardware Requirements

### WLED Controller

You'll need a microcontroller running WLED firmware:

- **ESP8266** (NodeMCU, Wemos D1 Mini, etc.)
- **ESP32** (recommended for better performance)

Flash WLED using the [official web installer](https://install.wled.me/).

### Connection Options

RoboWled supports two connection methods:

| Method | Pros | Cons |
|--------|------|------|
| **USB Serial** | Simple wiring, reliable | Uses a USB port on roboRIO |
| **Network (Ethernet)** | No extra cables to roboRIO | Requires network configuration, most WLED controllers don't have ethernet |

### Wiring for Serial Connection

Connect your WLED controller to the roboRIO via USB:

```
WLED Controller (USB) → roboRIO USB Port
```

Most ESP8266/ESP32 development boards have a built-in USB-to-serial converter, so a standard micro-USB or USB-C cable is all you need.

### Network Connection

For network connections, your WLED controller connects to the robot's network:

```
WLED Controller → Ethernet → Robot Radio/Switch
```

Configure your WLED device with a static IP address or use mDNS for reliable connections.

## Verifying Installation

After installation, verify that RoboWled is available by adding this import to your robot code:

```java
import robowled.wledpipe.SerialPipe;
import robowled.wledpipe.NetworkPipe;
```

Run a Gradle build to ensure dependencies are resolved:

```bash
./gradlew build
```

If your project builds without errors, RoboWled is installed correctly!

## Next Steps

Now that RoboWled is installed, head over to [Getting Started](getting-started.md) to learn how to use it.
