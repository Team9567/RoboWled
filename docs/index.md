---
layout: default
title: Home
---

# RoboWled Documentation

> A WPILib-compatible library for controlling WLED LED strips on FRC robots via serial or network connections.

## Quick Links

- [Installation](installation.html)
- [Getting Started](getting-started.html)

## API Reference

- [WledPipe Interface](api/wled-pipe.html)
- [SerialPipe](api/serial-pipe.html)
- [NetworkPipe](api/network-pipe.html)
- [DummyPipe](api/dummy-pipe.html)

## Guides

- [Connecting to WLED](guides/connecting-to-wled.html)
- [Sending Commands](guides/sending-commands.html)
- [Triggering Patterns](guides/triggering-patterns.html)
- [Simulation & Testing](guides/simulation-testing.html)

## Maven Repository

The Maven artifacts are available at [`/releases/`](releases/).

Add to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://team9567.github.io/RoboWled/releases/' }
}

dependencies {
    implementation 'com.github.team9567:robowled:VERSION'
}
```

