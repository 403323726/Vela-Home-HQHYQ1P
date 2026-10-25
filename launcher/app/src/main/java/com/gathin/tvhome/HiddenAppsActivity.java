package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** TV-friendly list for hiding and restoring apps without changing package state. */
public final class HiddenAppsActivity extends LocalizedActivity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final List<AppRow> rows = new ArrayList<>();
    private HiddenAdapter adapter;
    private TextView count;
    private TextView empty;

    private static final class AppRow {
        final String label;
        final String pkg;
        final Drawable icon;
        boolean hidden;

        AppRow(String label, String pkg, Drawable icon, boolean hidden) {
            this.label = label;
            this.pkg = pkg;
            this.icon = icon;
            this.hidden = hidden;
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_hidden_apps);
        count = findViewById(R.id.hiddenAppsCount);
        empty = findViewById(R.id.hiddenAppsEmpty);
        RecyclerView list = findViewById(R.id.hiddenAppsList);
        list.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        list.setItemAnimator(null);
        list.setHasFixedSize(true);
        adapter = new HiddenAdapter();
        list.setAdapter(adapter);
        loadApps(list);
    }

    private void loadApps(RecyclerView list) {
        PackageManager pm = getPackageManager();
        String self = getPackageName();
        Set<String> fixedHidden = new HashSet<>(Config.load(this).hiddenApps);
        Collections.addAll(fixedHidden, getResources().getStringArray(R.array.system_folder_hidden_apps));
        loader.execute(() -> {
            Map<String, ResolveInfo> found = new LinkedHashMap<>();
            for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
                for (ResolveInfo info : pm.queryIntentActivities(
                        new Intent(Intent.ACTION_MAIN).addCategory(category), 0)) {
                    if (info.activityInfo == null || info.activityInfo.applicationInfo == null
                            || !info.activityInfo.exported) continue;
                    String pkg = info.activityInfo.packageName;
                    if (pkg.equals(self) || HiddenApps.isProtected(this, pkg)
                            || fixedHidden.contains(pkg) || found.containsKey(pkg)) continue;
                    found.put(pkg, info);
                }
            }
            Set<String> hidden = HiddenApps.load(this);
            List<AppRow> loaded = new ArrayList<>();
            for (ResolveInfo info : found.values()) {
                String pkg = info.activityInfo.packageName;
                String label;
                try {
                    label = Cards.cleanLabel(String.valueOf(info.loadLabel(pm)), pkg);
                } catch (RuntimeException unavailable) {
                    label = pkg;
                }
                Drawable icon;
                try {
                    icon = info.loadIcon(pm);
                } catch (RuntimeException unavailable) {
                    icon = null;
                }
                loaded.add(new AppRow(label, pkg, icon, hidden.contains(pkg)));
            }
            Collator collator = Collator.getInstance(Locale.getDefault());
            loaded.sort((a, b) -> {
                if (a.hidden != b.hidden) return a.hidden ? -1 : 1;
                int byLabel = collator.compare(a.label, b.label);
                return byLabel != 0 ? byLabel : a.pkg.compareTo(b.pkg);
            });
            main.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                rows.clear();
                rows.addAll(loaded);
                adapter.notifyDataSetChanged();
                updateCount();
                empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
                if (!rows.isEmpty()) {
                    list.post(() -> {
                        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(0);
                        if (holder != null) holder.itemView.requestFocus();
                    });
                }
            });
        });
    }

    private void updateCount() {
        count.setText(getString(R.string.hidden_apps_count, HiddenApps.count(this)));
    }

    private final class HiddenAdapter extends RecyclerView.Adapter<HiddenAdapter.Holder> {
        final class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView label;
            final TextView pkg;
            final TextView status;

            Holder(View view) {
                super(view);
                icon = view.findViewById(R.id.hiddenAppIcon);
                label = view.findViewById(R.id.hiddenAppLabel);
                pkg = view.findViewById(R.id.hiddenAppPackage);
                status = view.findViewById(R.id.hiddenAppStatus);
            }
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_hidden_app, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            AppRow app = rows.get(position);
            holder.icon.setImageDrawable(app.icon != null
                    ? app.icon : getDrawable(R.drawable.ic_apps));
            holder.label.setText(app.label);
            holder.pkg.setText(app.pkg);
            holder.status.setText(app.hidden ? R.string.hidden_apps_hidden : R.string.hidden_apps_visible);
            holder.status.setTextColor(app.hidden ? 0xFFB9E7C7 : 0xFFAFBBC5);
            holder.itemView.setSelected(app.hidden);
            holder.itemView.setOnClickListener(v -> {
                int at = holder.getBindingAdapterPosition();
                if (at == RecyclerView.NO_POSITION) return;
                AppRow selected = rows.get(at);
                boolean hide = !selected.hidden;
                if (!HiddenApps.setHidden(HiddenAppsActivity.this, selected.pkg, hide)) return;
                selected.hidden = hide;
                holder.itemView.setSelected(hide);
                holder.status.setText(hide ? R.string.hidden_apps_hidden : R.string.hidden_apps_visible);
                holder.status.setTextColor(hide ? 0xFFB9E7C7 : 0xFFAFBBC5);
                setResult(RESULT_OK, new Intent());
                updateCount();
            });
            holder.itemView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                return keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }

    @Override
    protected void onDestroy() {
        main.removeCallbacksAndMessages(null);
        loader.shutdownNow();
        super.onDestroy();
    }
}