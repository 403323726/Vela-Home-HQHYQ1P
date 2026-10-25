package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/** App-wide language preference, independent of the TV's system locale. */
final class LocaleHelper {
    static final String ZH = "zh";
    static final String EN = "en";
    private static final String KEY = "app_language";

    private LocaleHelper() {
    }

    static String language(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                Config.SETTINGS_NAME, Context.MODE_PRIVATE);
        String saved = preferences.getString(KEY, null);
        if (ZH.equals(saved) || EN.equals(saved)) return saved;
        String system = Locale.getDefault().getLanguage();
        return system != null && system.startsWith("zh") ? ZH : EN;
    }

    static void setLanguage(Context context, String language) {
        if (!ZH.equals(language) && !EN.equals(language)) return;
        context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY, language).apply();
        Locale.setDefault(locale(language));
    }

    static Context wrap(Context context) {
        Locale locale = locale(language(context));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    private static Locale locale(String language) {
        return EN.equals(language) ? Locale.ENGLISH : Locale.SIMPLIFIED_CHINESE;
    }
}