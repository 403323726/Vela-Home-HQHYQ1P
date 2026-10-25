# ADB tools / ADB 工具

[简体中文](#简体中文) | [English](#english)

## 简体中文

### `TV-ADB-Enabler-3.0.apk`

这是用户提供并在哈趣 Q1 Pro 上实测过的第三方 ADB 控制器。

| 项目 | 值 |
|---|---|
| 包名 | `jiujiutv.jiujiu.adb` |
| 版本 | 3.0 |
| 文件大小 | 2,441,307 bytes |
| SHA-256 | `D57904C44A941C094BE2F4E887ED2A07451D2CB36027481350C92311DBCD8A5A` |
| 签名证书 SHA-256 | `1e668e1e689fe7451a53532651cb739aa29882d6171917bfbbb28ea10e84e929` |

APK 的清单中包含：

- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `ACCESS_WIFI_STATE`
- `WRITE_SETTINGS`（Android 9 及以下）
- `WRITE_SECURE_SETTINGS`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE`

这不是本仓库维护的软件，源码和长期行为无法验证。它被保留下来是为了复现参考设备的 ADB 开启流程，不代表推荐或安全审计通过。

使用建议：

1. 只在可信局域网中的投影仪上安装。
2. 安装前核对 SHA-256。
3. 打开应用后按提示开启 ADB。
4. 在电脑上运行 `python -I tools/tv.py --probe`。
5. 投影仪弹出调试授权时由设备所有者本人确认。
6. 不要把 `5555` 暴露到公网。
7. 如果你不需要 ADB，关闭 Network/ADB debugging。

### `ADBRecovery.apk`

这是用于 ADB 端口意外丢失时的应急工具。

| 项目 | 值 |
|---|---|
| 包名 | `com.codex.adbrecovery` |
| SHA-256 | `5143778BBE8927A07AC2983AD024EE5440DCCB62A6406215177C25A3F59E49AD` |

它在打开后尝试执行：

```sh
setprop service.adb.tcp.port 5555
setprop persist.adb.tcp.port 5555
setprop ctl.restart adbd
setprop ctl.start adbd
```

该工具不申请网络权限，也没有网络功能。它不能修复被关闭的 Wi-Fi、被停用的系统包或损坏的系统分区。

### 校验文件

Windows PowerShell：

```powershell
Get-FileHash .\adb\TV-ADB-Enabler-3.0.apk -Algorithm SHA256
Get-FileHash .\adb\ADBRecovery.apk -Algorithm SHA256
```

Linux/macOS：

```bash
sha256sum adb/TV-ADB-Enabler-3.0.apk adb/ADBRecovery.apk
```

---

## English

### `TV-ADB-Enabler-3.0.apk`

This is a user-supplied third-party ADB controller that was tested on the HaQu Q1 Pro.

| Item | Value |
|---|---|
| Package | `jiujiutv.jiujiu.adb` |
| Version | 3.0 |
| Size | 2,441,307 bytes |
| SHA-256 | `D57904C44A941C094BE2F4E887ED2A07451D2CB36027481350C92311DBCD8A5A` |
| Signing certificate SHA-256 | `1e668e1e689fe7451a53532651cb739aa29882d6171917bfbbb28ea10e84e929` |

Its manifest requests:

- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `ACCESS_WIFI_STATE`
- `WRITE_SETTINGS` (Android 9 and older)
- `WRITE_SECURE_SETTINGS`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE`

This is not software maintained by this repository. Its source and future behavior cannot be verified. It is preserved to reproduce the reference device's ADB setup, not as an endorsement or security audit.

Recommended use:

1. Install it only on a projector on a trusted LAN.
2. Verify the SHA-256 before installation.
3. Open it and follow its prompts to enable ADB.
4. Run `python -I tools/tv.py --probe` from the computer.
5. The device owner must accept the debugging authorization prompt.
6. Never expose port `5555` to the public internet.
7. Turn Network/ADB debugging off when it is no longer needed.

### `ADBRecovery.apk`

An emergency helper for a lost ADB port.

| Item | Value |
|---|---|
| Package | `com.codex.adbrecovery` |
| SHA-256 | `5143778BBE8927A07AC2983AD024EE5440DCCB62A6406215177C25A3F59E49AD` |

When opened, it attempts to run:

```sh
setprop service.adb.tcp.port 5555
setprop persist.adb.tcp.port 5555
setprop ctl.restart adbd
setprop ctl.start adbd
```

It has no network permission and no network functionality. It cannot repair broken Wi-Fi, a disabled system package, or a damaged system partition.

### Verify downloads

Windows PowerShell:

```powershell
Get-FileHash .\adb\TV-ADB-Enabler-3.0.apk -Algorithm SHA256
Get-FileHash .\adb\ADBRecovery.apk -Algorithm SHA256
```

Linux/macOS:

```bash
sha256sum adb/TV-ADB-Enabler-3.0.apk adb/ADBRecovery.apk
```
