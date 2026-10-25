# Vela Home APK

[简体中文](#简体中文) | [English](#english)

## 简体中文

### 文件

`CleanYourTV-HQHYQ1P-2.8.7.apk`

| 项目 | 值 |
|---|---|
| 应用名称 | Vela桌面 / Vela Home |
| 包名 | `com.gathin.tvhome` |
| 版本 | 2.8.7 |
| versionCode | 21 |
| minSdk | 26 |
| targetSdk | 34 |
| 默认 Home | `com.gathin.tvhome/.MainActivity` |
| SHA-256 | `859A321E2B5EC8AB2FFB1B7AE4D535F44C046AE725AC918734B643D9A05872FE` |

### 说明

- 这是用于侧载的 release 构建，使用本地调试签名，不适合上架应用商店。
- `minSdk 26` 表示 Android 8.0 及以上。
- 哈趣 Q1 Pro 参考设备为 Android 9 / SDK 28，已经实机验证。
- 安装不会自动停用原桌面；请先设置并验证新 Home。
- 语言切换、应用隐藏、排序、系统应用文件夹和桌面设置均包含在此版本中。

### 安装

```powershell
$env:TV_HOST = "<投影仪局域网 IP>"
python -I tools/b64push.py apk/CleanYourTV-HQHYQ1P-2.8.7.apk /data/local/tmp/vela-home.apk
python -I tools/tv.py "pm install -r /data/local/tmp/vela-home.apk"
python -I tools/tv.py "rm -f /data/local/tmp/vela-home.apk"
python -I tools/tv.py "cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity"
```

### 校验

```powershell
Get-FileHash .\apk\CleanYourTV-HQHYQ1P-2.8.7.apk -Algorithm SHA256
```

---

## English

### File

`CleanYourTV-HQHYQ1P-2.8.7.apk`

| Item | Value |
|---|---|
| App label | Vela桌面 / Vela Home |
| Package | `com.gathin.tvhome` |
| Version | 2.8.7 |
| versionCode | 21 |
| minSdk | 26 |
| targetSdk | 34 |
| Default Home | `com.gathin.tvhome/.MainActivity` |
| SHA-256 | `859A321E2B5EC8AB2FFB1B7AE4D535F44C046AE725AC918734B643D9A05872FE` |

### Notes

- This is a sideload release build signed with a local debug key. It is not intended for an app store.
- `minSdk 26` means Android 8.0 or newer.
- The HaQu Q1 Pro reference device runs Android 9 / SDK 28 and has been tested on the real device.
- Installation does not automatically disable the stock launcher. Set and verify the new Home first.
- Language switching, app hiding, reordering, the system-app folder, and launcher settings are included in this version.

### Install

```powershell
$env:TV_HOST = "<PROJECTOR_LAN_IP>"
python -I tools/b64push.py apk/CleanYourTV-HQHYQ1P-2.8.7.apk /data/local/tmp/vela-home.apk
python -I tools/tv.py "pm install -r /data/local/tmp/vela-home.apk"
python -I tools/tv.py "rm -f /data/local/tmp/vela-home.apk"
python -I tools/tv.py "cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity"
```

### Verify

```powershell
Get-FileHash .\apk\CleanYourTV-HQHYQ1P-2.8.7.apk -Algorithm SHA256
```
