---
layout: default
title: Triggering Patterns
---

# Triggering Patterns

This guide shows how to link LED patterns to robot events, game states, and sensor inputs.

## Pattern Strategy

Before writing code, plan your LED patterns:

| Robot State | LED Pattern | Purpose |
|-------------|-------------|---------|
| Disabled | Dim alliance color | Show alliance, save power |
| Enabled (idle) | Solid alliance color | Ready indicator |
| Auto mode | Breathing effect | Distinguish from teleop |
| Has game piece | Green flash | Driver feedback |
| Shooting | Rainbow burst | Visual excitement |
| Low battery | Red blink | Warning |
| Error state | Fast red strobe | Alert pit crew |

---

## Basic State Machine

Create a simple state machine for LED control:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;
    
    public enum LedState {
        DISABLED,
        IDLE,
        AUTO,
        HAS_PIECE,
        SHOOTING,
        ERROR
    }
    
    private LedState currentState = LedState.DISABLED;
    private LedState lastState = null;

    public LedSubsystem() {
        wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }

    public void setState(LedState state) {
        currentState = state;
    }

    @Override
    public void periodic() {
        // Only send commands when state changes
        if (currentState != lastState) {
            applyState(currentState);
            lastState = currentState;
        }
    }

    private void applyState(LedState state) {
        try {
            switch (state) {
                case DISABLED:
                    wled.sendString("{\"on\":true,\"bri\":50,\"ps\":1}\n");
                    break;
                case IDLE:
                    wled.sendString("{\"on\":true,\"bri\":200,\"ps\":2}\n");
                    break;
                case AUTO:
                    wled.sendString("{\"on\":true,\"bri\":255,\"ps\":3}\n");
                    break;
                case HAS_PIECE:
                    wled.sendString("{\"on\":true,\"bri\":255,\"ps\":4}\n");
                    break;
                case SHOOTING:
                    wled.sendString("{\"on\":true,\"bri\":255,\"ps\":5}\n");
                    break;
                case ERROR:
                    wled.sendString("{\"on\":true,\"bri\":255,\"ps\":10}\n");
                    break;
            }
        } catch (Exception e) {
            System.err.println("LED state change failed: " + e.getMessage());
        }
    }
}
```

---

## Using WPILib Triggers

Integrate with the command-based framework using Triggers:

```java
public class RobotContainer {
    private final LedSubsystem leds = new LedSubsystem();
    private final IntakeSubsystem intake = new IntakeSubsystem();
    private final ShooterSubsystem shooter = new ShooterSubsystem();

    public RobotContainer() {
        configureLedTriggers();
    }

    private void configureLedTriggers() {
        // Disabled state
        new Trigger(DriverStation::isDisabled)
            .onTrue(new InstantCommand(() -> leds.setState(LedState.DISABLED)));

        // Enabled but idle
        new Trigger(DriverStation::isEnabled)
            .and(() -> !intake.hasPiece())
            .and(() -> !shooter.isShooting())
            .onTrue(new InstantCommand(() -> leds.setState(LedState.IDLE)));

        // Auto mode
        new Trigger(DriverStation::isAutonomousEnabled)
            .onTrue(new InstantCommand(() -> leds.setState(LedState.AUTO)));

        // Has game piece
        new Trigger(intake::hasPiece)
            .onTrue(new InstantCommand(() -> leds.setState(LedState.HAS_PIECE)))
            .onFalse(new InstantCommand(() -> leds.setState(LedState.IDLE)));

        // Shooting
        new Trigger(shooter::isShooting)
            .onTrue(new InstantCommand(() -> leds.setState(LedState.SHOOTING)))
            .onFalse(new InstantCommand(() -> leds.setState(LedState.IDLE)));
    }
}
```

---

## Alliance-Based Colors

Automatically set colors based on alliance:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;
    private DriverStation.Alliance lastAlliance = null;

    public void updateAllianceColor() {
        var alliance = DriverStation.getAlliance();
        if (alliance.isEmpty()) return;
        
        if (alliance.get() != lastAlliance) {
            lastAlliance = alliance.get();
            try {
                if (lastAlliance == DriverStation.Alliance.Red) {
                    wled.sendString("{\"seg\":[{\"col\":[[255,0,0]]}]}\n");
                } else {
                    wled.sendString("{\"seg\":[{\"col\":[[0,0,255]]}]}\n");
                }
            } catch (Exception e) {
                System.err.println("Alliance color failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void periodic() {
        updateAllianceColor();
    }
}
```

