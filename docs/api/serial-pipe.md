# SerialPipe

Serial-based communication pipe for WLED devices. Communicates over USB serial, sending and receiving newline-delimited JSON.

Implements the [WledPipe](wled-pipe.html) interface.

## Package

```java
robowled.wledpipe.SerialPipe
```

## Constructor

### SerialPipe(SerialPort.Port usbPort, int baudRate)

Creates a SerialPipe connected to a WLED device via USB serial.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `usbPort` | `SerialPort.Port` | The WPILib serial port (e.g., `SerialPort.Port.kUSB`) |
| `baudRate` | `int` | The baud rate for serial communication (e.g., 115200) |

**Example:**

```java
import robowled.wledpipe.SerialPipe;
import edu.wpi.first.wpilibj.SerialPort;

// Create a serial connection on USB port at 115200 baud
SerialPipe wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
```

**Available Ports:**

| Port | Description |
|------|-------------|
| `SerialPort.Port.kUSB` | Primary USB port on roboRIO |
| `SerialPort.Port.kUSB1` | Secondary USB port |
| `SerialPort.Port.kUSB2` | Tertiary USB port |
| `SerialPort.Port.kMXP` | MXP expansion port serial |
| `SerialPort.Port.kOnboard` | Onboard RS-232 port |

---

## Methods

*All methods are defined by the [WledPipe](wled-pipe.html) interface.*

### sendObject(Object obj)

Sends an object as JSON over the serial port, followed by a newline.

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

Sends a raw string over the serial port.

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

## Complete Example

```java
import robowled.wledpipe.SerialPipe;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;

    public LedSubsystem() {
        wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }

    public void setPreset(int presetNumber) {
        try {
            wled.sendString("{\"ps\":" + presetNumber + "}\n");
        } catch (Exception e) {
            System.err.println("Failed to set preset: " + e.getMessage());
        }
    }

    public void setColor(int r, int g, int b) {
        try {
            String json = String.format(
                "{\"seg\":[{\"col\":[[%d,%d,%d]]}]}\n", r, g, b);
            wled.sendString(json);
        } catch (Exception e) {
            System.err.println("Failed to set color: " + e.getMessage());
        }
    }

    public void setBrightness(int brightness) {
        try {
            wled.sendString("{\"bri\":" + brightness + "}\n");
        } catch (Exception e) {
            System.err.println("Failed to set brightness: " + e.getMessage());
        }
    }

    @Override
    public void periodic() {
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
```

## See Also

- [WledPipe Interface](wled-pipe.html) - The interface SerialPipe implements
- [NetworkPipe](network-pipe.html) - For TCP/IP network connections
- [DummyPipe](dummy-pipe.html) - For testing and simulation
- [Getting Started](../getting-started.html) - Basic usage guide
- [WLED JSON API](https://kno.wled.ge/interfaces/json-api/) - Official WLED documentation

