# Safety / 安全说明

[简体中文](#简体中文) | [English](#english)

## 简体中文

### 先备份

在修改任何包或 Home 之前，保存：

- `pm list packages`
- `pm list packages -d`
- `getprop`
- `wm size`
- `wm density`
- 当前 Home Activity
- 重要包的 `dumpsys package` 输出

备份可能包含设备名或序列号，只保存在本地。

### 不要卸载系统应用

使用 `pm disable-user --user 0` 也只应在确认包用途、风险、恢复命令和用户同意之后进行。不要使用 `pm uninstall` 处理系统包。

### 必须保留的核心组件

不要停用：

- `com.android.*`、`com.google.android.*` 核心组件
- 权限控制器、包安装器、ExtServices、ExtShared、模块元数据
- `com.android.systemui`
- `com.android.shell`
- `com.android.phone`
- `com.android.settings`
- `com.android.tv.settings`
- 系统 Provider
- 输入源、HDMI、调谐器、遥控器、蓝牙配对、输入法、网络服务

### 工厂/测试菜单

不要启动工厂、生产、AT、工程或测试菜单，即使只是“看看”。某些主板会写入不可逆的工厂状态，影响输入源、CEC、Play Services 或后续启动。

### Home 保护

原桌面只有在以下条件全部满足后才能考虑停用：

1. 新桌面已成功安装。
2. 新桌面能单独启动。
3. `set-home-activity` 成功。
4. Home 解析到新桌面。
5. 从应用按 Home 能回到新桌面。
6. 原桌面仍可重新启用和设为 Home。

在哈趣 Q1 Pro 的参考固件上，当贝OS桌面 Activity 不能单独停用，保持包启用即可。

### ADB

- `5555` 是明文端口。
- 只在可信局域网开启。
- 不要映射到公网。
- 授权提示必须由设备所有者本人确认。
- 不要把 ADB 私钥、备份、真实 IP 或 Wi-Fi 名提交到仓库。

### 出问题时

1. 停止继续修改。
2. 记录最后的成功状态和失败命令。
3. 恢复原 Home。
4. 重新启用最近停用的包。
5. 检查 ADB 控制器是否仍启用。
6. 如果 ADB 丢失，使用 `adb/ADBRecovery.apk`。
7. 不要因为一时无法连接就直接恢复出厂设置。

---

## English

### Back up first

Before changing a package or Home, save:

- `pm list packages`
- `pm list packages -d`
- `getprop`
- `wm size`
- `wm density`
- Current Home activity
- `dumpsys package` output for important packages

Backups can contain device names or serial numbers. Keep them local.

### Do not uninstall system packages

Use `pm disable-user --user 0` only after confirming the package purpose, risk, undo command, and owner approval. Do not use `pm uninstall` for system packages.

### Keep core components enabled

Do not disable:

- Core `com.android.*` and `com.google.android.*` components
- Permission controller, package installer, ExtServices, ExtShared, and module metadata
- `com.android.systemui`
- `com.android.shell`
- `com.android.phone`
- `com.android.settings`
- `com.android.tv.settings`
- System providers
- Input, HDMI, tuner, remote, Bluetooth pairing, keyboard, and networking services

### Factory and test menus

Do not launch factory, production, AT, engineering, or test menus, even just to “look.” Some boards persist a factory-mode flag that can break input, CEC, Play Services, or boot.

### Home guard

Only consider disabling the stock launcher after all of these are true:

1. The new launcher installed successfully.
2. The new launcher starts on its own.
3. `set-home-activity` succeeds.
4. Home resolves to the new launcher.
5. Home from an app returns to the new launcher.
6. The stock launcher can still be enabled and set as Home.

On the HaQu Q1 Pro reference firmware, the Dangbei OS launcher Activity cannot be disabled individually. Keep the package enabled.

### ADB

- Port `5555` is plaintext.
- Enable it only on a trusted LAN.
- Never expose it to the public internet.
- The device owner must accept the authorization prompt.
- Do not commit ADB private keys, backups, real IP addresses, or Wi-Fi names.

### When something goes wrong

1. Stop changing more components.
2. Record the last known-good state and the failing command.
3. Restore the stock Home.
4. Re-enable the most recently disabled package.
5. Check whether the ADB controller is still enabled.
6. If ADB is gone, use `adb/ADBRecovery.apk`.
7. Do not jump to a factory reset just because ADB is temporarily unavailable.
