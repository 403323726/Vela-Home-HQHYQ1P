# Adapting a HaQu Q1 Pro with an AI Agent

This guide assumes that a computer can reach the projector over ADB and that you want an AI Agent with terminal access (Codex, Claude Code, Gemini CLI, Cursor, or similar) to help with discovery, launcher replacement, conservative cleanup, verification, and recovery.

> This is written for the HQHYQ1P / Android 9 / SDK 28 reference firmware. Treat the package names as device evidence, not universal rules.

## 0. Give the Agent rules first

Before any change, ask the Agent to read [`AGENTS.md`](../AGENTS.md). The essential rules are:

- Do not uninstall system packages.
- Do not open factory or test menus.
- Do not disable the stock launcher without a verified recovery path.
- Do not put a real IP, Wi-Fi name, serial number, account, or ADB private key in Git.
- Make one small change at a time and verify it immediately.
- Inspect unknown packages with `dumpsys package` and ask the owner before changing them.

If you use Codex, start a session in this repository and paste [`prompts/adapt-device.en.md`](../prompts/adapt-device.en.md).

## 1. Prepare the computer

You need:

- Windows 10/11, macOS, or Linux
- Python 3.10+
- JDK 17 or newer
- Android SDK 34, only if you are building the APK
- Git
- A trusted LAN shared with the projector

Create the Python environment:

```powershell
py -m venv tools/venv
tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt
```

Linux/macOS:

```bash
python3 -m venv tools/venv
tools/venv/bin/pip install -r tools/requirements.txt
```

Set the device address:

```powershell
$env:TV_HOST = "<PROJECTOR_LAN_IP>"
```

or:

```bash
export TV_HOST="<PROJECTOR_LAN_IP>"
```

Never commit the real address.

## 2. First connection

Start with read-only commands:

```powershell
python -I tools/tv.py --probe
python -I tools/tv.py "getprop ro.product.model"
python -I tools/tv.py "getprop ro.build.version.release"
python -I tools/tv.py "getprop ro.build.version.sdk"
```

Accept the debugging prompt on the projector if it appears.

If port `5555` is closed:

1. Install and open `adb/TV-ADB-Enabler-3.0.apk` on the projector.
2. Follow its prompts to enable ADB.
3. Run `python -I tools/tv.py --probe` again.
4. If it still fails, check the IP, guest-network isolation, and firewall before trying more random ADB tools.

## 3. Create a baseline backup

Save the baseline in `backup/` or `work/`. Both are excluded by `.gitignore`.

```powershell
New-Item -ItemType Directory -Force backup | Out-Null
python -I tools/tv.py "getprop" > backup/getprop.txt
python -I tools/tv.py "pm list packages" > backup/packages.txt
python -I tools/tv.py "pm list packages -d" > backup/packages-disabled.txt
python -I tools/tv.py "pm list packages -e" > backup/packages-enabled.txt
python -I tools/tv.py "wm size" > backup/wm-size.txt
python -I tools/tv.py "wm density" > backup/wm-density.txt
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME" > backup/home.txt
python -I tools/tv.py "settings list global" > backup/settings-global.txt
python -I tools/tv.py "settings list secure" > backup/settings-secure.txt
```

For any important package:

```powershell
python -I tools/tv.py "dumpsys package <PACKAGE>" > backup/<PACKAGE>.txt
```

Backups can contain device names or serial numbers. Keep them local.

## 4. Inventory before changes

Ask the Agent to produce a read-only table containing:

- Model, Android version, and SDK
- Current Home activity
- Resolution and density
- Enabled and disabled packages
- Vendor settings, input, network, Bluetooth, remote, and keyboard packages
- User-installed apps
- Stock launcher, ADB controller, and Dangbei OS components
- Unknown packages that need inspection

No package should be disabled during this step.

## 5. Package classification and conservative cleanup

### Candidates for investigation

- Vendor telemetry, analytics, recommendation, or ad services
- Duplicate cast/share/cleaner/app-store services
- Preloaded content apps that do not affect boot
- Streaming apps the owner explicitly does not use

### Keep enabled

- Core `com.android.*` and `com.google.android.*` components
- Permission controller, installer, ExtServices, ExtShared, and module metadata
- System UI, Settings, Shell, Phone, and system providers
- Input, HDMI, tuner, remote, Bluetooth pairing, keyboard, and networking services
- Play Store / Play Services if the device depends on them
- The ADB controller while the owner needs ADB

### Cleanup loop

Inspect one package:

```powershell
python -I tools/tv.py "dumpsys package <PACKAGE>"
```

After confirming its purpose and undo command:

```powershell
python -I tools/tv.py "pm disable-user --user 0 <PACKAGE>"
python -I tools/tv.py "pm list packages -d"
```

Write the undo command to a local recovery file:

```powershell
python -I tools/tv.py "pm enable --user 0 <PACKAGE>"
```

Do not use `pm uninstall`. Do not disable a large list in one batch. Do not disable a package merely because its name looks unimportant.

### If the firmware refuses

Some firmware raises `SecurityException` for `pm disable-user`, `pm hide`, or component changes. Stop retrying. Record the error and use a less invasive option such as background restrictions, privacy settings, or leaving the package enabled. Do not use root or modify system partitions to force it.

