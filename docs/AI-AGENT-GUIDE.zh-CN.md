# 使用 AI Agent 适配哈趣 Q1 Pro

本指南假设你已经能让电脑通过 ADB 连接到投影仪，并希望让 AI Agent（Claude Code、Codex、Gemini CLI、Cursor 等具备终端能力的工具）帮助完成设备发现、桌面替换、保守清理、验证和恢复。

> 适用于 HQHYQ1P / Android 9 / SDK 28 的参考固件。其他型号请把本文当作方法，不要直接照抄包名。

## 0. 先给 Agent 立规矩

在开始前，把仓库根目录的 [`AGENTS.md`](../AGENTS.md) 交给 Agent，并要求它先阅读。最重要的规则是：

- 不卸载系统应用。
- 不打开工厂/测试菜单。
- 不在没有恢复路径时停用原桌面。
- 不把真实 IP、Wi-Fi 名、序列号、账号或 ADB 私钥写进仓库。
- 每次只做一小步，并立即验证。
- 不确定时先 `dumpsys package`，再问用户。

如果你使用 Codex，可以在仓库目录启动会话，然后粘贴 [`prompts/adapt-device.zh-CN.md`](../prompts/adapt-device.zh-CN.md) 的提示词。

## 1. 准备环境

### 电脑

- Windows 10/11、macOS 或 Linux
- Python 3.10+
- JDK 17 或更高版本
- Android SDK 34（只有构建 APK 时需要）
- Git
- 与投影仪处于同一可信局域网

### Python 工具

```powershell
py -m venv tools/venv
tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt
```

Linux/macOS 使用：

```bash
python3 -m venv tools/venv
tools/venv/bin/pip install -r tools/requirements.txt
```

### 设置设备地址

Windows PowerShell：

```powershell
$env:TV_HOST = "<投影仪局域网 IP>"
```

Linux/macOS：

```bash
export TV_HOST="<投影仪局域网 IP>"
```

不要把真实地址写入 `README`、脚本或 Git 提交。

## 2. 第一次连接

先执行只读探测：

```powershell
python -I tools/tv.py --probe
python -I tools/tv.py "getprop ro.product.model"
python -I tools/tv.py "getprop ro.build.version.release"
python -I tools/tv.py "getprop ro.build.version.sdk"
```

确认输出与你的设备一致。若投影仪弹出调试授权，请在投影仪上手动选择允许。

如果 `5555` 没有开放：

1. 在投影仪上安装并打开 `adb/TV-ADB-Enabler-3.0.apk`。
2. 按应用提示开启 ADB。
3. 再运行 `python -I tools/tv.py --probe`。
4. 如果仍然失败，先检查 IP、Wi-Fi 隔离、访客网络和防火墙，不要盲目尝试更多“ADB 工具”。

## 3. 建立基线备份

在让 Agent 修改任何东西之前，先把以下内容保存到 `backup/` 或 `work/`（这两个目录已在 `.gitignore` 中排除）：

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

如果 Agent 要改动某个重要包，再单独保存：

```powershell
python -I tools/tv.py "dumpsys package <包名>" > backup/<包名>.txt
```

备份文件可能包含设备名称或序列号，只保存在本地，不要上传。

## 4. 让 Agent 做设备清单

推荐让 Agent 只执行只读命令，先输出一张表：

- 型号、Android 版本、SDK
- 当前 Home Activity
- 分辨率与密度
- 已启用和已停用的包
- 厂商设置、输入源、网络、蓝牙、遥控器、输入法相关包
- 用户安装的第三方应用
- 原桌面、ADB 控制器、当贝OS组件
- 哪些包用途不明

Agent 不应该在这一步停用任何包。

## 5. 包分类与保守清理

### 可以优先调查的类别

- 厂商遥测、统计、推荐、广告服务
- 明显重复的投屏、分享、清理、应用市场服务
- 不影响系统启动的预装内容应用
- 用户明确不用的流媒体应用

### 必须保留的类别

- `com.android.*`、`com.google.android.*` 核心组件
- 权限控制器、安装器、ExtServices、ExtShared、模块元数据
- System UI、Settings、Shell、Phone、系统 Provider
- 输入源、HDMI、调谐器、遥控器、蓝牙配对、输入法、网络服务
- Play Store / Play Services（如果设备依赖）
- 用户要求保留的 ADB 控制器

### 清理流程

每次只处理一个包：

```powershell
python -I tools/tv.py "dumpsys package <包名>"
```

确认用途和恢复方式后，再执行：

```powershell
python -I tools/tv.py "pm disable-user --user 0 <包名>"
python -I tools/tv.py "pm list packages -d"
```

然后把恢复命令写进本地恢复文件：

```powershell
python -I tools/tv.py "pm enable --user 0 <包名>"
```

不要使用 `pm uninstall`。不要一次停用十几个包。不要停用名字看起来像系统核心的包。

### 如果固件拒绝

某些固件会对 `pm disable-user`、`pm hide` 或组件级 `disable` 抛出 `SecurityException`。此时：

1. 停止重试。
2. 记录错误。
3. 改用后台限制、隐私设置或保留原样。
4. 不要为了“成功”去尝试 root 或修改系统分区。

## 6. 安装并设置 Vela Home

第一次安装建议使用 `-StockLauncher none`，保留原桌面：

```powershell
$env:TV_HOST = "<投影仪局域网 IP>"
powershell -ExecutionPolicy Bypass -File launcher/build-and-install.ps1 -TvHost $env:TV_HOST -StockLauncher none
```

