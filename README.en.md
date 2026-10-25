# Vela Home for HaQu Q1 Pro

[简体中文](README.md) | **English**

![Vela Home icon](assets/branding/vela-home-icon-512.png)

> An Apple TV-style Android TV launcher for the **HaQu Q1 Pro (HQHYQ1P, Android 9 / SDK 28)**, plus a practical workflow for using an AI Agent to adapt, verify, and recover a real projector safely.
>
> This is an unofficial community project. It is not affiliated with HaQu, Dangbei, CVTE, or the upstream CleanYourTV maintainers. Read the safety notes before changing a device.

## What this repository provides

Many China-market projectors hide Developer options, ADB, and even the native Android Settings screens. Installing one Settings APK is often not enough because different firmware builds hide different activities, and the home screen, HDMI, Wi-Fi, and system settings may be owned by vendor components.

This repository provides:

1. **A working Vela Home build** (`com.gathin.tvhome`) with Simplified Chinese and English UI that can be set as the default Home.
2. **An AI Agent workflow** that discovers device facts first and makes small, reversible changes instead of bulk-disabling packages.
3. **A real-device record** of verified package names, activities, HDMI routes, risks, and recovery steps for the HaQu Q1 Pro.

## Features

- Apple TV-style dark home screen, dock, and app grid
- Persistent Simplified Chinese / English language switching
- App hiding, app information, reordering, and moving
- System-app folder
- HDMI / input picker
- Wi-Fi and vendor settings shortcuts
- Optional Dangbei OS background restriction
- No network permission, analytics, telemetry, or network polling in the launcher

![Chinese home screen](assets/screenshots/home-zh.png)

![English language settings](assets/screenshots/settings-en.png)

## Downloads

| File | Purpose |
|---|---|
| [`apk/CleanYourTV-HQHYQ1P-2.8.7.apk`](apk/CleanYourTV-HQHYQ1P-2.8.7.apk) | Vela Home APK, version `2.8.7 (21)` |
| [`adb/TV-ADB-Enabler-3.0.apk`](adb/TV-ADB-Enabler-3.0.apk) | User-supplied third-party ADB controller (`jiujiutv.jiujiu.adb`) |
| [`adb/ADBRecovery.apk`](adb/ADBRecovery.apk) | Emergency ADB-port recovery helper |
| [`SHA256SUMS.txt`](SHA256SUMS.txt) | SHA-256 hashes for published files |

Verify every download. Install only the pinned files in this repository or an APK you built yourself, never an unknown copy forwarded through a chat group or cloud drive.

## Safety first

ADB over TCP port `5555` is unencrypted. Once it is enabled, other devices on the same LAN may try to connect.

- Enable it only on a trusted home or lab network.
- Never expose port `5555` to the public internet or keep it enabled on public Wi-Fi.
- Accept the “Allow USB debugging?” prompt only when you are performing the setup.
- Turn Network/ADB debugging off when you are finished. If you intentionally keep it enabled as the reference device does, restrict access to your LAN.
- The third-party ADB APK requests `WRITE_SECURE_SETTINGS`, network access, boot broadcast, and related permissions. Read [`adb/README.md`](adb/README.md) before installing it.

## Quick start: install Vela Home

The examples below use Windows PowerShell. Replace `<PROJECTOR_LAN_IP>` with your own address. Never commit a real IP address to this repository.

### 1. Enable ADB

Copy `adb/TV-ADB-Enabler-3.0.apk` to a USB drive or transfer it to the projector, install it, and open it. Follow its prompts to enable ADB, and make sure the projector and computer are on the same network.

Verify the connection and authorization:

```powershell
$env:TV_HOST = "<PROJECTOR_LAN_IP>"
python -I tools/tv.py --probe
```

Accept the prompt on the projector if one appears. A successful probe prints the model, Android version, and SDK.

### 2. Install the APK

```powershell
python -I tools/b64push.py apk/CleanYourTV-HQHYQ1P-2.8.7.apk /data/local/tmp/vela-home.apk
python -I tools/tv.py "pm install -r /data/local/tmp/vela-home.apk"
python -I tools/tv.py "rm -f /data/local/tmp/vela-home.apk"
```

`pm install -r` keeps existing app data. Do not disable the stock launcher yet.

### 3. Set Vela Home as Home

```powershell
python -I tools/tv.py "cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity"
python -I tools/tv.py "am start -W -n com.gathin.tvhome/.MainActivity"
```

Check that the Home key returns to Vela Home:

```powershell
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
```

The result should include:

```text
com.gathin.tvhome/.MainActivity
```

### 4. Verify the basics

At minimum, check that:

- Home returns to Vela Home
- Dock apps launch
- Inputs lists HDMI
- The Wi-Fi button opens the vendor network screen
- Launcher settings opens and the language switch works
- The ADB controller is still installed
- The original Dangbei OS launcher is still enabled

## Using an AI Agent to adapt your device

Do not tell an Agent only “debloat my TV.” That instruction is too broad and can disable load-bearing components. Ask it to follow a loop of **discover → back up → make a small change → verify with a screenshot → recover**.

### Recommended opening

Give the Agent this repository and ask it to read:

- [`AGENTS.md`](AGENTS.md)
- [`docs/AI-AGENT-GUIDE.en.md`](docs/AI-AGENT-GUIDE.en.md)
- [`docs/SAFETY.md`](docs/SAFETY.md)

