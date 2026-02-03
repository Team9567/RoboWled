---
layout: default
title: Sending Commands
---

# Sending Commands

This guide covers the WLED JSON API and how to send various commands from your robot code.

## WLED JSON API Basics

WLED uses a JSON-based API for control. Commands are sent as JSON objects with specific keys:

```json
{"key": value, "key2": value2}
```

RoboWled sends these as newline-terminated strings:

```java
wled.sendString("{\"on\":true}\n");
```

---

## Common Commands

### Power Control

```java
// Turn on
wled.sendString("{\"on\":true}\n");

// Turn off
wled.sendString("{\"on\":false}\n");

// Toggle (use current state inverted)
wled.sendString("{\"on\":\"t\"}\n");
```

### Brightness

Brightness ranges from 0 (off) to 255 (maximum):

```java
// Set to 50% brightness
wled.sendString("{\"bri\":128}\n");

// Set to maximum
wled.sendString("{\"bri\":255}\n");

// Set to minimum (still visible)
wled.sendString("{\"bri\":1}\n");
```

### Presets

Presets are saved configurations in WLED. Create them via the web interface, then activate by number:

```java
// Activate preset 1
wled.sendString("{\"ps\":1}\n");

// Activate preset 5
wled.sendString("{\"ps\":5}\n");

// Save current state to preset 10
wled.sendString("{\"psave\":10}\n");
```

### Solid Colors

Set a solid color using RGB values (0-255 each):

```java
// Red
wled.sendString("{\"seg\":[{\"col\":[[255,0,0]]}]}\n");

// Green
wled.sendString("{\"seg\":[{\"col\":[[0,255,0]]}]}\n");

// Blue
wled.sendString("{\"seg\":[{\"col\":[[0,0,255]]}]}\n");

// White
wled.sendString("{\"seg\":[{\"col\":[[255,255,255]]}]}\n");

// Purple
wled.sendString("{\"seg\":[{\"col\":[[128,0,255]]}]}\n");
```

### Effects

WLED has over 100 built-in effects. Set them by ID:

```java
// Solid (no effect)
wled.sendString("{\"seg\":[{\"fx\":0}]}\n");

// Blink
wled.sendString("{\"seg\":[{\"fx\":1}]}\n");

// Breathe
wled.sendString("{\"seg\":[{\"fx\":2}]}\n");

// Rainbow
wled.sendString("{\"seg\":[{\"fx\":9}]}\n");

// Fire
wled.sendString("{\"seg\":[{\"fx\":66}]}\n");
```

**Popular Effect IDs:**

| ID | Effect |
|----|--------|
| 0 | Solid |
| 1 | Blink |
| 2 | Breathe |
| 9 | Rainbow |
| 11 | Chase |
| 28 | Colorloop |
| 38 | Fire Flicker |
| 66 | Fire 2012 |
| 74 | Twinkle |
| 108 | Meteor |