## 6. Install and set Vela Home

Use `-StockLauncher none` for the first installation so the stock launcher stays available:

```powershell
$env:TV_HOST = "<PROJECTOR_LAN_IP>"
powershell -ExecutionPolicy Bypass -File launcher/build-and-install.ps1 -TvHost $env:TV_HOST -StockLauncher none
```

Or install the pinned APK directly:

```powershell
python -I tools/b64push.py apk/CleanYourTV-HQHYQ1P-2.8.7.apk /data/local/tmp/vela-home.apk
python -I tools/tv.py "pm install -r /data/local/tmp/vela-home.apk"
python -I tools/tv.py "rm -f /data/local/tmp/vela-home.apk"
python -I tools/tv.py "cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity"
```

Start it:

```powershell
python -I tools/tv.py "am start -W -n com.gathin.tvhome/.MainActivity"
```

## 7. Verification checklist

### Home

```powershell
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
```

Expected:

```text
com.gathin.tvhome/.MainActivity
```

### Foreground activity

```powershell
python -I tools/tv.py "dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity' | head -n 3"
```

### Screenshot

```powershell
python -I tools/tv.py --screenshot shots/after-install.png --wait 2
```

The Agent must inspect the screenshot, not just the exit code. For D-pad checks:

```powershell
python -I tools/tv.py --screenshot shots/after-down.png DPAD_DOWN --wait 1
```

### Functional checks

- Home returns to Vela Home
- Home from an opened app returns to Vela Home
- Dock apps launch
- Inputs lists HDMI
- Wi-Fi opens the network screen
- Settings and the launcher-settings tile behave as expected
- Both languages switch immediately
- The ADB controller is still installed and enabled
- The stock launcher is still enabled

## 8. Optional Dangbei OS background restriction

On the reference firmware, disabling only the Dangbei `LauncherActivity` fails with `SecurityException`. The conservative option is to keep the package enabled and restrict its background execution:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/restrict-dangbei-background.ps1 -TvHost $env:TV_HOST
```

Restore:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/restore-dangbei-background.ps1 -TvHost $env:TV_HOST
```

Add `-MakeDefault` if Dangbei OS should also become the default Home again.

Do not disable the ADB controller immediately after applying background restrictions. Reboot once and verify Vela Home, networking, and ADB.

## 9. Post-reboot verification

```powershell
python -I tools/tv.py "pm list packages -d"
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
python -I tools/tv.py "pidof com.gathin.tvhome"
python -I tools/tv.py "cmd appops get com.dangbei.leard.leradlauncher RUN_IN_BACKGROUND"
```

If a vendor service re-enables a package after every boot, document it instead of fighting the vendor boot logic.

## 10. Recovery

### Restore Dangbei background and Home

```powershell
powershell -ExecutionPolicy Bypass -File scripts/restore-dangbei-background.ps1 -TvHost $env:TV_HOST -MakeDefault
```

### Recover a lost ADB port

Install and open `adb/ADBRecovery.apk` on the projector. Wait about 10 seconds, then:

```powershell
python -I tools/tv.py --probe
```

### Return to the stock launcher

Make sure the package is enabled first, then:

```powershell
python -I tools/tv.py "cmd package set-home-activity --user 0 com.dangbei.leard.leradlauncher/com.dangbei.launcher.home.LauncherActivity"
```

If the component differs on your firmware, inspect it with `dumpsys package` first.

## 11. A restrained Agent prompt

See [`prompts/adapt-device.en.md`](../prompts/adapt-device.en.md). Short version:

```text
Read AGENTS.md and docs/SAFETY.md first. Probe my HaQu Q1 Pro read-only, save a complete baseline, and list the current Home, packages, disabled packages, and unknown packages. Do not disable, uninstall, or launch factory menus. Before every change, explain the purpose, risk, and undo command and wait for my approval. Change one package at a time, then take a screenshot and verify it.
```

## 12. Troubleshooting

### A Settings APK still cannot open Android Settings

The vendor may not export a standard Settings activity. Inspect:

```powershell
python -I tools/tv.py "dumpsys package com.android.settings | grep -A 3 'android.intent.action.SETTINGS'"
python -I tools/tv.py "cmd package query-activities -a android.settings.SETTINGS"
```

### The Home chooser appears repeatedly

There may be multiple Home claimants, or the stock launcher may be disabled without a working fallback:

```powershell
python -I tools/tv.py "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
```

Do not disable more components until the stock Home activity is known.

### HDMI says “No signal”

This is normal when no source is connected. Verify the input activity and TIF input ID, and check whether the vendor floating “No signal” window is expected.

### ADB disappears after a reboot

Check whether the ADB controller is still enabled. If it is disabled, use `adb/ADBRecovery.apk` before considering a factory reset.

### Home still uses the old language after switching

Version 2.8.7 fixes that issue. Confirm that `versionCode 21` or newer is installed and that `pm install -r` actually replaced the old APK.

## Final note

The hard part is not finding a button that opens a screen. The hard part is knowing:

1. Which component owns the screen
2. Whether changing it affects boot, input, networking, or the remote
3. How to recover if it fails

An Agent that works from evidence and a recovery path is safer than one chasing a one-shot “success.”
