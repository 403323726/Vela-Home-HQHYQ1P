# AI Agent prompt: adapt a HaQu Q1 Pro

Give the entire prompt below to an AI Agent with terminal access and make sure it starts in this repository root.

---

You are adapting a real Android projector. My device is a HaQu Q1 Pro (HQHYQ1P, Android 9, SDK 28). The ADB address is supplied through the `TV_HOST` environment variable.

Before starting, you must:

1. Read `AGENTS.md`, `docs/SAFETY.md`, and `docs/DEVICE-HQHYQ1P.md`.
2. Run read-only discovery first: model, SDK, display size, current Home, package list, disabled packages, and important vendor components.
3. Save a complete baseline in the local `backup/` or `work/` directory. Do not commit it.
4. Never uninstall system packages, launch factory/test menus, or modify root, recovery, SELinux, or system partitions.
5. Never disable the stock launcher without a verified recovery path.
6. Change one package or a small component at a time. Before each change, explain the purpose, risk, command, and undo command, and wait for my approval.
7. After every change, re-check `pm list packages -d`, pull a real screenshot, and inspect it. Do not rely on exit codes alone.
8. Never put a real IP, Wi-Fi name, serial number, account, ADB private key, or personal screenshot in the repository.

Current goals:

- Keep the stock Dangbei OS launcher package enabled and make Vela Home the default Home first.
- Verify Home, dock, inputs, Wi-Fi, vendor settings, Vela Home Settings, and language switching.
- Conservatively handle only clearly safe vendor telemetry/ad/duplicate background services that I explicitly approve.
- Keep the ADB controller `jiujiutv.jiujiu.adb` enabled unless I explicitly ask to disable it.

Start by outputting:

- A device-facts table
- The current Home activity
- An enabled/disabled package summary
- Packages related to input, networking, Bluetooth, remote, keyboard, launcher, settings, and ADB
- Unknown packages and the `dumpsys package` checks you recommend
- A no-change baseline backup result

Stop after the read-only phase and wait for my approval before changing anything.
