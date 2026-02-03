---
layout: default
title: Simulation & Testing
---

# Simulation & Testing

This guide covers how to use `DummyPipe` for testing your LED subsystems and running in simulation mode.

## Why Use DummyPipe?

When developing robot code, you often don't have access to the actual hardware:

- **At home** without a robot
- **In simulation mode** during development
- **Unit testing** LED logic
- **CI/CD pipelines** for automated testing

`DummyPipe` solves this by providing a mock implementation of `WledPipe` that:

1. Accumulates sent JSON into a state map you can inspect
2. Allows queuing mock responses
3. Simulates connection states
4. Optionally logs all sent messages

---

## Setting Up for Simulation

### Design Pattern: Interface-Based Subsystem

First, design your LED subsystem to accept a `WledPipe` interface:

```java
public class LedSubsystem extends SubsystemBase {
    private final WledPipe wled;
    
    public LedSubsystem(WledPipe wled) {
        this.wled = wled;
    }
    
    public void setPreset(int preset) {
        try {
            wled.sendString("{\"ps\":" + preset + "}\n");
        } catch (Exception e) {
            System.err.println("LED error: " + e.getMessage());
        }
    }
    
    public void setBrightness(int brightness) {
        try {
            wled.sendString("{\"bri\":" + brightness + "}\n");
        } catch (Exception e) {
            System.err.println("LED error: " + e.getMessage());
        }
    }
    
    public void setColor(int r, int g, int b) {
        try {
            String json = String.format(
                "{\"seg\":[{\"col\":[[%d,%d,%d]]}]}\n", r, g, b);
            wled.sendString(json);
        } catch (Exception e) {
            System.err.println("LED error: " + e.getMessage());
        }
    }
    
    public void turnOn() {
        try {
            wled.sendString("{\"on\":true}\n");
        } catch (Exception e) {
            System.err.println("LED error: " + e.getMessage());
        }
    }
    
    public void turnOff() {
        try {
            wled.sendString("{\"on\":false}\n");
        } catch (Exception e) {
            System.err.println("LED error: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return wled.isConnected();
    }
}
```

### Conditional Pipe Selection

In your `Robot.java` or `RobotContainer.java`, select the pipe based on the runtime mode:

```java
import edu.wpi.first.wpilibj.RobotBase;
import robowled.wledpipe.*;

public class RobotContainer {
    private final LedSubsystem leds;
    private DummyPipe simulationPipe;  // Keep reference for state inspection
    
    public RobotContainer() {
        WledPipe wledPipe;
        
        if (RobotBase.isSimulation()) {
            // Simulation mode - use DummyPipe with logging
            simulationPipe = new DummyPipe(msg -> 
                System.out.println("[WLED] " + msg.trim())
            );
            wledPipe = simulationPipe;
        } else {
            // Real robot - use actual hardware
            try {
                wledPipe = new SerialPipe(SerialPort.Port.kUSB, 115200);
            } catch (Exception e) {
                System.err.println("Failed to connect to WLED, using dummy");
                simulationPipe = new DummyPipe();
                wledPipe = simulationPipe;
            }
        }
        
        leds = new LedSubsystem(wledPipe);
    }
    
    // Access simulated state for dashboard
    public DummyPipe getSimulationPipe() {
        return simulationPipe;
    }
}
```

---

## LED State Tracking in Simulation

### Publishing State to Dashboard

Track LED state on your dashboard during simulation:

```java
public class Robot extends TimedRobot {
    private RobotContainer robotContainer;
    
    @Override
    public void robotInit() {
        robotContainer = new RobotContainer();
    }
    
    @Override
    public void simulationPeriodic() {
        DummyPipe simPipe = robotContainer.getSimulationPipe();
        if (simPipe != null) {
            Map<String, Object> state = simPipe.getAccumulatedState();
            
            // Publish LED state to SmartDashboard
            SmartDashboard.putBoolean("Sim/LED/On", 
                Boolean.TRUE.equals(state.get("on")));
            
            Object bri = state.get("bri");
            SmartDashboard.putNumber("Sim/LED/Brightness", 
                bri != null ? ((Number) bri).intValue() : 0);
            
            Object ps = state.get("ps");
            SmartDashboard.putNumber("Sim/LED/Preset", 
                ps != null ? ((Number) ps).intValue() : -1);
        }
    }
}
```

### Glass/Shuffleboard Visualization

You can create a visual representation of your LEDs using the published values:

1. Add boolean indicator for On/Off
2. Add number display for Brightness (0-255)
3. Add number display for active Preset

---

## Unit Testing