---

## Match Timer Integration

Change patterns based on match time:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;
    private boolean endgameWarned = false;
    private boolean finalCountdown = false;

    @Override
    public void periodic() {
        if (DriverStation.isTeleopEnabled()) {
            double matchTime = DriverStation.getMatchTime();
            
            // Endgame warning at 30 seconds
            if (matchTime <= 30 && matchTime > 10 && !endgameWarned) {
                endgameWarned = true;
                setEndgamePattern();
            }
            
            // Final countdown at 10 seconds
            if (matchTime <= 10 && !finalCountdown) {
                finalCountdown = true;
                setFinalCountdownPattern();
            }
        } else {
            // Reset flags when not in teleop
            endgameWarned = false;
            finalCountdown = false;
        }
    }

    private void setEndgamePattern() {
        try {
            // Yellow breathing effect
            wled.sendString("{\"seg\":[{\"col\":[[255,200,0]],\"fx\":2,\"sx\":150}]}\n");
        } catch (Exception e) {
            System.err.println("Endgame pattern failed: " + e.getMessage());
        }
    }

    private void setFinalCountdownPattern() {
        try {
            // Fast rainbow
            wled.sendString("{\"seg\":[{\"fx\":9,\"sx\":255}]}\n");
        } catch (Exception e) {
            System.err.println("Countdown pattern failed: " + e.getMessage());
        }
    }
}
```

---

## Sensor-Based Patterns

React to sensor inputs:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;
    private final DigitalInput beamBreak;
    private boolean lastBeamState = false;

    public LedSubsystem() {
        wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
        beamBreak = new DigitalInput(0);
    }

    @Override
    public void periodic() {
        boolean beamBroken = !beamBreak.get(); // Typically active-low
        
        if (beamBroken != lastBeamState) {
            lastBeamState = beamBroken;
            if (beamBroken) {
                // Game piece detected!
                flashGreen();
            } else {
                // Game piece released
                returnToIdle();
            }
        }
    }

    private void flashGreen() {
        try {
            wled.sendString("{\"seg\":[{\"col\":[[0,255,0]],\"fx\":1,\"sx\":200}]}\n");
        } catch (Exception e) {}
    }

    private void returnToIdle() {
        try {
            wled.sendString("{\"seg\":[{\"fx\":0}],\"ps\":1}\n");
        } catch (Exception e) {}
    }
}
```

---

## Command-Based Patterns

Create commands for LED patterns:

```java
public class FlashColorCommand extends Command {
    private final LedSubsystem leds;
    private final int r, g, b;
    private final double duration;
    private final Timer timer = new Timer();

    public FlashColorCommand(LedSubsystem leds, int r, int g, int b, double seconds) {
        this.leds = leds;
        this.r = r;
        this.g = g;
        this.b = b;
        this.duration = seconds;
        addRequirements(leds);
    }

    @Override
    public void initialize() {
        timer.reset();
        timer.start();
        leds.setColorWithBlink(r, g, b);
    }

    @Override
    public boolean isFinished() {
        return timer.hasElapsed(duration);
    }

    @Override
    public void end(boolean interrupted) {
        leds.returnToDefault();
    }
}

// Usage in command compositions:
public Command shootWithFeedback() {
    return Commands.sequence(
        new FlashColorCommand(leds, 255, 255, 0, 0.5), // Yellow flash
        shootCommand,
        new FlashColorCommand(leds, 0, 255, 0, 0.3)   // Green success
    );
}
```

---

## Priority System

