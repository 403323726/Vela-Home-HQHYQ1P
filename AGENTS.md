# AGENTS.md

Instructions for any AI Agent or coding assistant working with this repository and a real Android TV / projector.

## Mission

Help the device owner adapt Vela Home and the projector safely. Prefer evidence, small reversible changes, and explicit user consent over broad "debloat" actions.

The reference device is a HaQu Q1 Pro (`HQHYQ1P`, Android 9 / SDK 28). Treat its package names and activities as firmware-specific evidence, not as universal Android rules.

## Absolute safety rules

Never do any of the following without an explicit, specific request from the device owner:

- Run `pm uninstall` against a system package.
- Disable `com.android.*`, `com.google.android.*`, permission controllers, package installers, System UI, Settings, providers, shell, phone, or `android`.
- Launch factory, production, AT, engineering, or test menus. Some boards persist a factory-mode flag that survives a reboot.
- Disable the stock launcher before the replacement Home has been installed, started, and verified.
- Modify bootloader, recovery, root, SELinux, or verified-boot state.
- Expose ADB port `5555` to the public internet.
- Commit real IP addresses, Wi-Fi names, serial numbers, MAC addresses, accounts, device names, ADB private keys, or personal screenshots.
- Paste secrets or tokens into a shell command that will be committed or logged.

If a command is uncertain, inspect first and ask. Do not "try it and see" on a device that may not boot.

## First-contact checklist

Before changing anything:

1. Confirm the device is the intended one and that the owner is present.
2. Read `getprop ro.product.model`, `ro.build.version.release`, `ro.build.version.sdk`, and the firmware build.
3. Read the current Home activity:
   `cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME`
4. Save the installed package list, disabled package list, relevant settings, and `dumpsys` output to a local `backup/` or `work/` directory that is ignored by Git.
5. Ask what the owner uses: streaming apps, casting, voice assistant, Chromecast/Miracast, firmware updates, and USB devices.
6. Check whether the network is Wi-Fi, Ethernet, or a USB Ethernet adapter.
7. Record the current ADB controller package and whether the owner wants ADB left enabled.

A baseline should include, at minimum:

```text
pm list packages
pm list packages -d
pm list packages -e
getprop
wm size
wm density
cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
dumpsys package <important packages>
```

## Package triage

Classify packages into categories before changing them:

- Vendor telemetry, analytics, recommendation, ad, or content services
- Duplicate cast/share/cleaner services
- Replaceable preloaded apps
- Streaming or user apps the owner uses
- Core Android and system services that must remain enabled
- Unknown packages that require `dumpsys package` and owner confirmation

Use `dumpsys package <package>` to inspect activities, services, permissions, and the APK path. Do not infer safety from a short package name.

For the reference device, keep the following classes of packages enabled unless the owner explicitly accepts the risk after inspection:

- `com.android.*` and `com.google.android.*` core components
- Permission controller, package installer, ExtServices, ExtShared, module metadata
- `com.android.systemui`, `com.android.shell`, `com.android.phone`, `com.android.settings`, `com.android.tv.settings`
- Providers and system services that implement input, networking, Bluetooth pairing, remote control, or the on-screen keyboard
- `jiujiutv.jiujiu.adb` while the owner wants ADB access

## Change discipline

- Change one package or a small batch at a time.
- Use `pm disable-user --user 0 <package>` only when the package is clearly safe and the owner has agreed.
- Run one `pm` command per shell call; do not chain commands that can silently stop after the first failure.
- After each change, verify with `pm list packages -d` rather than trusting an exit code.
- Capture a screenshot or the focused activity after each batch.
- Keep an exact undo command for every change.
- If a firmware rejects `pm disable-user`, `pm hide`, or component changes, stop retrying and document the limitation.

## Home guard

A replacement launcher is not proven until all of these are true:

1. Its APK installs successfully.
2. It starts successfully on its own.
3. `cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity` succeeds.
4. `cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME` resolves to Vela Home.
5. Pressing Home from an app returns to Vela Home.
6. The stock launcher remains enabled and can be restored.

On the reference Q1 Pro, the stock Dangbei launcher Activity cannot be disabled individually because the firmware raises a `SecurityException`. Keep the package enabled. If background restrictions are desired, use the provided scripts and preserve the restore script.

## ADB safety

- Port `5555` is plaintext.
- Work on a trusted LAN only.
- Ask the owner to accept the authorization prompt themselves.
- Do not leave a relay or open ADB port running after the session unless the owner explicitly requests it.
- Do not place the ADB private key, `tools/adbkey/`, credentials, or device backups in Git.
- The third-party `TV-ADB-Enabler-3.0.apk` requests powerful permissions. Read `adb/README.md` and verify its SHA-256 before using it.

## Verification

For visual changes:

- Use ADB key injection plus a screenshot pulled from the TV.
- Check the focused activity when an app intentionally blocks screenshots.
- Test D-pad edges: Up, Down, Left, and Right.
- Test Home from at least one launched app.
- Reboot once and verify Home, ADB, language persistence, and the restore path.
- Do not claim a physical remote button works if it depends on an accessibility helper and you only injected a key; ask the owner to press the real button.

For package changes:

- Re-list disabled packages after reboot.
- Some firmware re-enables packages from its own boot list; document this instead of fighting it.
- Verify Wi-Fi and ADB survive a reboot if a setup wizard or vendor service was involved.

## Privacy

Never publish or commit:

- Real LAN IP addresses
- Wi-Fi SSIDs or passwords
- Serial numbers, Android IDs, MAC addresses, or device names
- Account emails or tokens
- ADB keys
- Screenshots showing personal content or accounts

Use placeholders such as `<PROJECTOR_LAN_IP>` in documentation and examples.

## Communication

When reporting work, state:

- What was inspected
- What changed
- What was not changed
- What was verified on the real device
- The exact recovery path
- Any remaining risk or firmware limitation

If a safety condition is unclear, stop and ask a concise question instead of making a destructive assumption.
