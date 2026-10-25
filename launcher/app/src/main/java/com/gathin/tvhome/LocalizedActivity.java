package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;

/** Applies the in-app language before any screen inflates its resources. */
abstract class LocalizedActivity extends Activity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}