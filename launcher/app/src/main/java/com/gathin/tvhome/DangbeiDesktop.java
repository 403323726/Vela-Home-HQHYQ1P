package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/** Read-only status for the disabled Dangbei OS launcher Activity. */
final class DangbeiDesktop {
    static final String PACKAGE = "com.dangbei.leard.leradlauncher";
    static final String ACTIVITY = "com.dangbei.leard.leradlauncher/com.dangbei.launcher.home.LauncherActivity";
    static final ComponentName COMPONENT = ComponentName.unflattenFromString(ACTIVITY);

    private DangbeiDesktop() {
    }

    static boolean installed(Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException unavailable) {
            return false;
        }
    }

    static boolean enabled(Context context) {
        if (!installed(context)) return false;
        PackageManager pm = context.getPackageManager();
        int packageState = pm.getApplicationEnabledSetting(PACKAGE);
        if (packageState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || packageState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || packageState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) return false;
        int componentState = pm.getComponentEnabledSetting(COMPONENT);
        return componentState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || componentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    static String stateText(Context context) {
        if (!installed(context)) return context.getString(R.string.dangbei_desktop_not_installed);
        return context.getString(enabled(context)
                ? R.string.dangbei_desktop_enabled : R.string.dangbei_desktop_disabled);
    }
}