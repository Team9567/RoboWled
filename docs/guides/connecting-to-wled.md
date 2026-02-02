# Connecting to WLED

This guide covers all the ways to connect your FRC robot to a WLED controller.

## Connection Methods Overview

| Method | Best For | Latency | Complexity |
|--------|----------|---------|------------|
| USB Serial | Simple setups, single controller | ~5-10ms | Low |
| Network TCP | Multiple controllers, longer distances | ~10-20ms | Medium |

## USB Serial Connection

### Hardware Setup

1. **Connect the WLED controller to the roboRIO via USB**
   - Use a quality USB cable (micro-USB or USB-C depending on your ESP board)
   - Connect to one of the roboRIO's USB ports

2. **Note the port**
   - The first USB device is typically `kUSB`
   - Additional devices use `kUSB1`, `kUSB2`

### Code Setup

```java
import robowled.wledpipe.SerialPipe;
import edu.wpi.first.wpilibj.SerialPort;

public class LEDSubsystem extends SubsystemBase {
    private SerialPipe wled;

    public LEDSubsystem() {
        try {
            wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
        } catch (Exception e) {
            System.err.println("Failed to initialize WLED: " + e.getMessage());
        }
    }
}
```

### Baud Rate

WLED typically uses **115200** baud for serial communication. If you're having issues, verify this matches your WLED configuration:

1. Open the WLED web interface
2. Go to **Config** → **Sync Interfaces**
3. Check the **Serial** section for baud rate settings

### Troubleshooting Serial

| Issue | Solution |
|-------|----------|
| No response from WLED | Check USB cable and port assignment |
| Garbled data | Verify baud rate matches WLED config |
| Intermittent connection | Try a different USB cable |
| Port not found | Check if another device is using the port |

---

## Network Connection

### Hardware Setup

**Wired Ethernet**
```
WLED Controller → Ethernet → Robot Radio/Switch
```

A PoE-enabled adapter will likely provide the best results.

