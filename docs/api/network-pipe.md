---
layout: default
title: NetworkPipe
---

# NetworkPipe

Network-based communication pipe for WLED devices. Communicates over TCP, sending and receiving newline-delimited JSON.

Implements the [WledPipe](wled-pipe.html) interface.

## Package

```java
robowled.wledpipe.NetworkPipe
```

## Constructors

### NetworkPipe(String host, int port)

Creates a NetworkPipe connected to a WLED device with the default timeout (20ms).

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `host` | `String` | The hostname (including mDNS `.local` names) or IP address of the WLED device |
| `port` | `int` | The TCP port number |

**Throws:** `IOException` if the connection fails

**Examples:**

```java
import robowled.wledpipe.NetworkPipe;

// Connect using mDNS hostname (recommended)
NetworkPipe wled = new NetworkPipe("wled-underglow.local", 21324);

// Connect using static IP address
NetworkPipe wled = new NetworkPipe("10.95.67.100", 21324);
```

---

### NetworkPipe(String host, int port, int timeoutMs)

Creates a NetworkPipe with a custom read timeout.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `host` | `String` | The hostname or IP address of the WLED device |
| `port` | `int` | The TCP port number |
| `timeoutMs` | `int` | Socket read timeout in milliseconds |

**Throws:** `IOException` if the connection fails

**Example:**

```java
// Connect with a 50ms timeout
NetworkPipe wled = new NetworkPipe("10.95.67.100", 21324, 50);
```

---

## Methods

### sendObject(Object obj)

Sends an object as JSON over the network, followed by a newline.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `obj` | `Object` | The object to serialize and send |

**Throws:** `Exception` if serialization or sending fails

**Example:**

```java
// Using a Map to build JSON
Map<String, Object> command = new HashMap<>();
command.put("on", true);
command.put("bri", 255);

wled.sendObject(command);
// Sends: {"on":true,"bri":255}\n
```

---

### sendString(String str)

Sends a raw string over the network.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `str` | `String` | The string to send |

**Throws:** `Exception` if sending fails

**Example:**

```java
// Send a raw JSON command
wled.sendString("{\"ps\":1}\n");

// Activate an effect
wled.sendString("{\"seg\":[{\"fx\":42}]}\n");
```

---

### tryReadString()

Non-blocking read operation. Call periodically to check for incoming data. Returns a complete line when available.

**Returns:** `String` - A complete line (without newline), or `null` if no complete line is available

**Throws:** `Exception` if reading fails

**Example:**

```java
// In a periodic method
@Override
public void periodic() {
    try {
        String response = wled.tryReadString();
        if (response != null) {
            System.out.println("WLED response: " + response);
        }
    } catch (Exception e) {
        // Handle error
    }
}
```

---

### tryReadObject(Class\<T> clazz)

Non-blocking read that deserializes a complete JSON line into the specified type.

**Type Parameters:**

| Parameter | Description |
|-----------|-------------|
| `T` | The type to return |

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `clazz` | `Class<T>` | The class to deserialize into |

**Returns:** `T` - A deserialized object, or `null` if no complete line is available

**Throws:** `Exception` if reading or deserialization fails

**Example:**

```java
// Define a response class
public class WledState {
    public boolean on;
    public int bri;
}

// Read and deserialize
WledState state = wled.tryReadObject(WledState.class);
if (state != null) {
    System.out.println("WLED is " + (state.on ? "on" : "off"));
    System.out.println("Brightness: " + state.bri);
}
```

---

### isConnected()

Checks if the socket is currently connected.

**Returns:** `boolean` - `true` if connected, `false` otherwise

**Example:**

```java
if (wled.isConnected()) {
    wled.sendString("{\"ps\":1}\n");
} else {
    System.err.println("WLED disconnected!");
}
```

---

### close()

Closes the network connection and releases resources. Should be called when the pipe is no longer needed.

**Example:**

```java
// Clean up when done
wled.close();
```

---

## Network Configuration

### WLED Ports

WLED uses different ports for different protocols:

