# ADBRecovery source

This directory contains the apktool-style source used to produce `../ADBRecovery.apk`.

- Package: `com.codex.adbrecovery`
- Purpose: request the device's ADB daemon to listen on TCP port `5555` again
- No network permission
- No analytics, services, or background polling

The prebuilt APK is included for convenience. If you rebuild it, sign with your own key and verify the resulting behavior on a non-critical device first.

The helper cannot repair broken Wi-Fi, a disabled system package, or a damaged system image.
