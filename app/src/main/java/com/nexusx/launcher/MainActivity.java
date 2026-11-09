package com.nexusx.launcher;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {

    FrameLayout shell;
    LinearLayout root, content;
    EditText search;
    TextView activeClock;
    boolean pinnedPage = false;

    final String PREFS = "nexus_preferences";
    final String PINNED = "pinned_apps";

    int cyan = Color.rgb(0, 220, 255);
    int purple = Color.rgb(165, 75, 255);

    int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable panel() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(0x443B72A8);
        g.setCornerRadius(dp(12));
        g.setStroke(dp(1), 0x6654CFFF);
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

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF030817);
        getWindow().setNavigationBarColor(0xFF030817);
        showHome();
    }

    void setup() {
        shell = new FrameLayout(this);

        CyberBackground space = new CyberBackground(this);
        shell.addView(space, new FrameLayout.LayoutParams(-1, -1));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(9), dp(8), dp(43), dp(8));
        root.setBackgroundColor(Color.TRANSPARENT);

        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));
        setContentView(shell);

        addSideHandle();
    }

    void addSideHandle() {
        SideHandle handle = new SideHandle(this);

        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(dp(43), dp(118));

        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        shell.addView(handle, params);

        final float[] startX = new float[1];

        handle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX[0] = event.getRawX();
                    return true;

                case MotionEvent.ACTION_UP:
                    float dx = event.getRawX() - startX[0];

                    if (Math.abs(dx) > dp(18)) {
                        if (dx < 0 && !pinnedPage) {
                            showPinned();
                        } else if (dx > 0 && pinnedPage) {
                            showHome();
                        }
                    } else {
                        if (pinnedPage) showHome();
                        else showPinned();
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    return true;
            }
            return true;
        });
    }

    void showHeader() {
        TextView logo = text("✦ N E X U S  X ✦", 23, cyan);
        root.addView(logo, new LinearLayout.LayoutParams(-1, dp(37)));

        TextView subtitle = text("GAMING COMMAND CENTER", 10, 0xFFB18CFF);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(19)));

        TextView clock = text("", 42, Color.WHITE);
        activeClock = clock;
        root.addView(clock, new LinearLayout.LayoutParams(-1, dp(51)));

        TextView date = text(
            new java.text.SimpleDateFormat(
                "EEEE, MMMM dd", Locale.getDefault()
            ).format(new Date()),
            12, 0xFFB9C7E8
        );
        root.addView(date, new LinearLayout.LayoutParams(-1, dp(21)));

        Handler handler = new Handler(Looper.getMainLooper());

        Runnable updateClock = new Runnable() {
            @Override
            public void run() {
                if (activeClock == clock) {
                    clock.setText(
                        new java.text.SimpleDateFormat(
                            "HH:mm", Locale.getDefault()
                        ).format(new Date())
                    );
                    handler.postDelayed(this, 1000);
                }
            }
        };
        updateClock.run();

        TextView status = text(
            pinnedPage ? "◈ PERSONAL APP COLLECTION" : "◈ SYSTEM ONLINE",
            10, cyan
        );
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(27)));
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
        content.setPadding(0, dp(5), 0, dp(5));

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        populateGrid(content, getLaunchableApps());

        TextView dockLabel = text("PINNED APPS", 10, 0xFFB18CFF);
        root.addView(dockLabel,
            new LinearLayout.LayoutParams(-1, dp(23)));

        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setBackground(panel());
        dock.setPadding(dp(3), dp(2), dp(3), dp(2));

        List<ResolveInfo> pinned = getPinnedApps();

        for (int i = 0; i < Math.min(5, pinned.size()); i++) {
            addAppTile(dock, pinned.get(i), 76);
        }

        if (pinned.isEmpty()) {
            TextView hint = text("Long-press an app to pin it", 11, 0xFFB9C7E8);
            dock.addView(hint,
                new LinearLayout.LayoutParams(-1, dp(65)));
        }

        root.addView(dock, new LinearLayout.LayoutParams(-1, dp(81)));
    }

    void showPinned() {
        pinnedPage = true;
        setup();
        showHeader();

        TextView title = text("✦ YOUR PINNED APPS", 18, cyan);
        root.addView(title,
            new LinearLayout.LayoutParams(-1, dp(39)));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xFFB0B9D5);
        search.setHint("⌕  Search pinned apps...");
        search.setTextSize(14);
        search.setPadding(dp(13), dp(4), dp(13), dp(4));
        search.setBackground(panel());

        root.addView(search,
            new LinearLayout.LayoutParams(-1, dp(48)));

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(7), 0, dp(7));

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start,
                int count, int after) {}

            public void onTextChanged(CharSequence s, int start,
                int before, int count) {
                loadPinned(s.toString());
            }

            public void afterTextChanged(Editable e) {}
        });

        loadPinned("");
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

    void populateGrid(LinearLayout target, List<ResolveInfo> apps) {
        target.removeAllViews();

        int columns = 4;

        for (int i = 0; i < apps.size(); i += columns) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = i; j < Math.min(i + columns, apps.size()); j++) {
                addAppTile(row, apps.get(j), 105);
            }

            for (int j = Math.min(i + columns, apps.size());
                 j < i + columns; j++) {
                row.addView(new View(this),
                    new LinearLayout.LayoutParams(0, dp(105), 1));
            }

            target.addView(row,
                new LinearLayout.LayoutParams(-1, dp(110)));
        }
    }

    void addAppTile(LinearLayout parent, ResolveInfo app, int height) {
        PackageManager pm = getPackageManager();

        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(2), dp(3), dp(2), dp(3));
        tile.setBackgroundColor(Color.TRANSPARENT);
        tile.setClickable(true);
        tile.setFocusable(true);

        int iconSize = height >= 100 ? 51 : 36;
        int labelSize = height >= 100 ? 11 : 9;

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.loadIcon(pm));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        tile.addView(icon,
            new LinearLayout.LayoutParams(dp(iconSize), dp(iconSize)));

        TextView label = text(
            app.loadLabel(pm).toString(), labelSize, Color.WHITE
        );
        label.setMaxLines(2);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);

        tile.addView(label,
            new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(0, dp(height), 1);

        params.setMargins(dp(1), dp(1), dp(1), dp(1));
        parent.addView(tile, params);

        tile.setOnClickListener(v -> launchApp(app));

        tile.setOnLongClickListener(v -> {
            showAppMenu(app);
            return true;
        });
    }

    void showAppMenu(ResolveInfo app) {
        String name = app.loadLabel(getPackageManager()).toString();
        boolean isPinned = isPinned(app);

        String[] options = {
            "Open",
            isPinned ? "Unpin" : "Pin",
            "App Information"
        };

        new AlertDialog.Builder(this)
            .setTitle(name)
            .setItems(options, (dialog, which) -> {
                if (which == 0) launchApp(app);
                else if (which == 1) togglePin(app);
                else if (which == 2) openAppInfo(app);
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
            Toast.makeText(this, "Cannot open app",
                Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Cannot open app information",
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
            .edit().putStringSet(PINNED, keys).apply();

        if (pinnedPage) {
            loadPinned(search == null ? "" : search.getText().toString());
        } else {
            showHome();
        }
    }

    List<ResolveInfo> getPinnedApps() {
        Set<String> keys = getPinnedKeys();
        List<ResolveInfo> result = new ArrayList<>();

        for (ResolveInfo app : getLaunchableApps()) {
            if (keys.contains(appKey(app))) result.add(app);
        }
        return result;
    }

    void loadPinned(String query) {
        if (content == null) return;

        List<ResolveInfo> apps = getPinnedApps();
        List<ResolveInfo> filtered = new ArrayList<>();

        for (ResolveInfo app : apps) {
            String name = app.loadLabel(getPackageManager()).toString();

            if (name.toLowerCase(Locale.ROOT).contains(
                query.toLowerCase(Locale.ROOT))) {
                filtered.add(app);
            }
        }

        if (filtered.isEmpty()) {
            content.removeAllViews();

            TextView empty = text(
                apps.isEmpty()
                    ? "No pinned apps yet.\nLong-press an app on Home to pin it."
                    : "No matching pinned apps found.",
                14, 0xFFB9C7E8
            );

            content.addView(empty,
                new LinearLayout.LayoutParams(-1, dp(150)));
        } else {
            populateGrid(content, filtered);
        }
    }

    @Override
    public void onBackPressed() {
        if (pinnedPage) showHome();
        else super.onBackPressed();
    }

    // Cyber-space background with a large glowing X.
    class CyberBackground extends View {
        Paint p = new Paint(3);
        Random random = new Random(17);

        CyberBackground(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            p.setShader(new LinearGradient(
                0, 0, w, h,
                new int[]{
                    0xFF020817, 0xFF10153A,
                    0xFF211044, 0xFF030817
                },
                null, Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);

            p.setColor(0x332D75B8);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));

            for (int i = 0; i < 9; i++) {
                float y = h * 0.48f + dp(i * 22);
                canvas.drawLine(0, y, w, y, p);
            }

            for (int i = 0; i < 7; i++) {
                float x = w * i / 6f;
                canvas.drawLine(x, h * 0.42f, x, h, p);
            }

            p.setStyle(Paint.Style.FILL);

            // Large central neon X.
            float cx = w * 0.52f;
            float cy = h * 0.53f;
            float size = Math.min(w * 0.72f, h * 0.40f);

            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(23));
            p.setColor(0x222D8FFF);
            canvas.drawLine(cx - size / 2, cy - size / 2,
                cx + size / 2, cy + size / 2, p);
            canvas.drawLine(cx + size / 2, cy - size / 2,
                cx - size / 2, cy + size / 2, p);

            p.setStrokeWidth(dp(5));
            p.setColor(0x5533CFFF);
            canvas.drawLine(cx - size / 2, cy - size / 2,
                cx + size / 2, cy + size / 2, p);
            canvas.drawLine(cx + size / 2, cy - size / 2,
                cx - size / 2, cy + size / 2, p);

            p.setStrokeWidth(dp(1.5f));
            p.setColor(0xAA55DFFF);
            canvas.drawLine(cx - size / 2, cy - size / 2,
                cx + size / 2, cy + size / 2, p);
            canvas.drawLine(cx + size / 2, cy - size / 2,
                cx - size / 2, cy + size / 2, p);

            p.setStyle(Paint.Style.FILL);

            // Stars and small cyber lights.
            for (int i = 0; i < 75; i++) {
                float x = random.nextFloat() * w;
                float y = random.nextFloat() * h;
                float r = dp(1) + random.nextFloat() * dp(1.5f);

                p.setColor(i % 4 == 0 ? 0xAA8A72FF : 0x9975DFFF);
                canvas.drawCircle(x, y, r, p);
            }

            // Soft lower glow.
            p.setShader(new RadialGradient(
                w * 0.5f, h * 0.78f, w * 0.65f,
                new int[]{0x332B50B9, 0x002B50B9},
                null, Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);
        }
    }

    // Four-sided, straight-edged cyan trapezoid handle.
    class SideHandle extends View {
        Paint p = new Paint(3);
        Path shape = new Path();

        SideHandle(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            shape.reset();
            shape.moveTo(0, dp(12));
            shape.lineTo(w, 0);
            shape.lineTo(w, h);
            shape.lineTo(0, h - dp(12));
            shape.close();

            p.setColor(0x6630BDEB);
            p.setStyle(Paint.Style.FILL);
            canvas.drawPath(shape, p);

            p.setColor(0xCC35D9FF);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.5f));
            canvas.drawPath(shape, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            p.setTextSize(dp(30));
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextAlign(Paint.Align.CENTER);

            String arrow = pinnedPage ? "›" : "‹";
            canvas.drawText(arrow, w / 2, h / 2 + dp(10), p);
        }
    }
}
