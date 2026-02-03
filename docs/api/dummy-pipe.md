# DummyPipe

A mock pipe for testing and simulation. Implements the [WledPipe](wled-pipe.md) interface without communicating with any real device.

## Package

```java
robowled.wledpipe.DummyPipe
```

## Overview

`DummyPipe` is designed for:

- **Unit testing** LED subsystems without hardware
- **Simulation mode** when running robot code on a desktop
- **Verifying command sequences** by inspecting accumulated state
- **Debugging** LED logic without a physical WLED device

### Key Features

| Feature | Description |
|---------|-------------|
| **State Accumulation** | Sent JSON messages are parsed and merged into an internal state map |
| **Mock Responses** | Queue fake responses for testing receive logic |
| **Send Callback** | Optional notification when messages are sent |
| **Connection Simulation** | Simulate connected/disconnected states |

---

## Constructors

### DummyPipe()

Creates a DummyPipe with no send callback.

```java
DummyPipe pipe = new DummyPipe();
```

### DummyPipe(Consumer\<String> sendCallback)

Creates a DummyPipe with a callback for sent messages.

```java
DummyPipe pipe = new DummyPipe(msg -> System.out.println("Sent: " + msg));
```

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `sendCallback` | `Consumer<String>` | Called with each string sent, or `null` to skip |

---

## State Accumulation

When you send JSON messages, DummyPipe parses them and merges the values into an accumulated state map. This allows you to verify what state the WLED device would be in after a sequence of commands.

### getAccumulatedState()

Returns the full accumulated state map.

```java
Map<String, Object> state = pipe.getAccumulatedState();
```

**Example:**

```java
DummyPipe pipe = new DummyPipe();

pipe.sendString("{\"on\":true}\n");
pipe.sendString("{\"bri\":255}\n");
pipe.sendString("{\"ps\":3}\n");

Map<String, Object> state = pipe.getAccumulatedState();
// state = {"on": true, "bri": 255, "ps": 3}
```

### getStateValue(String key)

Gets a specific value from the accumulated state.

```java
Object value = pipe.getStateValue("bri");  // Returns Integer 255
```

### getStateValue(String key, Class\<T> type)

Gets a value with type casting.

```java
Boolean isOn = pipe.getStateValue("on", Boolean.class);      // true
Integer brightness = pipe.getStateValue("bri", Integer.class); // 255
Integer preset = pipe.getStateValue("ps", Integer.class);      // 3
```

### clearState()

Clears all accumulated state.

```java
pipe.clearState();
```

---

## Mock Responses

Queue fake responses to test code that reads from WLED.

### queueResponse(String response)

Queues a string response (returned by `tryReadString()` in FIFO order).

```java
pipe.queueResponse("{\"on\":true,\"bri\":128}");

String response = pipe.tryReadString();  // Returns the queued response
```

### queueResponseObject(Object obj)

Queues a response by serializing an object to JSON.

```java
Map<String, Object> mockState = Map.of("on", true, "bri", 200);
pipe.queueResponseObject(mockState);
```

### clearResponses()

Clears all queued responses.

```java
pipe.clearResponses();
```

---

## Connection Simulation

### setConnected(boolean connected)

Simulates the connection state.

```java
pipe.setConnected(false);  // Simulate disconnection
assertFalse(pipe.isConnected());

pipe.setConnected(true);   // Simulate reconnection
assertTrue(pipe.isConnected());
```

---

## Additional Methods

### setSendCallback(Consumer\<String> callback)

Changes the send callback after construction.

```java
List<String> sentMessages = new ArrayList<>();
pipe.setSendCallback(sentMessages::add);

pipe.sendString("{\"on\":true}\n");
// sentMessages now contains the sent message
```

---

## Complete Example: Unit Testing

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LedSubsystemTest {
    
    @Test
    void testSetPreset() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        leds.setPreset(5);
        
        assertEquals(5, pipe.getStateValue("ps", Integer.class));
    }
    
    @Test
    void testSetColor() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        leds.setColor(255, 0, 0);  // Red
        
        // Verify the segment color was set
        Map<String, Object> state = pipe.getAccumulatedState();
        assertNotNull(state.get("seg"));
    }
    
    @Test
    void testMultipleCommands() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        leds.turnOn();
        leds.setBrightness(200);
        leds.setPreset(3);
        
        // All values should be accumulated
        assertTrue(pipe.getStateValue("on", Boolean.class));
        assertEquals(200, pipe.getStateValue("bri", Integer.class));
        assertEquals(3, pipe.getStateValue("ps", Integer.class));
    }
    
    @Test
    void testDisconnectionHandling() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        pipe.setConnected(false);
        
        // Your subsystem should handle disconnection gracefully
        assertFalse(leds.isConnected());
    }
}
```

## Complete Example: Simulation with Logging

```java
public class Robot extends TimedRobot {
    private LedSubsystem leds;
    private DummyPipe simulationPipe;
    
    @Override
    public void robotInit() {
        if (RobotBase.isSimulation()) {
            // Use DummyPipe in simulation
            simulationPipe = new DummyPipe(msg -> 
                System.out.println("[WLED SIM] " + msg.trim())
            );
            leds = new LedSubsystem(simulationPipe);
        } else {
            // Use real hardware
            leds = new LedSubsystem(
                new SerialPipe(SerialPort.Port.kUSB, 115200)
            );
        }
    }
    
    @Override
    public void simulationPeriodic() {
        // Log current simulated LED state
        if (simulationPipe != null) {
            Map<String, Object> state = simulationPipe.getAccumulatedState();
            SmartDashboard.putBoolean("LED/On", 
                Boolean.TRUE.equals(state.get("on")));
            SmartDashboard.putNumber("LED/Brightness", 
                state.get("bri") != null ? (Integer) state.get("bri") : 0);
        }
    }
}
```

---

## See Also

- [WledPipe Interface](wled-pipe.md) - The interface DummyPipe implements
- [Simulation & Testing Guide](../guides/simulation-testing.md) - Detailed guide
- [SerialPipe](serial-pipe.md) - For real serial connections
- [NetworkPipe](network-pipe.md) - For real network connections

