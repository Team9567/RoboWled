# Installation

## WPILib Vendordep Installation

The recommended way to install RoboWled is as a WPILib vendor dependency.

### Using VS Code with WPILib Extension

1. Open your robot project in VS Code
2. Open the Command Palette (`Ctrl+Shift+P` / `Cmd+Shift+P`)
3. Select: **WPILib: Manage Vendor Libraries**
4. Choose **Install new library (online)**
5. Paste the following URL:

```
https://team9567.github.io/RoboWled/RoboWled.json
```

6. Press Enter to install

### Manual Installation

If you prefer to install manually, download the vendordep JSON file and place it in your project's `vendordeps/` folder:

```bash
cd your-robot-project/vendordeps/
curl -O https://team9567.github.io/RoboWled/RoboWled.json
```

Then run a Gradle sync to download the dependencies.

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
| **Network (Ethernet)** | No extra cables to roboRIO | Requires network configuration, most wled controllers don't have ethernet |

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

Configure your WLED device with a static IP address for reliable connections.

## Verifying Installation

After installation, verify that RoboWled is available by adding this import to your robot code:

```java
import robowled.wledpipe.SerialPipe;
import robowled.wledpipe.NetworkPipe;
```

If your project builds without errors, RoboWled is installed correctly!

## Next Steps

Now that RoboWled is installed, head over to [Getting Started](getting-started.md) to learn how to use it.

