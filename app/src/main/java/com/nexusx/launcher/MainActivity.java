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

    FrameLayout shell, drawer;
    LinearLayout root, drawerContent;
    EditText search;
    TextView activeClock;

    boolean drawerOpen = false;
    float drawerWidth, handleWidth, closedOffset;

    final String PREFS = "nexus_preferences";
    final String PINNED = "pinned_apps";

    int cyan = Color.rgb(0, 220, 255);
    int purple = Color.rgb(165, 75, 255);

    int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable panel() {
        GradientDrawable g = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0xDD102747, 0xEE10152F, 0xEE211343}
        );
        g.setCornerRadius(dp(13));
        g.setStroke(dp(1), 0x9954CFFF);
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

    void fitTitle(TextView t, int min, int max) {
        t.setMaxLines(2);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        t.setAutoSizeTextTypeUniformWithConfiguration(
            min, max, 1, android.util.TypedValue.COMPLEX_UNIT_SP
        );
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

        shell.addView(
            new CyberBackground(this),
            new FrameLayout.LayoutParams(-1, -1)
        );

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(9), dp(7), dp(9), dp(7));

        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));
        setContentView(shell);
    }

    void showHeader() {
        TextView logo = text("✦ N E X U S  X ✦", 22, cyan);
        fitTitle(logo, 15, 22);
        root.addView(logo, new LinearLayout.LayoutParams(-1, dp(36)));

        TextView subtitle = text("GAMING COMMAND CENTER", 10, 0xFFB18CFF);
        fitTitle(subtitle, 8, 10);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(18)));

        TextView clock = text("", 42, Color.WHITE);
        activeClock = clock;
        root.addView(clock, new LinearLayout.LayoutParams(-1, dp(49)));

        TextView date = text(
            new java.text.SimpleDateFormat(
                "EEEE, MMMM dd", Locale.getDefault()
            ).format(new Date()),
            12, 0xFFB9C7E8
        );
        fitTitle(date, 9, 12);
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

        TextView status = text("◈ SYSTEM ONLINE", 9, cyan);
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(25)));
    }

    void showHome() {
        setup();
        showHeader();

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setFillViewport(false);

        LinearLayout homeContent = new LinearLayout(this);
        homeContent.setOrientation(LinearLayout.VERTICAL);
        homeContent.setPadding(0, dp(5), 0, dp(5));

        scroll.addView(homeContent);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        populateGrid(homeContent, getLaunchableApps(), 4, 105);

        buildDrawer();
    }

    // The drawer is layered over the home screen.
    void buildDrawer() {
        handleWidth = dp(64);
        drawerWidth = getResources().getDisplayMetrics().widthPixels * 0.82f;
        closedOffset = drawerWidth - handleWidth;

        drawer = new FrameLayout(this);

        int bodyWidth = (int)drawerWidth - (int)handleWidth;

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(10), dp(12), dp(9), dp(10));
        body.setBackground(panel());

        FrameLayout.LayoutParams bodyParams =
            new FrameLayout.LayoutParams(
                bodyWidth, -1, Gravity.RIGHT
            );
        bodyParams.leftMargin = (int)handleWidth;
        drawer.addView(body, bodyParams);

        TextView title = text("✦ PINNED APPS ✦", 18, cyan);
        fitTitle(title, 12, 18);
        body.addView(title,
            new LinearLayout.LayoutParams(-1, dp(48)));

        TextView subtitle = text("YOUR QUICK ACCESS", 9, 0xFFB18CFF);
        body.addView(subtitle,
            new LinearLayout.LayoutParams(-1, dp(22)));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xFF9BAFCB);
        search.setHint("⌕  Search apps");
        search.setTextSize(13);
        search.setPadding(dp(10), dp(4), dp(10), dp(4));
        search.setBackground(panel());

        body.addView(search,
            new LinearLayout.LayoutParams(-1, dp(45)));

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);

        drawerContent = new LinearLayout(this);
        drawerContent.setOrientation(LinearLayout.VERTICAL);
        drawerContent.setPadding(0, dp(8), 0, dp(8));

        scroll.addView(drawerContent);
        body.addView(scroll,
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

        SideHandle handle = new SideHandle(this);
        FrameLayout.LayoutParams handleParams =
            new FrameLayout.LayoutParams(
                (int)handleWidth, dp(118), Gravity.LEFT | Gravity.CENTER_VERTICAL
            );
        drawer.addView(handle, handleParams);

        shell.addView(drawer,
            new FrameLayout.LayoutParams(
                (int)drawerWidth, -1, Gravity.RIGHT
            ));

        drawer.setTranslationX(closedOffset);
        drawerOpen = false;

        handle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawer.animate().cancel();
                    v.setTag(Float.valueOf(event.getRawX()));
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float start = (Float)v.getTag();
                    float dx = event.getRawX() - start;
                    float next = closedOffset + dx;
                    next = Math.max(0, Math.min(closedOffset, next));
                    drawer.setTranslationX(next);
                    return true;

                case MotionEvent.ACTION_UP:
                    float downX = (Float)v.getTag();
                    float moveX = event.getRawX() - downX;

                    if (Math.abs(moveX) < dp(8)) {
                        settleDrawer(!drawerOpen);
                    } else {
                        settleDrawer(
                            drawer.getTranslationX() < closedOffset * 0.52f
                        );
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    settleDrawer(
                        drawer.getTranslationX() < closedOffset * 0.52f
                    );
                    return true;
            }
            return true;
        });

        loadPinned("");
    }

    void settleDrawer(boolean open) {
        drawerOpen = open;

        float target = open ? 0 : closedOffset;

        drawer.animate()
            .translationX(target)
            .setDuration(210)
            .start();
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

    void populateGrid(
        LinearLayout target,
        List<ResolveInfo> apps,
        int columns,
        int height
    ) {
        target.removeAllViews();

        for (int i = 0; i < apps.size(); i += columns) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = i; j < Math.min(i + columns, apps.size()); j++) {
                addAppTile(row, apps.get(j), height);
            }

            for (int j = Math.min(i + columns, apps.size());
                 j < i + columns; j++) {
                row.addView(new View(this),
                    new LinearLayout.LayoutParams(0, dp(height), 1));
            }

            target.addView(row,
                new LinearLayout.LayoutParams(-1, dp(height + 5)));
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

        int iconSize = height >= 100 ? 49 : 35;
        int labelSize = height >= 100 ? 10 : 9;

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
        label.setAutoSizeTextTypeUniformWithConfiguration(
            8, labelSize, 1,
            android.util.TypedValue.COMPLEX_UNIT_SP
        );

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
        boolean pinned = isPinned(app);

        String[] options = {
            "Open",
            pinned ? "Unpin" : "Pin",
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

        if (drawerContent != null) {
            loadPinned(search == null ? "" : search.getText().toString());
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
        if (drawerContent == null) return;

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
            drawerContent.removeAllViews();

            TextView empty = text(
                apps.isEmpty()
                    ? "No pinned apps"
                    : "No matching apps",
                13, 0xFFB9C7E8
            );

            drawerContent.addView(empty,
                new LinearLayout.LayoutParams(-1, dp(110)));
        } else {
            populateGrid(drawerContent, filtered, 3, 100);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerOpen) {
            settleDrawer(false);
        } else {
            super.onBackPressed();
        }
    }

    // Detailed cyber background.
    class CyberBackground extends View {
        Paint p = new Paint(3);
        Random stars = new Random(24);

        CyberBackground(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void line(Canvas c, float x1, float y1, float x2, float y2,
                  int color, float width) {
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(width);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setColor(color);
            c.drawLine(x1, y1, x2, y2, p);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                0, 0, w, h,
                new int[]{
                    0xFF020611, 0xFF09142D,
                    0xFF171039, 0xFF030817
                },
                null, Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);

            // Nebula lighting.
            p.setShader(new RadialGradient(
                w * 0.76f, h * 0.40f, w * 0.75f,
                new int[]{0x553B1B83, 0x22202F8B, 0x00102050},
                null, Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);

            // Circuit-board traces around the screen.
            int[] circuit = {0x5532CFFF, 0x443D8CFF, 0x554C4BBD};

            for (int i = 0; i < 7; i++) {
                float y = dp(90 + i * 65);
                float x = (i % 2 == 0) ? dp(8) : w - dp(8);
                float end = (i % 2 == 0) ? dp(65) : w - dp(65);

                line(canvas, x, y, end, y, circuit[i % 3], dp(1));
                line(canvas, end, y, end, y + dp(17),
                    circuit[i % 3], dp(1));
                line(canvas, end, y + dp(17), end + dp(17), y + dp(17),
                    circuit[i % 3], dp(1));

                p.setStyle(Paint.Style.FILL);
                p.setColor(0xAA36DFFF);
                canvas.drawCircle(end, y, dp(2), p);
            }

            // Faint technical grid.
            for (int i = 0; i < 12; i++) {
                float y = h * 0.35f + i * dp(27);
                line(canvas, 0, y, w, y, 0x222F9CD0, dp(1));
            }

            for (int i = 0; i < 9; i++) {
                float x = i * w / 8f;
                line(canvas, x, h * 0.32f, x, h, 0x1E2F9CD0, dp(1));
            }

            // Central X: layered neon rails and energy core.
            float cx = w * 0.50f;
            float cy = h * 0.52f;
            float s = Math.min(w * 0.65f, h * 0.34f);

            float ax = cx - s / 2;
            float bx = cx + s / 2;
            float top = cy - s / 2;
            float bottom = cy + s / 2;

            line(canvas, ax, top, bx, bottom, 0x332E75FF, dp(28));
            line(canvas, bx, top, ax, bottom, 0x332E75FF, dp(28));

            line(canvas, ax, top, bx, bottom, 0x5535CFFF, dp(13));
            line(canvas, bx, top, ax, bottom, 0x5535CFFF, dp(13));

            line(canvas, ax, top, bx, bottom, 0xCC37DFFF, dp(3));
            line(canvas, bx, top, ax, bottom, 0xCC37DFFF, dp(3));

            line(canvas, ax + dp(5), top, bx + dp(5), bottom,
                0x664B69FF, dp(1));
            line(canvas, bx - dp(5), top, ax - dp(5), bottom,
                0x664B69FF, dp(1));

            // HUD circles around the X.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(0x5545BFFF);
            canvas.drawCircle(cx, cy, s * 0.66f, p);
            p.setColor(0x664D64C8);
            canvas.drawCircle(cx, cy, s * 0.73f, p);

            p.setColor(0x5539CFFF);
            RectF arc = new RectF(
                cx - s * 0.78f, cy - s * 0.78f,
                cx + s * 0.78f, cy + s * 0.78f
            );
            canvas.drawArc(arc, 205, 48, false, p);
            canvas.drawArc(arc, 25, 52, false, p);
            canvas.drawArc(arc, 115, 35, false, p);

            // Small HUD tick marks.
            for (int i = 0; i < 32; i++) {
                double a = Math.PI * 2 * i / 32;
                float r1 = s * 0.70f;
                float r2 = r1 + (i % 4 == 0 ? dp(9) : dp(4));

                float x1 = cx + (float)Math.cos(a) * r1;
                float y1 = cy + (float)Math.sin(a) * r1;
                float x2 = cx + (float)Math.cos(a) * r2;
                float y2 = cy + (float)Math.sin(a) * r2;

                line(canvas, x1, y1, x2, y2,
                    0x7754DFFF, dp(1));
            }

            p.setStyle(Paint.Style.FILL);

            // Stars and glowing particles.
            stars.setSeed(24);
            for (int i = 0; i < 95; i++) {
                float x = stars.nextFloat() * w;
                float y = stars.nextFloat() * h;
                float r = dp(1) + stars.nextFloat() * dp(1.4f);

                p.setColor(i % 5 == 0 ? 0xCC9C75FF : 0xAA65DFFF);
                canvas.drawCircle(x, y, r, p);
            }

            // Corner targeting brackets.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setColor(0x8851CFFF);

            float m = dp(13);
            float len = dp(20);

            canvas.drawLine(m, m, m + len, m, p);
            canvas.drawLine(m, m, m, m + len, p);

            canvas.drawLine(w - m, m, w - m - len, m, p);
            canvas.drawLine(w - m, m, w - m, m + len, p);

            canvas.drawLine(m, h - m, m + len, h - m, p);
            canvas.drawLine(m, h - m, m, h - m - len, p);

            canvas.drawLine(w - m, h - m, w - m - len, h - m, p);
            canvas.drawLine(w - m, h - m, w - m, h - m - len, p);

            p.setStyle(Paint.Style.FILL);
        }
    }

    // Straight-edged sideways isosceles trapezoid handle.
    class SideHandle extends View {
        Paint p = new Paint(3);
        Path shape = new Path();

        SideHandle(Context c) {
            super(c);
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

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xAA0C6C9B);
            canvas.drawPath(shape, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.5f));
            p.setColor(0xFF39DFFF);
            canvas.drawPath(shape, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(0x5572E8FF);
            p.setTextSize(dp(31));
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextAlign(Paint.Align.CENTER);

            canvas.drawText("‹‹‹", w / 2, h / 2 + dp(10), p);
        }
    }
}
