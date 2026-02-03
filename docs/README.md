# RoboWled

> A WPILib-compatible library for controlling WLED LED strips on FRC robots via serial or network connections.

## What is RoboWled?

**RoboWled** provides a robust, flexible interface for controlling [WLED](https://kno.wled.ge/)-powered LED strips from your FRC robot code. Whether you're adding underglow effects, status indicators, or game-piece detection feedback, RoboWled makes it easy to integrate addressable LEDs into your robot.

## Key Features

- **Dual Communication Channels**: Supports both serial (USB) and network (TCP/IP) connections to WLED devices
- **Simple Preset Activation**: Easily trigger on-device saved presets
- **Raw JSON Control**: Push raw JSON configurations directly to WLED
- **Type-Safe Java API**: Build logical representations of settings in pure Java with automatic JSON serialization
- **Bidirectional Communication**: Read state back from WLED devices

## Quick Example

```java
import robowled.wledpipe.SerialPipe;
import edu.wpi.first.wpilibj.SerialPort;

// Connect via USB serial
SerialPipe wled = new SerialPipe(SerialPort.Port.kUSB, 115200);

// Send a command to activate preset 1
wled.sendString("{\"ps\":1}\n");
```

## Why WLED?

[WLED](https://kno.wled.ge/) is a fast and feature-rich firmware for controlling addressable LEDs. It runs on inexpensive ESP8266/ESP32 microcontrollers and provides:

- Support for over 100 special effects
- Customizable palettes and segments
- Real-time control via multiple protocols
- Easy setup via web interface
- Low latency response times

## Getting Started

Ready to add some lights to your robot? Head over to the [Installation](installation.html) guide to get started!

## License

RoboWled is licensed under the [GNU Lesser General Public License v3.0 (LGPL-3.0)](https://www.gnu.org/licenses/lgpl-3.0.html). You are free to use, modify, and redistribute the software in your FRC projects.