A copy-paste prompt is available at [`prompts/adapt-device.en.md`](prompts/adapt-device.en.md).

### What the Agent must do first

1. Read the model, SDK, display size, Home activity, package list, and disabled-package list over ADB.
2. Save the output in a local `backup/` or `work/` directory.
3. Classify packages as vendor telemetry/ads, replaceable apps, or system-critical components. Do not guess from the package name alone.
4. For every uncertain package, inspect it with `dumpsys package <package>` and ask the user before changing it.
5. Change one component or a small batch at a time, then immediately verify with a screenshot and another `pm list packages -d` check.
6. Always keep a recovery script and a route back to the stock Home.

### Actions an Agent must never perform automatically

- `pm uninstall` a system package
- Disable core `com.android.*` or `com.google.android.*` components
- Launch factory, production, AT, or test menus
- Disable the stock launcher without a verified recovery path
- Expose ADB to the public internet
- Commit a real IP, Wi-Fi name, serial number, ADB private key, or account information

## Verified HaQu Q1 Pro facts

| Item | Value |
|---|---|
| Model | HQHYQ1P |
| Android | 9 / SDK 28 |
| Display | 1920×1080 |
| Physical density | 240 |
| Vela Home package | `com.gathin.tvhome` |
| Default Home | `com.gathin.tvhome/.MainActivity` |
| Stock launcher package | `com.dangbei.leard.leradlauncher` |
| Vendor settings | `com.dangbei.haqusetting/.ui.HomeActivity` |
| ADB controller | `jiujiutv.jiujiu.adb` |

See [`docs/DEVICE-HQHYQ1P.md`](docs/DEVICE-HQHYQ1P.md) for more device-specific details and verified activities.

### Handling Dangbei OS

On the reference firmware, disabling only the Dangbei `LauncherActivity` is rejected with a `SecurityException`. The conservative approach is:

- Keep `com.dangbei.leard.leradlauncher` enabled
- Make Vela Home the default Home
- Optionally restrict Dangbei OS background execution
- Keep [`scripts/restore-dangbei-background.ps1`](scripts/restore-dangbei-background.ps1) available for recovery

Do not disable the entire vendor launcher package until you have verified a recovery path.

## Build from source

The source is in [`launcher/`](launcher/). You need JDK 17+, Android SDK 34, and Python 3.

```powershell
py -m venv tools/venv
tools\venv\Scripts\python.exe -m pip install -r tools/requirements.txt
$env:JAVA_HOME = "<JDK_DIRECTORY>"
$env:ANDROID_HOME = "<ANDROID_SDK_DIRECTORY>"
$env:TV_HOST = "<PROJECTOR_LAN_IP>"
powershell -ExecutionPolicy Bypass -File launcher/build-and-install.ps1 -TvHost $env:TV_HOST -StockLauncher none
```

`-StockLauncher none` installs Vela Home and sets it as Home **without stopping the stock launcher**. Use that for the first installation. Only consider background restrictions after the new launcher is stable.

## Recovery

- Re-enable Dangbei OS background execution: `scripts/restore-dangbei-background.ps1`
- Make the stock launcher the default again: use that script's `-MakeDefault` option
- If the ADB port disappears: install and open `adb/ADBRecovery.apk` on the projector
- Full safety and recovery guidance: [`docs/SAFETY.md`](docs/SAFETY.md)

The recovery scripts only run explicit `pm enable`, Home switching, and app-op restoration commands. They do not uninstall Vela Home.

## Repository layout

```text
Vela-Home-HQHYQ1P/
├─ launcher/                 Vela Home Android source
├─ apk/                      Prebuilt APK
├─ adb/                      ADB enable and recovery tools
├─ assets/                   Square icon and screenshots
├─ docs/                     Device, safety, and AI Agent guides
├─ prompts/                  Copy-paste Agent prompts
├─ scripts/                  Build, check, and recovery scripts
├─ tools/                    ADB TCP client and upload helper
├─ AGENTS.md                 Mandatory device-safety rules for Agents
├─ LICENSE                   MIT
└─ NOTICE.md                 Upstream and third-party notices
```

## Known limitations

- The test results target **HQHYQ1P / Android 9 / a specific firmware**, not every HaQu model or firmware.
- Some firmware builds reject per-component changes to the vendor launcher or prevent ordinary apps from changing another app's component state.
- Android 9 TV firmware may not implement the standard `ACTION_HOME_SETTINGS`; use the vendor settings or ADB to set Home.
- The third-party ADB APK is not maintained by this project, and its source, future versions, and behavior are not guaranteed.
- A black screen or a floating “No signal” window is expected when no HDMI source is connected.

## Contributing

Contributions are welcome, especially:

- Tested notes for other HaQu models
- Package and Activity evidence from `dumpsys`
- Recovery scripts
- Chinese and English documentation without personal data
- Fixes that do not use root, bypass store protection, or expose another person's device

Remove IP addresses, Wi-Fi names, serial numbers, accounts, ADB private keys, and personal information from screenshots before committing.

## License and attribution

Vela Home is based on the [CleanYourTV](https://github.com/Gathin23/cleanyourtv) launcher and is distributed under the MIT License. The original project is copyright Gathin and its contributors. See [`NOTICE.md`](NOTICE.md) for attribution, font licensing, and third-party APK details.