Handle competing LED requests with priorities:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;
    
    public enum Priority {
        LOW,      // Background patterns
        NORMAL,   // Game piece status
        HIGH,     // Shooting, climbing
        CRITICAL  // Errors, warnings
    }

    private Priority currentPriority = Priority.LOW;
    private String currentPattern = "";

    public void requestPattern(String pattern, Priority priority) {
        if (priority.ordinal() >= currentPriority.ordinal()) {
            currentPriority = priority;
            currentPattern = pattern;
            applyPattern();
        }
    }

    public void releasePattern(Priority priority) {
        if (priority == currentPriority) {
            currentPriority = Priority.LOW;
            applyDefaultPattern();
        }
    }

    private void applyPattern() {
        try {
            wled.sendString(currentPattern + "\n");
        } catch (Exception e) {}
    }

    private void applyDefaultPattern() {
        try {
            wled.sendString("{\"ps\":1}\n");
        } catch (Exception e) {}
    }
}

// Usage:
leds.requestPattern("{\"seg\":[{\"col\":[[255,0,0]],\"fx\":1}]}", Priority.CRITICAL);
// Later...
leds.releasePattern(Priority.CRITICAL);
```

---

## Performance Tips

1. **Debounce rapid changes** - Don't send commands faster than 20-30Hz
2. **Use state tracking** - Only send commands when the pattern actually changes
3. **Batch related changes** - Combine color, brightness, and effect in one command
4. **Use presets** - Pre-configure complex patterns in WLED, activate by number
5. **Async error handling** - Don't let LED failures slow down your main loop

```java
// Debounced pattern updates
private long lastUpdateTime = 0;
private static final long MIN_UPDATE_INTERVAL_MS = 50;

public void requestPattern(String pattern) {
    long now = System.currentTimeMillis();
    if (now - lastUpdateTime >= MIN_UPDATE_INTERVAL_MS) {
        lastUpdateTime = now;
        try {
            wled.sendString(pattern + "\n");
        } catch (Exception e) {}
    }
}
```

---

## Complete Example

Here's a full LED subsystem with all features:

```java
public class LedSubsystem extends SubsystemBase {
    private final SerialPipe wled;
    private LedState currentState = LedState.DISABLED;
    private LedState lastState = null;
    private DriverStation.Alliance currentAlliance = null;

    public enum LedState {
        DISABLED(1, 50),
        IDLE(2, 200),
        AUTO(3, 255),
        HAS_PIECE(4, 255),
        SHOOTING(5, 255),
        CLIMBING(6, 255),
        ERROR(10, 255);

        final int preset;
        final int brightness;

        LedState(int preset, int brightness) {
            this.preset = preset;
            this.brightness = brightness;
        }
    }

    public LedSubsystem() {
        wled = new SerialPipe(SerialPort.Port.kUSB, 115200);
    }

    public void setState(LedState state) {
        currentState = state;
    }

    @Override
    public void periodic() {
        updateAlliance();
        
        if (currentState != lastState) {
            applyState();
            lastState = currentState;
        }
    }

    private void updateAlliance() {
        var alliance = DriverStation.getAlliance();
        if (alliance.isPresent() && alliance.get() != currentAlliance) {
            currentAlliance = alliance.get();
            // Presets 1-6 should be configured for current alliance
            // Or send alliance color directly
        }
    }

    private void applyState() {
        try {
            String cmd = String.format("{\"on\":true,\"bri\":%d,\"ps\":%d}",
                currentState.brightness, currentState.preset);
            wled.sendString(cmd + "\n");
        } catch (Exception e) {
            System.err.println("LED update failed: " + e.getMessage());
        }
    }

    public Command flashCommand(int r, int g, int b, double seconds) {
        return Commands.sequence(
            Commands.runOnce(() -> setColor(r, g, b)),
            Commands.waitSeconds(seconds),
            Commands.runOnce(() -> applyState())
        );
    }

    private void setColor(int r, int g, int b) {
        try {
            wled.sendString(String.format(
                "{\"seg\":[{\"col\":[[%d,%d,%d]]}]}\n", r, g, b));
        } catch (Exception e) {}
    }
}
```

## Next Steps

- [SerialPipe API](../api/serial-pipe.html) - Full API reference
- [NetworkPipe API](../api/network-pipe.html) - Network connection details
- [WLED Effects](https://kno.wled.ge/features/effects/) - Browse all available effects

