# Getting Started

This guide will walk you through creating your first WLED-controlled lighting system on your FRC robot.

## Prerequisites

Before you begin, make sure you have:

1. [Installed RoboWled](installation.html) in your robot project
2. A WLED controller connected to LEDs and configured
3. Either a USB cable or network connection between the roboRIO and WLED

## Creating a Connection

### Option 1: Serial Connection (USB)

For USB serial connections, create a `SerialPipe`:

```java
import robowled.wledpipe.SerialPipe;
import edu.wpi.first.wpilibj.SerialPort;

public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;

    public LedSubsystem() {
        // Connect to WLED on USB port at 115200 baud
        wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }
}
```

Available serial ports:
- `SerialPort.Port.kUSB` - Primary USB port
- `SerialPort.Port.kUSB1` - Secondary USB port
- `SerialPort.Port.kUSB2` - Tertiary USB port
- `SerialPort.Port.kMXP` - MXP serial port

### Option 2: Network Connection (TCP/IP)

For network connections, create a `NetworkPipe`. You can connect using either an mDNS hostname or a static IP address:

```java
import robowled.wledpipe.NetworkPipe;

public class LedSubsystem extends SubsystemBase {
    private NetworkPipe wled;

    public LedSubsystem() {
        try {
            // Option A: Connect using mDNS hostname (recommended)
            wled = new NetworkPipe("wled-underglow.local", 21324);
            
            // Option B: Connect using static IP address
            // wled = new NetworkPipe("10.TE.AM.100", 21324);
        } catch (IOException e) {
            System.err.println("Failed to connect to WLED: " + e.getMessage());
        }
    }
}
```

**Using mDNS (recommended):** Find your WLED's mDNS hostname in **Config** → **Ethernet** on the WLED web interface. The hostname looks like `wled-xxxxxx.local`. You can customize this name in WLED settings.

**Using static IP:** Replace `10.TE.AM.100` with your WLED controller's configured IP address.

## Sending Commands

### Activating Presets

The simplest way to control WLED is by activating saved presets. First, create and save presets using the WLED web interface, then activate them from your robot code:

```java
// Activate preset 1
wled.sendString("{\"ps\":1}\n");

// Activate preset 2
wled.sendString("{\"ps\":2}\n");
```

### Turning LEDs On/Off

```java
// Turn LEDs on
wled.sendString("{\"on\":true}\n");

// Turn LEDs off
wled.sendString("{\"on\":false}\n");
```

### Setting Brightness

```java
// Set brightness to 50% (0-255 range)
wled.sendString("{\"bri\":128}\n");

// Set brightness to maximum
wled.sendString("{\"bri\":255}\n");
```

### Setting a Solid Color

```java
// Set all LEDs to red (RGB values)
wled.sendString("{\"seg\":[{\"col\":[[255,0,0]]}]}\n");

// Set all LEDs to green
wled.sendString("{\"seg\":[{\"col\":[[0,255,0]]}]}\n");

// Set all LEDs to blue
wled.sendString("{\"seg\":[{\"col\":[[0,0,255]]}]}\n");
```

## Recommended: Interface-Based Design

For flexibility and testability, design your Led subsystem to accept a `WledPipe` interface instead of a specific implementation:

```java
import robowled.wledpipe.WledPipe;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class LedSubsystem extends SubsystemBase {
    private final WledPipe wled;

    // Accept any pipe implementation
    public LedSubsystem(WledPipe wled) {
        this.wled = wled;
    }
    
    // ... your LED methods
}
```

This allows you to:
- Use `SerialPipe` or `NetworkPipe` for real hardware
- Use `DummyPipe` for simulation and testing

See [Simulation & Testing](guides/simulation-testing.html) for details.

## Example: Alliance-Based Colors

Here's a complete example that changes LED colors based on the robot's alliance:

```java
import robowled.wledpipe.WledPipe;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class LedSubsystem extends SubsystemBase {
    private final WledPipe wled;

    public LedSubsystem(WledPipe wled) {
        this.wled = wled;
    }

    public void setAllianceColor() {
        try {
            var alliance = DriverStation.getAlliance();
            if (alliance.isPresent()) {
                if (alliance.get() == DriverStation.Alliance.Red) {
                    // Red alliance - set LEDs to red
                    wled.sendString("{\"seg\":[{\"col\":[[255,0,0]]}]}\n");
                } else {
                    // Blue alliance - set LEDs to blue
                    wled.sendString("{\"seg\":[{\"col\":[[0,0,255]]}]}\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to set alliance color: " + e.getMessage());
        }
    }

    public void setEnabled() {
        try {
            wled.sendString("{\"on\":true,\"bri\":255}\n");
        } catch (Exception e) {
            System.err.println("Failed to enable LEDs: " + e.getMessage());
        }
    }

    public void setDisabled() {
        try {
            wled.sendString("{\"on\":true,\"bri\":50}\n");
        } catch (Exception e) {
            System.err.println("Failed to dim LEDs: " + e.getMessage());
        }
    }
}
```

## Using with Commands

Integrate WLED control with the WPILib command framework:

```java
// In RobotContainer.java
public RobotContainer() {
    // Create the appropriate pipe based on runtime mode
    WledPipe wledPipe;
    if (RobotBase.isSimulation()) {
        wledPipe = new DummyPipe(msg -> System.out.println("[WLED] " + msg));
    } else {
        wledPipe = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }
    
    LedSubsystem leds = new LedSubsystem(wledPipe);

    // Set alliance color when robot is enabled
    new Trigger(DriverStation::isEnabled)
        .onTrue(new InstantCommand(leds::setAllianceColor, leds));

    // Dim LEDs when disabled
    new Trigger(DriverStation::isDisabled)
        .onTrue(new InstantCommand(leds::setDisabled, leds));
}
```

## Next Steps

- Learn about the [WledPipe Interface](api/wled-pipe.html)
- Learn more about the [SerialPipe API](api/serial-pipe.html)
- Learn more about the [NetworkPipe API](api/network-pipe.html)
- Use [DummyPipe](api/dummy-pipe.html) for simulation and testing
- Explore [Sending Commands](guides/sending-commands.html) for advanced WLED control
- Check out [Triggering Patterns](guides/triggering-patterns.html) for game-state integration
- Learn about [Simulation & Testing](guides/simulation-testing.html)