| Port | Protocol | Description |
|------|----------|-------------|
| 80 | HTTP | Web interface and REST API |
| 21324 | UDP | Real-time LED data (WARLS, DRGB, etc.) |
| 19446 | UDP | Hyperion protocol |

For TCP JSON communication similar to serial, you may need to configure a custom port or use the HTTP API.

### mDNS Hostnames (Recommended)

mDNS allows you to connect using a hostname like `wled-xxxxxx.local` instead of an IP address. This simplifies configuration and survives network changes.

**Finding your WLED's mDNS hostname:**

1. Open the WLED web interface
2. Go to **Config** → **WiFi Setup**
3. Find the **mDNS address** field
4. Append `.local` to get the full hostname (e.g., `wled-a1b2c3.local`)

**Using mDNS:**

```java
// Connect using mDNS - no IP configuration needed
NetworkPipe wled = new NetworkPipe("wled-underglow.local", 21324);
```

You can customize the mDNS name in WLED settings to something memorable like `wled-front` or `wled-rear`.

### Static IP Configuration

If you prefer static IPs or need faster initial connections:

1. Connect to WLED's web interface
2. Go to **Config** → **Ethernet**
3. Set a static IP in your team's subnet (e.g., `10.TE.AM.100`)
4. Set gateway to `10.TE.AM.1`

### FRC Network Considerations

On FRC robots, network traffic between the roboRIO and other devices goes through the robot radio. Ensure your WLED device:

- Is connected to the robot's network (via ethernet)
- Has a known mDNS hostname or static IP in the `10.TE.AM.x` range
- Is not using ports reserved by FRC (e.g., 1735 for NetworkTables)

---

## Complete Example

```java
import robowled.wledpipe.NetworkPipe;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.io.IOException;

public class LedSubsystem extends SubsystemBase {
    private NetworkPipe wled;
    private boolean connected = false;
    
    // Use mDNS hostname for easy configuration
    private static final String WLED_HOST = "wled-underglow.local";
    private static final int WLED_PORT = 21324;

    public LedSubsystem() {
        connect();
    }

    private void connect() {
        try {
            wled = new NetworkPipe(WLED_HOST, WLED_PORT);
            connected = true;
            System.out.println("Connected to WLED at " + WLED_HOST);
        } catch (IOException e) {
            connected = false;
            System.err.println("Failed to connect to WLED: " + e.getMessage());
        }
    }

    public void setPreset(int presetNumber) {
        if (!connected) return;
        try {
            wled.sendString("{\"ps\":" + presetNumber + "}\n");
        } catch (Exception e) {
            System.err.println("Failed to set preset: " + e.getMessage());
            connected = false;
        }
    }

    public void setColor(int r, int g, int b) {
        if (!connected) return;
        try {
            String json = String.format(
                "{\"seg\":[{\"col\":[[%d,%d,%d]]}]}\n", r, g, b);
            wled.sendString(json);
        } catch (Exception e) {
            System.err.println("Failed to set color: " + e.getMessage());
            connected = false;
        }
    }

    public void setBrightness(int brightness) {
        if (!connected) return;
        try {
            wled.sendString("{\"bri\":" + brightness + "}\n");
        } catch (Exception e) {
            System.err.println("Failed to set brightness: " + e.getMessage());
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected && wled != null && wled.isConnected();
    }

    @Override
    public void periodic() {
        // Attempt reconnection if disconnected
        if (!connected) {
            connect();
        }

        if (connected) {
            try {
                String response = wled.tryReadString();
                if (response != null) {
                    // Process any responses from WLED
                }
            } catch (Exception e) {
                // Silently ignore read errors in periodic
            }
        }
    }

    public void close() {
        if (wled != null) {
            wled.close();
            connected = false;
        }
    }
}
```

## See Also

- [WledPipe Interface](wled-pipe.html) - The interface NetworkPipe implements
- [SerialPipe](serial-pipe.html) - For USB serial connections
- [DummyPipe](dummy-pipe.html) - For testing and simulation
- [Getting Started](../getting-started.html) - Basic usage guide
- [WLED JSON API](https://kno.wled.ge/interfaces/json-api/) - Official WLED documentation

