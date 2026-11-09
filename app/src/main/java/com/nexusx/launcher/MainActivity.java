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

        TextView logo = text("✦ N E X U S  X ✦", 27, cyan);
        root.addView(logo, new LinearLayout.LayoutParams(-1, 75));

        TextView subtitle = text("GAMING COMMAND CENTER", 11, 0xFFB18CFF);
        root.addView(subtitle);

        root.addView(new Space(this), new LinearLayout.LayoutParams(1, 65));

        TextView clock = text("", 52, Color.WHITE);
        root.addView(clock);

        TextView date = text(
            new java.text.SimpleDateFormat("EEEE, MMMM dd",
            Locale.getDefault()).format(new Date()),
            15, 0xFFB9C7E8
        );
        root.addView(date);

        new Handler().post(new Runnable() {
            public void run() {
                if (!drawer && clock.getParent() != null) {
                    clock.setText(new java.text.SimpleDateFormat(
                        "HH:mm", Locale.getDefault()).format(new Date()));
                    new Handler().postDelayed(this, 1000);
                }
            }
        });

        root.addView(new Space(this),
            new LinearLayout.LayoutParams(1, 65));

        TextView status = text("◈ SYSTEM ONLINE  •  NEXUS CORE",
            13, cyan);
        root.addView(status);

        root.addView(new Space(this),
            new LinearLayout.LayoutParams(1, 35));

        TextView apps = button("✦  OPEN APP LIBRARY");
        apps.setOnClickListener(v -> showDrawer());

        TextView settings = button("⚙  LAUNCHER SETTINGS");
        settings.setOnClickListener(v ->
            Toast.makeText(this, "More customization coming soon!",
            Toast.LENGTH_SHORT).show());

        root.addView(new Space(this),
            new LinearLayout.LayoutParams(1, 0, 1));

        root.addView(text("NEXUS X  •  VERSION 1.0", 11,
            0xFF7886B0));
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