如果只安装成品 APK，也可执行：

```powershell
python -I tools/b64push.py apk/CleanYourTV-HQHYQ1P-2.8.7.apk /data/local/tmp/vela-home.apk
python -I tools/tv.py "pm install -r /data/local/tmp/vela-home.apk"
python -I tools/tv.py "rm -f /data/local/tmp/vela-home.apk"
python -I tools/tv.py "cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity"
```

安装后先启动：

```powershell
python -I tools/tv.py "am start -W -n com.gathin.tvhome/.MainActivity"
```

## 7. 必须验证的项目

### Home

```powershell
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
```

结果应为：

```text
com.gathin.tvhome/.MainActivity
```

### 当前前台 Activity

```powershell
python -I tools/tv.py "dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity' | head -n 3"
```

### 截图

```powershell
python -I tools/tv.py --screenshot shots/after-install.png --wait 2
```

请让 Agent 真的查看截图，而不是只看命令退出码。需要测试 D-pad 时：

```powershell
python -I tools/tv.py --screenshot shots/after-down.png DPAD_DOWN --wait 1
```

### 功能清单

- Home 键回到 Vela Home
- 打开一个应用后按 Home，仍回到 Vela Home
- Dock 应用可以启动
- 输入源能列出 HDMI
- Wi-Fi 按钮能打开网络页
- 设置按钮和桌面设置入口符合预期
- 语言切换中英文都能即时生效
- ADB 控制器仍然安装和启用
- 原桌面仍然启用

## 8. 可选的当贝OS后台限制

参考固件无法单独停用当贝OS的 `LauncherActivity`，但可以保留包启用并限制后台。先确认你接受这个方案，再运行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/restrict-dangbei-background.ps1 -TvHost $env:TV_HOST
```

恢复：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/restore-dangbei-background.ps1 -TvHost $env:TV_HOST
```

需要把当贝OS重新设为默认 Home 时，加 `-MakeDefault`。

不要在限制后台后立刻停用 ADB 控制器。先重启并确认 Vela Home、网络和 ADB 都还能恢复。

## 9. 重启后的复查

重启一次，至少检查：

```powershell
python -I tools/tv.py "pm list packages -d"
python -I tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
python -I tools/tv.py "pidof com.gathin.tvhome"
python -I tools/tv.py "cmd appops get com.dangbei.leard.leradlauncher RUN_IN_BACKGROUND"
```

如果某个包每次重启都被厂商服务重新启用，记录下来，不要反复和厂商启动逻辑对抗。

## 10. 恢复

### 恢复当贝OS后台

```powershell
powershell -ExecutionPolicy Bypass -File scripts/restore-dangbei-background.ps1 -TvHost $env:TV_HOST -MakeDefault
```

### ADB 端口丢失

在投影仪上安装并打开 `adb/ADBRecovery.apk`，等待约 10 秒，再从电脑运行：

```powershell
python -I tools/tv.py --probe
```

### 回到原桌面

先确保原桌面包处于启用状态，再：

```powershell
python -I tools/tv.py "cmd package set-home-activity --user 0 com.dangbei.leard.leradlauncher/com.dangbei.launcher.home.LauncherActivity"
```

如果厂商组件名与你的固件不同，先用 `dumpsys package` 查出真实 Activity。

## 11. 让 Agent 保持克制的提示词

见 [`prompts/adapt-device.zh-CN.md`](../prompts/adapt-device.zh-CN.md)。最短版本：

```text
请读取 AGENTS.md 和 docs/SAFETY.md。先只读探测我的哈趣 Q1 Pro，保存完整基线备份，列出 Home、包列表、已停用包和未知包。不要停用、卸载或启动工厂菜单。任何改动前先向我说明目的、风险和恢复命令，并等我确认。每次只改一个包，然后截图和复查。
```

## 12. 常见问题

### 装完设置 APK 仍打不开安卓设置

这很常见。厂商可能没有导出标准 Settings Activity，或者把入口交给自己的设置应用。先查：

```powershell
python -I tools/tv.py "dumpsys package com.android.settings | grep -A 3 'android.intent.action.SETTINGS'"
python -I tools/tv.py "cmd package query-activities -a android.settings.SETTINGS"
```

### Home 选择器总是弹出

通常是有多个 Home claimant，或原桌面被停用后没有恢复。先查：

```powershell
python -I tools/tv.py "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
```

不要在没有确认原桌面 Activity 的情况下继续停用组件。

### HDMI 显示无信号

没有接信号源时这是正常的。先确认输入源 Activity 和 TIF input ID，再检查是否进入了厂商的浮动“无信号”窗口。

### 投影仪重启后 ADB 不见了

先检查 ADB 控制器是否仍启用。如果它被停用，使用 `adb/ADBRecovery.apk`，不要直接恢复出厂设置。

### 语言切换后首页还是旧语言

本仓库 2.8.7 已修复该问题。确认安装的是 `versionCode 21` 或更新版本，并检查 `pm install -r` 是否成功覆盖旧版本。

## 最后

设备适配的难点通常不是“找到一个能点的按钮”，而是确认：

1. 这个按钮属于谁；
2. 它会不会影响启动、输入源、网络或遥控器；
3. 出问题时怎么恢复。

让 Agent 按证据和恢复路径工作，比让它追求一次性的“成功”更重要。
