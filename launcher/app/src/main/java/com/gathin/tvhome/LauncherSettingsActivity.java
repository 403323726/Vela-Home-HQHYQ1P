package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/** Remote-friendly, two-column settings owned by the launcher. */
public final class LauncherSettingsActivity extends LocalizedActivity {
    static final String EXTRA_REORDER = "com.gathin.tvhome.extra.REORDER_FROM_SETTINGS";
    private static final String EXTRA_LANGUAGE_CHANGED =
            "com.gathin.tvhome.extra.LANGUAGE_CHANGED";
    private static final int HIDDEN_APPS_REQUEST = 43;
    private static final String SECTION_KEY = "settings_section";
    private SharedPreferences choices;
    private final TextView[] columnChoices = new TextView[4];
    private final TextView[] rowChoices = new TextView[4];
    private static final int[] DOCK_OPACITIES = {100, 70, 50, 30};
    private final TextView[] dockChoices = new TextView[DOCK_OPACITIES.length];
    private TextView[] navigation;
    private View[] sections;
    private TextView wallpaperChoice;
    private TextView animationsChoice;
    private TextView advancedChoice;
    private TextView followThemeChoice;
    private TextView hiddenAppsChoice;
    private TextView dangbeiDesktopChoice;
    private TextView languageChinese;
    private TextView languageEnglish;
    private int columns;
    private int rowsBelowDock;
    private int dockOpacity;
    private int selectedSection;
    private boolean animations;
    private boolean advancedEffects;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_launcher_settings);
        // recreate() keeps this Intent but drops setResult(), so restore the pending result that
        // tells Home to rebuild itself with the newly selected language.
        if (getIntent().getBooleanExtra(EXTRA_LANGUAGE_CHANGED, false)) setResult(RESULT_OK);
        choices = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        Config config = Config.load(this);
        setupNavigation();

        View reorderChoice = findViewById(R.id.reorderChoice);
        reorderChoice.setVisibility(config.launcherSettingsButton ? View.VISIBLE : View.GONE);
        reorderChoice.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_REORDER, true));
            finish();
        });
        columns = config.columns;
        rowsBelowDock = config.rowsBelowDock;
        dockOpacity = config.dockOpacity;
        animations = config.animations;
        advancedEffects = config.animations && config.advancedEffects;

        int[] ids = {R.id.columns4, R.id.columns5, R.id.columns6, R.id.columns7};
        for (int i = 0; i < ids.length; i++) {
            final int value = i + 4;
            columnChoices[i] = findViewById(ids[i]);
            columnChoices[i].setOnClickListener(v -> {
                if (columns == value) return;
                columns = value;
                choices.edit().putInt("columns", value).apply();
                setResult(RESULT_OK);
                refresh();
            });
        }
        int[] rowIds = {R.id.rows0, R.id.rows1, R.id.rows2, R.id.rows3};
        for (int i = 0; i < rowIds.length; i++) {
            final int value = i;
            rowChoices[i] = findViewById(rowIds[i]);
            rowChoices[i].setOnClickListener(v -> {
                if (rowsBelowDock == value) return;
                rowsBelowDock = value;
                choices.edit().putInt(Config.ROWS_BELOW_DOCK_KEY, value).apply();
                setResult(RESULT_OK);
                refresh();
            });
        }
        int[] dockIds = {R.id.dock100, R.id.dock70, R.id.dock50, R.id.dock30};
        for (int i = 0; i < dockIds.length; i++) {
            final int value = DOCK_OPACITIES[i];
            dockChoices[i] = findViewById(dockIds[i]);
            dockChoices[i].setOnClickListener(v -> {
                if (dockOpacity == value) return;
                dockOpacity = value;
                choices.edit().putInt(Config.DOCK_OPACITY_KEY, value).apply();
                setResult(RESULT_OK);
                refresh();
            });
        }
        if (getResources().getBoolean(R.bool.rtl_app_grid)) {
            reverseRow(columnChoices);
            reverseRow(rowChoices);
            reverseRow(dockChoices);
        }

        wallpaperChoice = findViewById(R.id.wallpaperChoice);
        wallpaperChoice.setOnClickListener(v -> showWallpaperPicker());
        animationsChoice = findViewById(R.id.animationsChoice);
        animationsChoice.setOnClickListener(v -> {
            animations = !animations;
            if (!animations) advancedEffects = false;
            choices.edit().putBoolean("animations", animations)
                    .putBoolean("advanced_effects", advancedEffects).apply();
            setResult(RESULT_OK);
            refresh();
        });
        advancedChoice = findViewById(R.id.advancedChoice);
        advancedChoice.setOnClickListener(v -> {
            if (advancedEffects) {
                setAdvanced(false);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.advanced_effects)
                        .setMessage(R.string.advanced_effects_warning)
                        .setPositiveButton(R.string.enable, (dialog, which) -> setAdvanced(true))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        followThemeChoice = findViewById(R.id.followThemeChoice);
        followThemeChoice.setOnClickListener(v -> {
            if (!hasOnTvChoices()) return;
            choices.edit().remove("columns").remove(Config.ROWS_BELOW_DOCK_KEY).remove("animations")
                    .remove("advanced_effects").remove(Config.WALLPAPER_CHOICE_KEY)
                    .remove(Config.DOCK_OPACITY_KEY).commit();
            Config current = Config.load(this);
            columns = current.columns;
            rowsBelowDock = current.rowsBelowDock;
            dockOpacity = current.dockOpacity;
            animations = current.animations;
            advancedEffects = current.animations && current.advancedEffects;
            setResult(RESULT_OK);
            refresh();
        });
        hiddenAppsChoice = findViewById(R.id.hiddenAppsChoice);
        hiddenAppsChoice.setOnClickListener(v -> startActivityForResult(
                new Intent(this, HiddenAppsActivity.class), HIDDEN_APPS_REQUEST));
        dangbeiDesktopChoice = findViewById(R.id.dangbeiDesktopChoice);
        dangbeiDesktopChoice.setOnClickListener(v -> showDangbeiRecovery());
        findViewById(R.id.tvSettingsChoice).setOnClickListener(v ->
                open(SystemSettings.intent(this, Settings.ACTION_SETTINGS)));
        languageChinese = findViewById(R.id.languageChinese);
        languageEnglish = findViewById(R.id.languageEnglish);
        languageChinese.setOnClickListener(v -> chooseLanguage(LocaleHelper.ZH));
        languageEnglish.setOnClickListener(v -> chooseLanguage(LocaleHelper.EN));

        refresh();
        selectedSection = state != null ? state.getInt(SECTION_KEY, 0)
                : choices.getInt(SECTION_KEY, 0);
        selectedSection = Math.max(0, Math.min(selectedSection, navigation.length - 1));
        selectSection(selectedSection, false);
        navigation[selectedSection].requestFocus();
    }

    private void setupNavigation() {
        int[] navIds = {R.id.navLayout, R.id.navAppearance, R.id.navApps, R.id.navSystem,
                R.id.navLanguage};
        int[] sectionIds = {R.id.sectionLayout, R.id.sectionAppearance, R.id.sectionApps,
                R.id.sectionSystem, R.id.sectionLanguage};
        navigation = new TextView[navIds.length];
        sections = new View[sectionIds.length];
        for (int i = 0; i < navIds.length; i++) {
            final int index = i;
            navigation[i] = findViewById(navIds[i]);
            sections[i] = findViewById(sectionIds[i]);
            navigation[i].setOnFocusChangeListener((v, focused) -> {
                if (focused) selectSection(index, false);
            });
            navigation[i].setOnClickListener(v -> selectSection(index, true));
            navigation[i].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    selectSection(index, true);
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP && index == 0
                        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN && index == navigation.length - 1) {
                    return true;
                }
                return false;
            });
        }
    }

    private void selectSection(int index, boolean focusContent) {
        selectedSection = Math.max(0, Math.min(index, sections.length - 1));
        for (int i = 0; i < sections.length; i++) {
            navigation[i].setSelected(i == selectedSection);
            sections[i].setVisibility(i == selectedSection ? View.VISIBLE : View.GONE);
        }
        choices.edit().putInt(SECTION_KEY, selectedSection).apply();
        setNextFocusLeft(sections[selectedSection], navigation[selectedSection].getId());
        if (focusContent) {
            View first = firstFocusable(sections[selectedSection]);
            if (first != null) first.requestFocus();
        }
    }

    private static View firstFocusable(View root) {
        if (root == null || root.getVisibility() != View.VISIBLE) return null;
        // A ScrollView can itself be focusable; prefer its first real control instead of parking on it.
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = firstFocusable(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return root.isFocusable() ? root : null;
    }

    private static void setNextFocusLeft(View root, int navId) {
        if (root == null) return;
        if (root.isFocusable()) root.setNextFocusLeftId(navId);
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) setNextFocusLeft(group.getChildAt(i), navId);
        }
    }

    private void reverseRow(TextView[] views) {
        LinearLayout row = (LinearLayout) views[0].getParent();
        row.removeAllViews();
        for (int i = views.length - 1; i >= 0; i--) row.addView(views[i]);
    }

    private void chooseLanguage(String language) {
        if (language.equals(LocaleHelper.language(this))) return;
        LocaleHelper.setLanguage(this, language);
        getIntent().putExtra(EXTRA_LANGUAGE_CHANGED, true);
        setResult(RESULT_OK);
        recreate();
    }

    private void showDangbeiRecovery() {
        String state = DangbeiDesktop.stateText(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dangbei_desktop_recovery_title)
                .setMessage(getString(R.string.dangbei_desktop_recovery_message,
                        state, DangbeiDesktop.PACKAGE))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showWallpaperPicker() {
        File dir = Config.dir(this);
        List<File> images = new java.util.ArrayList<>(WallpaperRotation.images(
                dir == null ? null : new File(dir, "wallpapers")));
        File single = dir == null ? null : new File(dir, Config.WALLPAPER_NAME);
        if (single != null && single.isFile()) images.add(0, single);
        String[] labels = new String[images.size() + 1];
        for (int i = 0; i < images.size(); i++) labels[i] = images.get(i).getName();
        labels[images.size()] = getString(R.string.wallpaper_none);
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        int checked = "none".equals(current) ? images.size() : indexOf(images, current);
        new AlertDialog.Builder(this)
                .setTitle(R.string.wallpaper_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == images.size()) {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, "none").apply();
                    } else {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY,
                                images.get(which).getName()).apply();
                    }
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.wallpaper_applied, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNeutralButton(R.string.wallpaper_theme_default, (dialog, which) -> {
                    choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();
                    setResult(RESULT_OK);
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static int indexOf(List<File> images, String name) {
        if (name == null) return -1;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    private void setAdvanced(boolean enabled) {
        advancedEffects = enabled;
        choices.edit().putBoolean("advanced_effects", enabled).apply();
        setResult(RESULT_OK);
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < columnChoices.length; i++) {
            columnChoices[i].setSelected(columns == i + 4);
        }
        for (int i = 0; i < rowChoices.length; i++) {
            rowChoices[i].setSelected(rowsBelowDock == i);
        }
        for (int i = 0; i < dockChoices.length; i++) {
            dockChoices[i].setSelected(dockOpacity == DOCK_OPACITIES[i]);
        }
        animationsChoice.setText(getString(R.string.animations_state,
                getString(animations ? R.string.on : R.string.off)));
        advancedChoice.setText(getString(R.string.advanced_effects_state,
                getString(advancedEffects ? R.string.on : R.string.off)));
        advancedChoice.setEnabled(animations);
        advancedChoice.setAlpha(animations ? 1f : 0.5f);
        String wallpaperCurrent = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        wallpaperChoice.setText(getString(R.string.wallpaper_state,
                "none".equals(wallpaperCurrent) ? getString(R.string.wallpaper_none)
                        : wallpaperCurrent != null ? wallpaperCurrent
                        : getString(R.string.wallpaper_theme_default)));
        followThemeChoice.setSelected(!hasOnTvChoices());
        hiddenAppsChoice.setText(getString(R.string.hidden_apps_manage_count,
                HiddenApps.count(this)));
        dangbeiDesktopChoice.setText(getString(R.string.dangbei_desktop_state,
                DangbeiDesktop.stateText(this)));
        String language = LocaleHelper.language(this);
        languageChinese.setSelected(LocaleHelper.ZH.equals(language));
        languageEnglish.setSelected(LocaleHelper.EN.equals(language));
    }

    private boolean hasOnTvChoices() {
        return choices.contains("columns") || choices.contains(Config.ROWS_BELOW_DOCK_KEY)
                || choices.contains("animations") || choices.contains("advanced_effects")
                || choices.contains(Config.WALLPAPER_CHOICE_KEY) || choices.contains(Config.DOCK_OPACITY_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HIDDEN_APPS_REQUEST && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            refresh();
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra(EXTRA_LANGUAGE_CHANGED, false)) setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putInt(SECTION_KEY, selectedSection);
        super.onSaveInstanceState(state);
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        }
    }
}