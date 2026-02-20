---
layout: default
title: DummyPipe
---

# DummyPipe

A mock pipe for testing and simulation. Implements the [WledPipe](wled-pipe.html) interface without communicating with any real device.

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
| **State Accumulation** | Sent JSON messages are parsed and merged into an accumulated JsonObject |
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

When you send JSON messages, DummyPipe parses them and merges the values into an accumulated `JsonObject`. This allows you to verify what state the WLED device would be in after a sequence of commands.

**Note:** Non-JSON strings (or invalid JSON) are silently ignored for accumulation purposes, but the send callback is still invoked.

### getAccumulatedState()

Returns the full accumulated state as a JsonObject.

```java
JsonObject state = pipe.getAccumulatedState();
```

**Example:**

```java
DummyPipe pipe = new DummyPipe();

pipe.sendString("{\"on\":true}\n");
pipe.sendString("{\"bri\":255}\n");
pipe.sendString("{\"ps\":3}\n");

JsonObject state = pipe.getAccumulatedState();
// state contains: {"on":true,"bri":255,"ps":3}

assertTrue(state.get("on").getAsBoolean());
assertEquals(255, state.get("bri").getAsInt());
assertEquals(3, state.get("ps").getAsInt());
```

### getStateValue(String key)

Gets a specific value from the accumulated state.

```java
JsonElement value = pipe.getStateValue("bri");  // Returns JsonPrimitive(255)
int brightness = value.getAsInt();              // 255
```

### hasStateValue(String key)

Checks if a key exists in the accumulated state.

```java
if (pipe.hasStateValue("on")) {
    boolean isOn = pipe.getStateValue("on").getAsBoolean();
}
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

### queueResponse(JsonElement json)

Queues a JsonElement response.

```java
JsonObject mockState = new JsonObject();
mockState.addProperty("on", true);
mockState.addProperty("bri", 200);
pipe.queueResponse(mockState);

JsonElement response = pipe.tryReadGson();  // Returns the queued JsonObject
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
List<String> captured = new ArrayList<>();
pipe.setSendCallback(captured::add);

pipe.sendString("{\"on\":true}\n");
// captured now contains the sent message
```

---

## Inherited Methods

DummyPipe implements all methods from the [WledPipe](wled-pipe.html) interface:

| Method | DummyPipe Behavior |
|--------|-------------------|
| `sendGson(JsonElement)` | Converts to string and calls `sendString()` |
| `sendString(String)` | Invokes callback, then parses and merges JSON into accumulated state |
| `tryReadString()` | Returns next queued mock response, or `null` if queue is empty |
| `tryReadGson()` | Parses next queued response as JSON, or returns `null` |
| `isConnected()` | Returns simulated connection state (default: `true`) |
| `close()` | Sets connected to `false`, clears responses and accumulated state |

---

## Complete Example: Unit Testing

```java
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LedSubsystemTest {
    
    @Test
    void testSetPreset() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        leds.setPreset(5);
        
        assertEquals(5, pipe.getStateValue("ps").getAsInt());
    }
    
    @Test
    void testMultipleCommands() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        leds.turnOn();
        leds.setBrightness(200);
        leds.setPreset(3);
        
        // All values should be accumulated
        JsonObject state = pipe.getAccumulatedState();
        assertTrue(state.get("on").getAsBoolean());
        assertEquals(200, state.get("bri").getAsInt());
        assertEquals(3, state.get("ps").getAsInt());
    }
    
    @Test
    void testStateOverwrite() {
        DummyPipe pipe = new DummyPipe();
        LedSubsystem leds = new LedSubsystem(pipe);
        
        leds.setBrightness(100);
        leds.setBrightness(200);  // Overwrites previous value
        
        assertEquals(200, pipe.getStateValue("bri").getAsInt());
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

## Complete Example: Simulation with Dashboard

```java
public class Robot extends TimedRobot {
    private LedSubsystem leds;
    private DummyPipe simulationPipe;
    
    @Override
    public void robotInit() {
        if (RobotBase.isSimulation()) {
            // Use DummyPipe in simulation with logging
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
        // Publish simulated LED state to dashboard
        if (simulationPipe != null) {
            JsonObject state = simulationPipe.getAccumulatedState();
            
            if (state.has("on")) {
                SmartDashboard.putBoolean("LED/On", state.get("on").getAsBoolean());
            }
            if (state.has("bri")) {
                SmartDashboard.putNumber("LED/Brightness", state.get("bri").getAsInt());
            }
            if (state.has("ps")) {
                SmartDashboard.putNumber("LED/Preset", state.get("ps").getAsInt());
            }
        }
    }
}
```

---

## See Also

- [WledPipe Interface](wled-pipe.html) - The interface DummyPipe implements
- [Simulation & Testing Guide](../guides/simulation-testing.html) - Detailed guide
- [SerialPipe](serial-pipe.html) - For real serial connections
- [NetworkPipe](network-pipe.html) - For real network connections

