# HaQu Q1 Pro / HQHYQ1P Notes

[简体中文](#简体中文) | [English](#english)

## 简体中文

### 已验证环境

| 项目 | 值 |
|---|---|
| 型号 | HQHYQ1P |
| Android | 9 |
| SDK | 28 |
| 分辨率 | 1920×1080 |
| 物理密度 | 240 dpi |
| Vela Home | `com.gathin.tvhome` 2.8.7 |
| 默认 Home | `com.gathin.tvhome/.MainActivity` |
| 原桌面 | `com.dangbei.leard.leradlauncher` |
| 原桌面 Activity | `com.dangbei.leard.leradlauncher/com.dangbei.launcher.home.LauncherActivity` |
| 厂商设置 | `com.dangbei.haqusetting/.ui.HomeActivity` |
| ADB 控制器 | `jiujiutv.jiujiu.adb` 3.0 |

这些结果来自一个具体固件版本。系统升级后包名和 Activity 可能改变，必须重新用 `dumpsys package` 验证。

### 已验证的界面行为

- 顶部“输入源”可以列出 HDMI1。
- HDMI1 的 TIF input ID 为 `com.mediatek.tvinput/.hdmi.HDMIInputService/HW5`。
- 厂商 HDMI 页面可尝试：
  `com.cvte.tv.api.impl/com.cvte.tv.api.tif.TvSupportActivity`
- 未接 HDMI 信号时，正确路径会显示厂商的浮动“无信号”窗口；如果只看到纯黑屏，可能进入了底层输入层而不是厂商提示层。
- 顶部“设置”打开的是厂商设置，不是 Vela Home 自己的设置。
- Vela Home 的设置入口是应用列表末尾的“桌面设置 / Vela桌面设置”磁贴。
- 语言切换在 `Vela桌面设置 → 语言` 中。
- 当贝音乐 `com.dangbei.dbmusic.dbos.tab` 可从桌面隐藏。
- 系统应用文件夹固定在 Dock 最后。

### 当贝OS桌面

参考固件不允许单独停用当贝OS的 `LauncherActivity`：

- `pm disable-user` 组件操作会触发 `SecurityException`
- 保留 `com.dangbei.leard.leradlauncher` 包启用
- 可通过 appops 限制后台：
  - `RUN_IN_BACKGROUND: ignore`
  - `RUN_ANY_IN_BACKGROUND: ignore`
- 恢复脚本必须保留

不要假设其他哈趣型号也有同样的限制。

### 系统应用文件夹参考

参考设备精简后保留过：

- `com.dangbei.lerad.videoposter`
- `com.dangbei.screencast`
- `com.dangbei.zhushou.os`
- `com.tv.filemanager.os`

仅从文件夹隐藏、不卸载的示例：

- `com.dangbei.lerad.leradwatcher`
- `com.cvte.tv.setting`
- `com.cvte.tv.btaudioonly`
- `com.dangbei.haqusetting`
- `com.cvte.functionaltest`
- `com.mediatek.bootvideo`
- `com.dangbei.lerad.etna.haqu`
- `com.dangbei.miracast`

请先用 `dumpsys package` 确认这些包在你的固件中仍然存在、用途一致，再修改 `launcher/app/src/main/res/values/config.xml`。

### 排查命令

```powershell
python -I tools/tv.py "getprop ro.product.model"
python -I tools/tv.py "getprop ro.build.version.sdk"
python -I tools/tv.py "wm size"
python -I tools/tv.py "wm density"
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
python -I tools/tv.py "dumpsys package com.dangbei.leard.leradlauncher"
python -I tools/tv.py "dumpsys package com.dangbei.haqusetting"
python -I tools/tv.py "dumpsys package jiujiutv.jiujiu.adb"
```

### 已知坑

1. 顶部“设置”不等于 Vela Home 设置，它通常进入厂商设置。
2. 没有 HDMI 信号时黑屏不一定是桌面故障。
3. 某些包会被厂商开机服务重新启用，记录即可，不要反复强行处理。
4. 停用 ADB 控制器前必须确认还有其他调试入口。
5. 原桌面包可以保持启用，不需要为了“省内存”而冒险停用整个包。

---

## English

### Verified environment

| Item | Value |
|---|---|
| Model | HQHYQ1P |
| Android | 9 |
| SDK | 28 |
| Display | 1920×1080 |
| Physical density | 240 dpi |
| Vela Home | `com.gathin.tvhome` 2.8.7 |
| Default Home | `com.gathin.tvhome/.MainActivity` |
| Stock launcher | `com.dangbei.leard.leradlauncher` |
| Stock Home activity | `com.dangbei.leard.leradlauncher/com.dangbei.launcher.home.LauncherActivity` |
| Vendor settings | `com.dangbei.haqusetting/.ui.HomeActivity` |
| ADB controller | `jiujiutv.jiujiu.adb` 3.0 |

These results came from one firmware build. Package names and activities can change after an update; verify them with `dumpsys package`.

### Verified UI behavior

- The top-bar Inputs pill lists HDMI1.
- The HDMI1 TIF input ID is `com.mediatek.tvinput/.hdmi.HDMIInputService/HW5`.
- The vendor HDMI screen can be tried with:
  `com.cvte.tv.api.impl/com.cvte.tv.api.tif.TvSupportActivity`
- With no HDMI source connected, the correct route shows the vendor floating “No signal” window. A plain black screen may mean the firmware entered the low-level input layer instead.
- The top-bar Settings pill opens vendor Settings, not Vela Home Settings.
- Vela Home Settings is the “桌面设置 / Vela桌面设置” tile at the end of the app list.
- Language switching is under `Vela桌面设置 → 语言`.
- Dangbei Music `com.dangbei.dbmusic.dbos.tab` can be hidden from Home.
- The system-app folder is pinned to the end of the dock.

### Dangbei OS launcher

The reference firmware rejects per-component changes to the Dangbei `LauncherActivity`:

- Component-level `pm disable-user` raises `SecurityException`
- Keep `com.dangbei.leard.leradlauncher` enabled
- Optionally restrict background execution with appops:
  - `RUN_IN_BACKGROUND: ignore`
  - `RUN_ANY_IN_BACKGROUND: ignore`
- Keep the restore script available

Do not assume another HaQu model behaves the same way.

### System-app folder reference

After cleanup, the reference device kept:

- `com.dangbei.lerad.videoposter`
- `com.dangbei.screencast`
- `com.dangbei.zhushou.os`
- `com.tv.filemanager.os`

Examples hidden from the folder without uninstalling:

- `com.dangbei.lerad.leradwatcher`
- `com.cvte.tv.setting`
- `com.cvte.tv.btaudioonly`
- `com.dangbei.haqusetting`
- `com.cvte.functionaltest`
- `com.mediatek.bootvideo`
- `com.dangbei.lerad.etna.haqu`
- `com.dangbei.miracast`

Inspect each package with `dumpsys package` before editing `launcher/app/src/main/res/values/config.xml`.

### Diagnostic commands

```powershell
python -I tools/tv.py "getprop ro.product.model"
python -I tools/tv.py "getprop ro.build.version.sdk"
python -I tools/tv.py "wm size"
python -I tools/tv.py "wm density"
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
python -I tools/tv.py "dumpsys package com.dangbei.leard.leradlauncher"
python -I tools/tv.py "dumpsys package com.dangbei.haqusetting"
python -I tools/tv.py "dumpsys package jiujiutv.jiujiu.adb"
```

### Known pitfalls

1. The top-bar Settings pill is usually vendor Settings, not Vela Home Settings.
2. A black HDMI screen is not necessarily a launcher failure.
3. A vendor boot service may re-enable some packages; document it instead of fighting it.
4. Do not disable the ADB controller until another debugging route is confirmed.
5. Keep the stock launcher package enabled rather than risking a device that cannot return Home.
