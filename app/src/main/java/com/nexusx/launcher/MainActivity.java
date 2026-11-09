package com.nexusx.launcher;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {

    FrameLayout shell;
    LinearLayout root;
    LinearLayout content;
    EditText search;
    TextView activeClock;

    boolean pinnedPage = false;

    final String PREFS = "nexus_preferences";
    final String PINNED = "pinned_apps";

    int cyan = Color.rgb(0, 220, 255);
    int purple = Color.rgb(165, 75, 255);

    GradientDrawable background() {
        GradientDrawable g = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{
                0xFF030817,
                0xFF111A42,
                0xFF28104B,
                0xFF050916
            }
        );
        return g;
    }

    GradientDrawable panel() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(0x665D64A8);
        g.setCornerRadius(20);
        g.setStroke(1, 0x995B9EFF);
        return g;
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create(
            "sans-serif", Typeface.BOLD
        ));
        return t;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(0xFF050916);
        getWindow().setNavigationBarColor(0xFF050916);

        showHome();
    }

    void setup() {
        shell = new FrameLayout(this);
        shell.setBackground(background());

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(12, 10, 26, 10);
        root.setBackground(background());

        shell.addView(root, new FrameLayout.LayoutParams(
            -1, -1
        ));

        setContentView(shell);

        addSideHandle();
    }

    void addSideHandle() {
        TextView handle = text(
            pinnedPage ? "›" : "‹",
            32,
            Color.WHITE
        );

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(0xFFEC347D);
        shape.setCornerRadius(24);
        shape.setStroke(2, 0xFFFF8DB5);
        handle.setBackground(shape);
        handle.setElevation(12);

        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(38, 94);

        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        params.rightMargin = 2;

        shell.addView(handle, params);

        handle.setOnClickListener(v -> {
            if (pinnedPage) {
                showHome();
            } else {
                showPinned();
            }
        });

        final float[] startX = new float[1];

        handle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    startX[0] = event.getRawX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    return true;

                case MotionEvent.ACTION_UP:
                    float dx = event.getRawX() - startX[0];

                    if (Math.abs(dx) > 25) {
                        if (dx < 0 && !pinnedPage) {
                            showPinned();
                        } else if (dx > 0 && pinnedPage) {
                            showHome();
                        }
                    } else {
                        handle.performClick();
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    return true;
            }

            return true;
        });
    }

    void showHeader() {
        TextView logo = text(
            "✦ N E X U S  X ✦",
            23,
            cyan
        );

        root.addView(logo,
            new LinearLayout.LayoutParams(-1, 38));

        TextView subtitle = text(
            "GAMING COMMAND CENTER",
            10,
            0xFFB18CFF
        );

        root.addView(subtitle,
            new LinearLayout.LayoutParams(-1, 20));

        TextView clock = text(
            "",
            43,
            Color.WHITE
        );

        activeClock = clock;

        root.addView(clock,
            new LinearLayout.LayoutParams(-1, 52));

        TextView date = text(
            new java.text.SimpleDateFormat(
                "EEEE, MMMM dd",
                Locale.getDefault()
            ).format(new Date()),
            12,
            0xFFB9C7E8
        );

        root.addView(date,
            new LinearLayout.LayoutParams(-1, 22));

        Handler handler = new Handler(Looper.getMainLooper());

        Runnable updateClock = new Runnable() {
            @Override
            public void run() {
                if (activeClock == clock) {
                    clock.setText(
                        new java.text.SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(new Date())
                    );

                    handler.postDelayed(this, 1000);
                }
            }
        };

        updateClock.run();

        TextView status = text(
            "◈ SYSTEM ONLINE  •  ALL APPLICATIONS",
            10,
            cyan
        );

        root.addView(status,
            new LinearLayout.LayoutParams(-1, 34));
    }

    void showHome() {
        pinnedPage = false;
        setup();
        showHeader();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 8, 0, 8);

        scroll.addView(content);

        root.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        populateGrid(content, getLaunchableApps());

        TextView dockLabel = text(
            "PINNED APPS",
            10,
            0xFFB18CFF
        );

        root.addView(dockLabel,
            new LinearLayout.LayoutParams(-1, 24));

        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setBackground(panel());
        dock.setPadding(3, 3, 3, 3);

        List<ResolveInfo> pinned = getPinnedApps();

        for (int i = 0; i < Math.min(5, pinned.size()); i++) {
            addAppTile(dock, pinned.get(i), 78);
        }

        if (pinned.isEmpty()) {
            TextView hint = text(
                "Long-press an app to pin it",
                11,
                0xFFB9C7E8
            );

            dock.addView(hint,
                new LinearLayout.LayoutParams(-1, 68));
        }

        root.addView(dock,
            new LinearLayout.LayoutParams(-1, 88));

        root.addView(text(
            "TAP OR PULL THE SIDE ARROW FOR PINNED APPS",
            9,
            cyan
        ));
    }

    void showPinned() {
        pinnedPage = true;
        setup();
        showHeader();

        TextView title = text(
            "✦ YOUR PINNED APPS",
            19,
            cyan
        );

        root.addView(title,
            new LinearLayout.LayoutParams(-1, 44));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xFFB0B9D5);
        search.setHint("⌕  Search pinned apps...");
        search.setTextSize(14);
        search.setPadding(15, 5, 15, 5);
        search.setBackground(panel());

        root.addView(search,
            new LinearLayout.LayoutParams(-1, 52));

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 8, 0, 8);

        scroll.addView(content);

        root.addView(scroll,
            new LinearLayout.LayoutParams(-1, 0, 1));

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(
                CharSequence s, int start, int count, int after
            ) {}

            public void onTextChanged(
                CharSequence s, int start, int before, int count
            ) {
                loadPinned(s.toString());
            }

            public void afterTextChanged(Editable e) {}
        });

        loadPinned("");

        root.addView(text(
            "TAP OR PULL THE SIDE ARROW TO RETURN HOME",
            9,
            cyan
        ));
    }

    List<ResolveInfo> getLaunchableApps() {
        PackageManager pm = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps =
            pm.queryIntentActivities(intent, 0);

        Collections.sort(apps, (a, b) ->
            a.loadLabel(pm).toString().compareToIgnoreCase(
                b.loadLabel(pm).toString()
            )
        );

        return apps;
    }

    void populateGrid(
        LinearLayout target,
        List<ResolveInfo> apps
    ) {
        target.removeAllViews();

        int columns = 5;

        for (int i = 0; i < apps.size(); i += columns) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = i;
                 j < Math.min(i + columns, apps.size()); j++) {
                addAppTile(row, apps.get(j), 128);
            }

            for (int j = Math.min(
                    i + columns, apps.size());
                 j < i + columns; j++) {
                row.addView(new View(this),
                    new LinearLayout.LayoutParams(0, 128, 1));
            }

            target.addView(row,
                new LinearLayout.LayoutParams(-1, 132));
        }
    }

    void addAppTile(
        LinearLayout parent,
        ResolveInfo app,
        int height
    ) {
        PackageManager pm = getPackageManager();

        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(2, 5, 2, 4);
        tile.setBackground(panel());
        tile.setClickable(true);
        tile.setFocusable(true);

        int iconSize = height >= 110 ? 62 : 40;
        int labelSize = height >= 110 ? 11 : 9;

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.loadIcon(pm));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        tile.addView(icon,
            new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView label = text(
            app.loadLabel(pm).toString(),
            labelSize,
            Color.WHITE
        );

        label.setMaxLines(2);
        label.setEllipsize(
            android.text.TextUtils.TruncateAt.END
        );

        tile.addView(label,
            new LinearLayout.LayoutParams(-1, 36));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(0, height, 1);

        params.setMargins(3, 3, 3, 3);

        parent.addView(tile, params);

        tile.setOnClickListener(v -> launchApp(app));

        tile.setOnLongClickListener(v -> {
            showAppMenu(app);
            return true;
        });
    }

    void showAppMenu(ResolveInfo app) {
        String name = app.loadLabel(
            getPackageManager()
        ).toString();

        boolean isPinned = isPinned(app);

        String[] options = {
            "Open",
            isPinned ? "Unpin" : "Pin",
            "App Information"
        };

        new AlertDialog.Builder(this)
            .setTitle(name)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    launchApp(app);
                } else if (which == 1) {
                    togglePin(app);
                } else if (which == 2) {
                    openAppInfo(app);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    void launchApp(ResolveInfo app) {
        try {
            Intent launch = new Intent(Intent.ACTION_MAIN);
            launch.addCategory(Intent.CATEGORY_LAUNCHER);

            launch.setComponent(new ComponentName(
                app.activityInfo.packageName,
                app.activityInfo.name
            ));

            startActivity(launch);

        } catch (Exception e) {
            Toast.makeText(this,
                "Cannot open app", Toast.LENGTH_SHORT).show();
        }
    }

    void openAppInfo(ResolveInfo app) {
        try {
            Intent intent = new Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            );

            intent.setData(Uri.parse(
                "package:" + app.activityInfo.packageName
            ));

            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this,
                "Cannot open app information",
                Toast.LENGTH_SHORT).show();
        }
    }

    String appKey(ResolveInfo app) {
        return new ComponentName(
            app.activityInfo.packageName,
            app.activityInfo.name
        ).flattenToString();
    }

    Set<String> getPinnedKeys() {
        return new HashSet<>(
            getSharedPreferences(PREFS, MODE_PRIVATE)
                .getStringSet(PINNED, new HashSet<>())
        );
    }

    boolean isPinned(ResolveInfo app) {
        return getPinnedKeys().contains(appKey(app));
    }

    void togglePin(ResolveInfo app) {
        Set<String> keys = getPinnedKeys();
        String key = appKey(app);

        if (keys.contains(key)) {
            keys.remove(key);
            Toast.makeText(this, "App unpinned",
                Toast.LENGTH_SHORT).show();
        } else {
            keys.add(key);
            Toast.makeText(this, "App pinned!",
                Toast.LENGTH_SHORT).show();
        }

        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putStringSet(PINNED, keys)
            .apply();

        if (pinnedPage) {
            loadPinned(search == null ? "" :
                search.getText().toString());
        } else {
            showHome();
        }
    }

    List<ResolveInfo> getPinnedApps() {
        Set<String> keys = getPinnedKeys();
        List<ResolveInfo> result = new ArrayList<>();

        for (ResolveInfo app : getLaunchableApps()) {
            if (keys.contains(appKey(app))) {
                result.add(app);
            }
        }

        return result;
    }

    void loadPinned(String query) {
        if (content == null) return;

        List<ResolveInfo> apps = getPinnedApps();
        List<ResolveInfo> filtered = new ArrayList<>();

        for (ResolveInfo app : apps) {
            String name = app.loadLabel(
                getPackageManager()
            ).toString();

            if (name.toLowerCase(Locale.ROOT)
                .contains(query.toLowerCase(Locale.ROOT))) {
                filtered.add(app);
            }
        }

        if (filtered.isEmpty()) {
            content.removeAllViews();

            TextView empty = text(
                apps.isEmpty()
                    ? "No pinned apps yet.\nLong-press an app on Home to pin it."
                    : "No matching pinned apps found.",
                14,
                0xFFB9C7E8
            );

            content.addView(empty,
                new LinearLayout.LayoutParams(-1, 150));

        } else {
            populateGrid(content, filtered);
        }
    }

    @Override
    public void onBackPressed() {
        if (pinnedPage) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }
}
