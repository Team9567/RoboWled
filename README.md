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

## 📦 Installation

Add RoboWled to your robot project's `build.gradle`:

```groovy
repositories {
    // Add the RoboWled Maven repository
    maven { url 'https://team9567.github.io/RoboWled/' }
}

dependencies {
    implementation 'com.github.team9567:robowled:VERSION'
}
```

Replace `VERSION` with the desired version (e.g., `1.0.0`).

---

## 📜 License

**This project is licensed under the Lesser GNU General Public License v3.0**.
You are free to use, modify, and redistribute the software, provided that any derivative work is also licensed under LGPLv3.

See [`LICENSE`](./LICENSE.txt) for full details.

---