Consult the [WLED project Ethernet (LAN)](https://kno.wled.ge/features/ethernet-lan/) documentation for details on
supported ethernet adapters/interfaces and other related concerns.

### Addressing Options

You have two options for addressing your WLED device on the network:

| Method | Pros | Cons |
|--------|------|------|
| **Static IP** | Predictable, fast connection | Must configure IP on device, potential conflicts |
| **mDNS Hostname** | No IP configuration needed, survives network changes | Slightly slower initial resolution |

### Using mDNS (Recommended)

mDNS (multicast DNS) allows you to connect to WLED devices using a hostname like `wled-xxxxxx.local` instead of an IP address. This is often simpler than managing static IPs.

**Finding your WLED's mDNS hostname:**

1. Open the WLED web interface
2. Go to **Config** → **Ethernet**
3. Look for the **mDNS address** field (e.g., `wled-a1b2c3`)
4. Your full hostname is that value plus `.local` (e.g., `wled-a1b2c3.local`)

You can also customize the mDNS name in the WLED settings to something memorable like `wled-front` or `wled-underglow`.

**Code using mDNS:**

```java
import robowled.wledpipe.NetworkPipe;
import java.io.IOException;

public class LEDSubsystem extends SubsystemBase {
    private NetworkPipe wled;
    // Use mDNS hostname instead of IP address
    private static final String WLED_HOST = "wled-underglow.local";
    private static final int WLED_PORT = 21324;

    public LEDSubsystem() {
        try {
            wled = new NetworkPipe(WLED_HOST, WLED_PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect to WLED: " + e.getMessage());
        }
    }
}
```

**mDNS considerations for FRC:**

- mDNS resolution may take slightly longer on first connection (typically < 1 second)
- The roboRIO supports mDNS resolution out of the box
- mDNS works across the robot radio network
- If mDNS fails, you can fall back to a static IP

### Using Static IP

If you prefer static IPs or mDNS isn't working reliably, configure your WLED with a static IP:

1. Open the WLED web interface
2. Go to **Config** → **Ethernet**
3. Set a static IP in your team's subnet (e.g., `10.TE.AM.100`)
4. Set gateway to `10.TE.AM.1` and subnet mask to `255.255.255.0`

### Code Setup (Static IP)

```java
import robowled.wledpipe.NetworkPipe;
import java.io.IOException;

public class LEDSubsystem extends SubsystemBase {
    private NetworkPipe wled;
    private static final String WLED_IP = "10.95.67.100";
    private static final int WLED_PORT = 21324;

    public LEDSubsystem() {
        try {
            wled = new NetworkPipe(WLED_IP, WLED_PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect to WLED: " + e.getMessage());
        }
    }
}
```

### Troubleshooting Network

| Issue | Solution |
|-------|----------|
| Connection refused | Check IP address/hostname and port number |
| Connection timeout | Verify WLED is on the same network |
| Intermittent disconnects | Check for IP conflicts, ensure stable ethernet connection |
| mDNS hostname not resolving | Verify hostname is correct, check WLED mDNS settings |
| Slow initial connection with mDNS | Normal on first connect; consider caching resolved IP |

---

## Handling Connection Errors

Both connection types can fail. Here's a robust pattern for handling errors:

```java
public class LEDSubsystem extends SubsystemBase {
    private SerialPipe serialWled;
    private NetworkPipe networkWled;
    private boolean connected = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;

    public LEDSubsystem() {
        connect();
    }

    private void connect() {
        // Try serial first
        try {
            serialWled = new SerialPipe(SerialPort.Port.kUSB, 115200);
            connected = true;
            System.out.println("Connected to WLED via serial");
            return;
        } catch (Exception e) {
            System.out.println("Serial connection failed, trying network...");
        }

        // Fall back to network
        try {
            networkWled = new NetworkPipe("10.95.67.100", 21324);
            connected = true;
            System.out.println("Connected to WLED via network");
        } catch (IOException e) {
            System.err.println("All WLED connections failed");
            connected = false;
        }
    }

    public void sendCommand(String json) {
        if (!connected) {
            attemptReconnect();
            return;
        }

        try {
            if (serialWled != null) {
                serialWled.sendString(json);
            } else if (networkWled != null) {
                networkWled.sendString(json);
            }
        } catch (Exception e) {
            connected = false;
            System.err.println("WLED send failed: " + e.getMessage());
        }
    }

    private void attemptReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            connect();
        }
    }

    @Override
    public void periodic() {
        // Reset reconnect counter periodically
        if (connected) {
            reconnectAttempts = 0;
        }
    }
}
```

---

## Multiple WLED Controllers

You can connect to multiple WLED controllers simultaneously:

```java
public class MultiLEDSubsystem extends SubsystemBase {
    private final NetworkPipe frontLeds;
    private final NetworkPipe rearLeds;
    private final SerialPipe statusLeds;

    public MultiLEDSubsystem() throws IOException {
        frontLeds = new NetworkPipe("10.95.67.101", 21324);
        rearLeds = new NetworkPipe("10.95.67.102", 21324);
        statusLeds = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }

    public void setAllColor(int r, int g, int b) {
        String json = String.format("{\"seg\":[{\"col\":[[%d,%d,%d]]}]}\n", r, g, b);
        try {
            frontLeds.sendString(json);
            rearLeds.sendString(json);
            statusLeds.sendString(json);
        } catch (Exception e) {
            System.err.println("Failed to set color: " + e.getMessage());
        }
    }
}
```

---

## Best Practices

1. **Use mDNS hostnames** for simpler configuration, or static IPs if you need guaranteed fast connections
2. **Give WLED devices memorable mDNS names** like `wled-front.local` or `wled-status.local`
3. **Handle exceptions gracefully** - LED failures shouldn't crash your robot
4. **Test connections during robot init** to catch problems early
5. **Log connection status** to help with debugging at competitions

## Next Steps

- [Sending Commands](sending-commands.md) - Learn the WLED command format
- [Triggering Patterns](triggering-patterns.md) - Link patterns to robot events

