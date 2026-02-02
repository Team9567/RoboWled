# RoboWled - Robot WLED control
[![Documentation](https://github.com/Team9567/RoboWled/actions/workflows/build-pdf.yml/badge.svg)](https://github.com/Team9567/RoboWled/actions/workflows/build-pdf.yml) [![CI](https://github.com/Team9567/RoboWled/actions/workflows/ci.yml/badge.svg)](https://github.com/Team9567/RoboWled/actions/workflows/ci.yml) [![Release](https://github.com/Team9567/RoboWled/actions/workflows/release.yml/badge.svg)](https://github.com/Team9567/RoboWled/actions/workflows/release.yml)

> ✨ A WPILib-compatible library for controlling WLED LED strips on FRC robots via serial or network connections.

**RoboWled** is a WPILib-compatible library that provides a robust, flexible interface for WLED-controlled LED strips on FRC robots.

---

## 📖 Documentation

[RoboWled Gitbook](https://yagsl.gitbook.io/RoboWled/documentation)

---

## 🔧 Key Features

- Supports both serial (e.g. via USB-serial) and networked (e.g. via ethernet) channels for control
- Simple activation of on-device saved presets
- Pushing raw json configs
- Building logical representations of settings in pure java, and serializing it into Json
- Deserializing Json into java objects for programmatic manipulation of settings

---

## 📦 Installation (WPILib Vendordep)

1. In **VS Code** with WPILib extension:
   - Open Command Palette (`Ctrl+Shift+P` / `Cmd+Shift+P`)
   - Select: `WPILib: Manage Vendor Libraries`
   - Choose `Install new library (online or offline)`
   - Select **Online**
   - Paste the URL to the RoboWled vendordep JSON file, e.g.:
     `https://team9567.github.io/RoboWled/RoboWled.json`
   - Press Enter to install

---


## 📂 Examples

The repository contains several example projects under the `/examples` folder demonstrating how to use RoboWled for showing patterns, and linking pattern changes to robot triggers.

These example projects do **not** include RoboWled as a dependency via Maven or vendordep directly. Instead, they use a modified `build.gradle` that links the RoboWled source code located in the `/robowled` folder relative to the example.

This is done by adding the following snippet to the `sourceSets` block in each example’s `build.gradle`:

```groovy
sourceSets {
    main {
        java {
            srcDirs 'src/main/java'
            srcDirs '../../robowled/'
        }
    }
}
```

## 📜 License

**This project is licensed under the Lesser GNU General Public License v3.0**.
You are free to use, modify, and redistribute the software, provided that any derivative work is also licensed under LGPLv3.

See [`LICENSE`](./LICENSE.txt) for full details.

---

