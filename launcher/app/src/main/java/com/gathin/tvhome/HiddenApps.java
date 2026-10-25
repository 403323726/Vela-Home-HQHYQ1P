package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** User-controlled apps hidden from Home and the system apps folder. */
final class HiddenApps {
    private static final String KEY = "user_hidden_apps";
    /** The ADB controller must remain reachable, so it cannot be hidden by this screen. */
    static final String ADB_CONTROLLER_PACKAGE = "jiujiutv.jiujiu.adb";

    private HiddenApps() {
    }

    static Set<String> load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                Config.SETTINGS_NAME, Context.MODE_PRIVATE);
        Set<String> saved = preferences.getStringSet(KEY, null);
        return saved == null ? new HashSet<>() : new HashSet<>(saved);
    }

    static boolean isProtected(Context context, String pkg) {
        return pkg == null || pkg.equals(context.getPackageName())
                || pkg.equals(ADB_CONTROLLER_PACKAGE);
    }

    static boolean setHidden(Context context, String pkg, boolean hidden) {
        if (isProtected(context, pkg)) return false;
        Set<String> saved = load(context);
        boolean changed = hidden ? saved.add(pkg) : saved.remove(pkg);
        if (changed) {
            context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE)
                    .edit().putStringSet(KEY, new HashSet<>(saved)).apply();
        }
        return true;
    }

    static int count(Context context) {
        return load(context).size();
    }
}