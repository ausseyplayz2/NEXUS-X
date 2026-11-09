package com.nexusx.launcher;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root;
    LinearLayout content;
    EditText search;
    boolean drawer = false;

    int cyan = Color.rgb(0, 220, 255);
    int purple = Color.rgb(165, 75, 255);

    GradientDrawable background() {
        GradientDrawable g = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF080D25, 0xFF171044, 0xFF050916}
        );
        g.setCornerRadius(0);
        return g;
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        return t;
    }

    GradientDrawable panel() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(0xCC151C3B);
        g.setCornerRadius(24);
        g.setStroke(2, 0xFF2866B9);
        return g;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(0xFF080D25);
        getWindow().setNavigationBarColor(0xFF080D25);
        getWindow().getDecorView().setSystemUiVisibility(0);

        showHome();
    }

    void setup() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 25, 20, 15);
        root.setBackground(background());
        setContentView(root);
    }


    void showHome() {
        drawer = false;
        setup();

        TextView logo = text("✦ N E X U S  X ✦", 25, cyan);
        root.addView(logo, new LinearLayout.LayoutParams(-1, 55));

        TextView subtitle = text("GAMING COMMAND CENTER", 10, 0xFFB18CFF);
        root.addView(subtitle);

        TextView clock = text("", 45, Color.WHITE);
        root.addView(clock, new LinearLayout.LayoutParams(-1, 65));

        TextView date = text(
            new java.text.SimpleDateFormat(
                "EEEE, MMMM dd", Locale.getDefault()
            ).format(new Date()), 13, 0xFFB9C7E8
        );
        root.addView(date);

        new Handler().post(new Runnable() {
            public void run() {
                if (!drawer && clock.getParent() != null) {
                    clock.setText(new java.text.SimpleDateFormat(
                        "HH:mm", Locale.getDefault()
                    ).format(new Date()));
                    new Handler().postDelayed(this, 1000);
                }
            }
        });

        TextView status = text(
            "◈ SYSTEM ONLINE  •  SELECT AN APP",
            11, cyan
        );
        LinearLayout.LayoutParams sp =
            new LinearLayout.LayoutParams(-1, 45);
        sp.topMargin = 8;
        root.addView(status, sp);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);

        LinearLayout appGrid = new LinearLayout(this);
        appGrid.setOrientation(LinearLayout.VERTICAL);
        appGrid.setPadding(0, 8, 0, 8);

        scroll.addView(appGrid);

        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        List<ResolveInfo> apps = getLaunchableApps();
        PackageManager pm = getPackageManager();

        int columns = 5;
        int rowsPerPage = 6;

        for (int i = 0; i < apps.size(); i += columns) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = i; j < Math.min(i + columns, apps.size()); j++) {
                addAppTile(row, apps.get(j), pm, 85);
            }

            for (int j = Math.min(i + columns, apps.size());
                 j < i + columns; j++) {
                row.addView(new View(this),
                    new LinearLayout.LayoutParams(0, 85, 1));
            }

            appGrid.addView(row);
        }

        // Bottom dock: first four installed apps
        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setBackground(panel());
        dock.setPadding(4, 5, 4, 5);

        for (int i = 0; i < Math.min(4, apps.size()); i++) {
            addAppTile(dock, apps.get(i), pm, 75);
        }

        LinearLayout.LayoutParams dp =
            new LinearLayout.LayoutParams(-1, 85);
        dp.topMargin = 8;
        root.addView(dock, dp);
    }

    List<ResolveInfo> getLaunchableApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        Collections.sort(apps, (a, b) ->
            a.loadLabel(pm).toString().compareToIgnoreCase(
                b.loadLabel(pm).toString()
            )
        );

        return apps;
    }

    void addAppTile(LinearLayout parent, ResolveInfo app,
                    PackageManager pm, int height) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(2, 4, 2, 4);
        tile.setClickable(true);
        tile.setFocusable(true);

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.loadIcon(pm));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        tile.addView(icon, new LinearLayout.LayoutParams(42, 42));

        TextView label = text(
            app.loadLabel(pm).toString(), 10, Color.WHITE
        );
        label.setMaxLines(1);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);

        tile.addView(label, new LinearLayout.LayoutParams(
            -1, 25
        ));

        parent.addView(tile, new LinearLayout.LayoutParams(
            0, height, 1
        ));

        tile.setOnClickListener(v -> launchApp(app));
    }

    void launchApp(ResolveInfo app) {
        try {
            Intent launch = new Intent(Intent.ACTION_MAIN);
            launch.addCategory(Intent.CATEGORY_LAUNCHER);
            launch.setComponent(new ComponentName(
                app.activityInfo.packageName,
                app.activityInfo.name
            ));
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open app",
                Toast.LENGTH_SHORT).show();
        }
    }

    TextView button(String label) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setClickable(true);
        b.setFocusable(true);
        b.setBackground(panel());

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, 65);
        p.setMargins(0, 10, 0, 10);

        root.addView(b, p);
        return b;
    }

    void showDrawer() {
        drawer = true;
        setup();

        TextView title = text("✦ APP LIBRARY", 24, cyan);
        root.addView(title, new LinearLayout.LayoutParams(-1, 65));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Search installed apps...");
        search.setHintTextColor(0xFF8995B8);
        search.setTextColor(Color.WHITE);
        search.setBackground(panel());
        search.setPadding(18, 5, 18, 5);
        root.addView(search, new LinearLayout.LayoutParams(-1, 55));

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);

        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1));

        TextView back = button("← BACK TO HOME");
        back.setOnClickListener(v -> showHome());

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,
                int start, int count, int after) {}
            public void onTextChanged(CharSequence s,
                int start, int before, int count) {
                loadApps(s.toString());
            }
            public void afterTextChanged(Editable e) {}
        });

        loadApps("");
    }

    void loadApps(String query) {
        content.removeAllViews();

        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        Collections.sort(apps, (a, b) ->
            a.loadLabel(pm).toString().compareToIgnoreCase(
                b.loadLabel(pm).toString()));

        for (ResolveInfo app : apps) {
            String name = app.loadLabel(pm).toString();

            if (!name.toLowerCase().contains(
                query.toLowerCase())) continue;

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(12, 8, 12, 8);
            row.setBackground(panel());

            ImageView icon = new ImageView(this);
            icon.setImageDrawable(app.loadIcon(pm));
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(icon, new LinearLayout.LayoutParams(55, 55));

            TextView label = text(name, 15, Color.WHITE);
            label.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(label, new LinearLayout.LayoutParams(
                0, 55, 1));

            LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, 68);
            p.setMargins(0, 5, 0, 5);
            content.addView(row, p);

            row.setOnClickListener(v -> {
                Intent launch = new Intent();
                launch.setComponent(new ComponentName(
                    app.activityInfo.packageName,
                    app.activityInfo.name));
                try {
                    startActivity(launch);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open app",
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer) showHome();
        else super.onBackPressed();
    }
}
