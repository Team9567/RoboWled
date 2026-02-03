# WledPipe Interface

Common interface for all WLED communication pipes.

## Package

```java
robowled.wledpipe.WledPipe
```

## Overview

`WledPipe` is the base interface that all pipe implementations share. This allows you to write code that works with any transport mechanism (serial, network, or mock).

### Implementations

| Class | Description |
|-------|-------------|
| [SerialPipe](serial-pipe.html) | USB/serial communication |
| [NetworkPipe](network-pipe.html) | TCP/IP network communication |
| [DummyPipe](dummy-pipe.html) | Mock pipe for testing and simulation |

## Interface Methods

### sendObject(Object obj)

Sends an object as JSON, followed by a newline.

```java
void sendObject(Object obj) throws Exception;
```

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `obj` | `Object` | The object to serialize and send |

**Throws:** `Exception` if serialization or sending fails

---

### sendString(String str)

Sends a raw string.

```java
void sendString(String str) throws Exception;
```

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `str` | `String` | The string to send |

**Throws:** `Exception` if sending fails

---

### tryReadString()

Non-blocking read operation. Call periodically to check for incoming data.

```java
String tryReadString() throws Exception;
```

**Returns:** A complete line (without newline), or `null` if no complete line is available

**Throws:** `Exception` if reading fails

---

### tryReadObject(Class\<T> clazz)

Non-blocking read that deserializes a complete JSON line into the specified type.

```java
<T> T tryReadObject(Class<T> clazz) throws Exception;
```

**Type Parameters:**

| Parameter | Description |
|-----------|-------------|
| `T` | The type to return |

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `clazz` | `Class<T>` | The class to deserialize into |

**Returns:** A deserialized object, or `null` if no complete line is available

**Throws:** `Exception` if reading or deserialization fails

---

### isConnected()

Checks if the pipe is connected and ready for communication.

```java
boolean isConnected();
```

**Returns:** `true` if connected, `false` otherwise

---

### close()

Closes the pipe and releases any resources.

```java
void close();
```

---

## Using the Interface

The interface allows you to write flexible code that can work with different pipe implementations:

```java
public class LedSubsystem extends SubsystemBase {
    private final WledPipe wled;
    
    // Accept any pipe implementation
    public LedSubsystem(WledPipe wled) {
        this.wled = wled;
    }
    
    public void setPreset(int preset) {
        try {
            wled.sendString("{\"ps\":" + preset + "}\n");
        } catch (Exception e) {
            System.err.println("Failed to set preset: " + e.getMessage());
        }
    }
    
    public void setBrightness(int brightness) {
        try {
            wled.sendString("{\"bri\":" + brightness + "}\n");
        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
        }
    }
}
```

### Production Usage

```java
// With serial connection
LedSubsystem leds = new LedSubsystem(
    new SerialPipe(SerialPort.Port.kUSB, 115200)
);

// With network connection
LedSubsystem leds = new LedSubsystem(
    new NetworkPipe("wled-underglow.local", 21324)
);
```

### Simulation/Testing Usage

```java
// With dummy pipe for simulation
DummyPipe mockPipe = new DummyPipe();
LedSubsystem leds = new LedSubsystem(mockPipe);

// Verify commands were sent correctly
leds.setBrightness(200);
assertEquals(200, mockPipe.getStateValue("bri", Integer.class));
```

## See Also

- [SerialPipe](serial-pipe.html) - USB serial implementation
- [NetworkPipe](network-pipe.html) - Network implementation
- [DummyPipe](dummy-pipe.html) - Mock implementation for testing
- [Simulation & Testing Guide](../guides/simulation-testing.html)