See the full list at [WLED Effects List](https://kno.wled.ge/features/effects/).

### Effect Speed and Intensity

```java
// Set effect speed (0-255, 128 is default)
wled.sendString("{\"seg\":[{\"sx\":200}]}\n");

// Set effect intensity (0-255, 128 is default)
wled.sendString("{\"seg\":[{\"ix\":150}]}\n");

// Set both
wled.sendString("{\"seg\":[{\"sx\":200,\"ix\":150}]}\n");
```

### Palettes

WLED includes color palettes that effects can use:

```java
// Set palette by ID
wled.sendString("{\"seg\":[{\"pal\":5}]}\n");
```

**Popular Palette IDs:**

| ID | Palette |
|----|---------|
| 0 | Default |
| 1 | Random Cycle |
| 2 | Color 1 |
| 5 | Ocean |
| 6 | Forest |
| 8 | Rainbow |
| 35 | Party |
| 50 | C9 (Christmas) |

---

## Combining Commands

You can combine multiple settings in a single command:

```java
// Turn on, set brightness, and activate preset
wled.sendString("{\"on\":true,\"bri\":200,\"ps\":1}\n");

// Set color, effect, and speed
wled.sendString("{\"seg\":[{\"col\":[[255,0,0]],\"fx\":2,\"sx\":100}]}\n");
```

---

## Helper Methods

Create helper methods for common operations:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;

    public LedSubsystem() {
        wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }

    public void setPreset(int preset) {
        send("{\"ps\":" + preset + "}");
    }

    public void setBrightness(int brightness) {
        send("{\"bri\":" + Math.min(255, Math.max(0, brightness)) + "}");
    }

    public void setColor(int r, int g, int b) {
        send(String.format("{\"seg\":[{\"col\":[[%d,%d,%d]]}]}", r, g, b));
    }

    public void setEffect(int effectId) {
        send("{\"seg\":[{\"fx\":" + effectId + "}]}");
    }

    public void setEffect(int effectId, int speed, int intensity) {
        send(String.format("{\"seg\":[{\"fx\":%d,\"sx\":%d,\"ix\":%d}]}",
            effectId, speed, intensity));
    }

    public void turnOn() {
        send("{\"on\":true}");
    }

    public void turnOff() {
        send("{\"on\":false}");
    }

    private void send(String json) {
        try {
            wled.sendString(json + "\n");
        } catch (Exception e) {
            System.err.println("WLED send failed: " + e.getMessage());
        }
    }
}
```

---

## Using Objects Instead of Strings

For complex commands, use Java objects with Jackson serialization:

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public void setColorWithObject(int r, int g, int b) {
    try {
        Map<String, Object> segment = new HashMap<>();
        segment.put("col", List.of(List.of(r, g, b)));

        Map<String, Object> command = new HashMap<>();
        command.put("seg", List.of(segment));

        wled.sendObject(command);
    } catch (Exception e) {
        System.err.println("Failed to send: " + e.getMessage());
    }
}
```

---

## Segments

WLED supports dividing your LED strip into segments with independent control:

```java
// Set segment 0 to red, segment 1 to blue
String json = "{\"seg\":[" +
    "{\"id\":0,\"col\":[[255,0,0]]}," +
    "{\"id\":1,\"col\":[[0,0,255]]}" +
"]}\n";
wled.sendString(json);
```

### Creating Segments

```java
// Define segment 0: LEDs 0-29, segment 1: LEDs 30-59
String json = "{\"seg\":[" +
    "{\"id\":0,\"start\":0,\"stop\":30}," +
    "{\"id\":1,\"start\":30,\"stop\":60}" +
"]}\n";
wled.sendString(json);
```

---

## Transitions

Control how quickly changes take effect:

```java
// Instant change (0 = no transition)
wled.sendString("{\"transition\":0,\"bri\":255}\n");

// 1 second fade (transition is in 100ms units)
wled.sendString("{\"transition\":10,\"bri\":128}\n");

// 2.5 second fade
wled.sendString("{\"transition\":25,\"seg\":[{\"col\":[[0,255,0]]}]}\n");
```

---

## Reading State

Query WLED's current state:

```java
// Request state (WLED will respond with full state JSON)
wled.sendString("{\"v\":true}\n");

// Read response in periodic()
@Override
public void periodic() {
    try {
        String response = wled.tryReadString();
        if (response != null) {
            // Parse the JSON response
            System.out.println("WLED State: " + response);
        }
    } catch (Exception e) {
        // Handle error
    }
}
```

---

## Best Practices

1. **Don't spam commands** - WLED can handle ~30-60 updates per second, but sending faster wastes bandwidth
2. **Use presets for complex patterns** - Create them in the web UI, activate by number
3. **Handle exceptions** - Network/serial issues shouldn't crash your robot
4. **Use transitions for smooth changes** - Instant changes can look jarring
5. **Test on the bench** - Verify commands work before competition

## Next Steps

- [Triggering Patterns](triggering-patterns.html) - Link LED changes to robot events
- [WLED JSON API Docs](https://kno.wled.ge/interfaces/json-api/) - Full API reference