### Basic Test Setup

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LedSubsystemTest {
    private DummyPipe pipe;
    private LedSubsystem leds;
    
    @BeforeEach
    void setUp() {
        pipe = new DummyPipe();
        leds = new LedSubsystem(pipe);
    }
    
    @Test
    void testTurnOn() {
        leds.turnOn();
        assertTrue(pipe.getStateValue("on", Boolean.class));
    }
    
    @Test
    void testTurnOff() {
        leds.turnOff();
        assertFalse(pipe.getStateValue("on", Boolean.class));
    }
    
    @Test
    void testSetBrightness() {
        leds.setBrightness(128);
        assertEquals(128, pipe.getStateValue("bri", Integer.class));
    }
    
    @Test
    void testSetPreset() {
        leds.setPreset(5);
        assertEquals(5, pipe.getStateValue("ps", Integer.class));
    }
}
```

### Testing Command Sequences

```java
@Test
void testAllianceColorSequence() {
    // Simulate enabling with red alliance
    leds.turnOn();
    leds.setBrightness(255);
    leds.setColor(255, 0, 0);  // Red
    
    Map<String, Object> state = pipe.getAccumulatedState();
    
    assertTrue((Boolean) state.get("on"));
    assertEquals(255, state.get("bri"));
    assertNotNull(state.get("seg"));  // Color was set
}

@Test
void testStateAccumulation() {
    // Multiple commands should accumulate
    leds.turnOn();
    leds.setBrightness(100);
    leds.setPreset(1);
    leds.setBrightness(200);  // Update brightness
    
    // Final state should reflect all changes
    Map<String, Object> state = pipe.getAccumulatedState();
    assertTrue((Boolean) state.get("on"));
    assertEquals(200, state.get("bri"));  // Updated value
    assertEquals(1, state.get("ps"));
}
```

### Testing with Message Capture

```java
@Test
void testMessageFormat() {
    List<String> sentMessages = new ArrayList<>();
    pipe.setSendCallback(sentMessages::add);
    
    leds.setPreset(3);
    
    assertEquals(1, sentMessages.size());
    assertTrue(sentMessages.get(0).contains("\"ps\":3"));
}
```

### Testing Error Handling

```java
@Test
void testDisconnectionHandling() {
    pipe.setConnected(false);
    
    assertFalse(leds.isConnected());
    
    // Commands should not throw even when disconnected
    assertDoesNotThrow(() -> leds.setPreset(1));
}
```

---

## Advanced: Simulating WLED Responses

If your code reads responses from WLED, you can queue mock responses:

```java
@Test
void testReadingState() throws Exception {
    // Queue a mock response
    pipe.queueResponse("{\"on\":true,\"bri\":128,\"ps\":2}");
    
    // Your code that reads state
    String response = pipe.tryReadString();
    assertNotNull(response);
    assertTrue(response.contains("\"on\":true"));
}

@Test
void testDeserializingResponse() throws Exception {
    // Queue a typed response
    Map<String, Object> mockState = new HashMap<>();
    mockState.put("on", true);
    mockState.put("bri", 200);
    pipe.queueResponseObject(mockState);
    
    // Deserialize response
    WledState state = pipe.tryReadObject(WledState.class);
    assertTrue(state.on);
    assertEquals(200, state.bri);
}

// Response class
static class WledState {
    public boolean on;
    public int bri;
}
```

---

## Best Practices

### 1. Always Use the Interface

Design subsystems to depend on `WledPipe`, not concrete implementations:

```java
// Good - flexible
public LedSubsystem(WledPipe wled)

// Bad - hard to test
public LedSubsystem() {
    this.wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
}
```

### 2. Reset State Between Tests

```java
@BeforeEach
void setUp() {
    pipe = new DummyPipe();  // Fresh pipe for each test
    leds = new LedSubsystem(pipe);
}

// Or clear state explicitly
@AfterEach
void tearDown() {
    pipe.clearState();
    pipe.clearResponses();
}
```

### 3. Test Edge Cases

```java
@Test
void testBrightnessLimits() {
    leds.setBrightness(0);
    assertEquals(0, pipe.getStateValue("bri", Integer.class));
    
    leds.setBrightness(255);
    assertEquals(255, pipe.getStateValue("bri", Integer.class));
}
```

### 4. Use Descriptive Assertions

```java
@Test
void testPresetActivation() {
    leds.setPreset(5);
    
    assertEquals(5, pipe.getStateValue("ps", Integer.class),
        "Preset 5 should be active after setPreset(5)");
}
```

---

## See Also

- [DummyPipe API Reference](../api/dummy-pipe.html)
- [WledPipe Interface](../api/wled-pipe.html)
- [Getting Started](../getting-started.html)

