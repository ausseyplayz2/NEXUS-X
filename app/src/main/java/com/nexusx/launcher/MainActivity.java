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
            new int[]{0x66102747, 0x7710152F, 0x77211343}
        );
        g.setCornerRadius(0);
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
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout logoPanel = new LinearLayout(this);
        logoPanel.setOrientation(LinearLayout.VERTICAL);
        logoPanel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        logoPanel.setLayoutParams(
            new LinearLayout.LayoutParams(0, dp(72), 1.15f)
        );

        TextView logo = text("◈  NEXUS-X", 23, cyan);
        logo.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        logo.setShadowLayer(dp(12), 0, 0, 0xCC16BFFF);
        fitTitle(logo, 17, 23);
        logoPanel.addView(logo,
            new LinearLayout.LayoutParams(-1, dp(48)));

        View accentLine = new View(this);
        GradientDrawable accent = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xFF26DFFF, 0xAA7655FF, 0x0026DFFF}
        );
        accentLine.setBackground(accent);
        logoPanel.addView(accentLine,
            new LinearLayout.LayoutParams(dp(112), dp(2)));

        LinearLayout timePanel = new LinearLayout(this);
        timePanel.setOrientation(LinearLayout.VERTICAL);
        timePanel.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        timePanel.setLayoutParams(
            new LinearLayout.LayoutParams(0, dp(72), 0.85f)
        );

        TextView militaryClock = text("", 34, Color.WHITE);
        militaryClock.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        militaryClock.setShadowLayer(dp(9), 0, 0, 0xAA31CFFF);
        activeClock = militaryClock;
        timePanel.addView(militaryClock,
            new LinearLayout.LayoutParams(-1, dp(42)));

        TextView normalClock = text("", 13, cyan);
        normalClock.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        timePanel.addView(normalClock,
            new LinearLayout.LayoutParams(-1, dp(23)));

        header.addView(logoPanel);
        header.addView(timePanel);
        root.addView(header,
            new LinearLayout.LayoutParams(-1, dp(74)));

        TextView date = text("", 11, 0xFFB9C7E8);
        date.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        fitTitle(date, 9, 11);
        root.addView(date,
            new LinearLayout.LayoutParams(-1, dp(23)));

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable updateClock = new Runnable() {
            @Override
            public void run() {
                if (activeClock == militaryClock) {
                    Date now = new Date();

                    militaryClock.setText(
                        new java.text.SimpleDateFormat(
                            "HH:mm", Locale.getDefault()
                        ).format(now)
                    );

                    normalClock.setText(
                        new java.text.SimpleDateFormat(
                            "h:mm a", Locale.getDefault()
                        ).format(now)
                    );

                    date.setText(
                        new java.text.SimpleDateFormat(
                            "EEEE, MMMM dd, yyyy", Locale.getDefault()
                        ).format(now)
                    );

                    handler.postDelayed(this, 1000);
                }
            }
        };
        updateClock.run();

        TextView status = text("◈ SYSTEM ONLINE", 9, cyan);
        status.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        status.setShadowLayer(dp(5), 0, 0, 0x8831CFFF);
        root.addView(status,
            new LinearLayout.LayoutParams(-1, dp(21)));
    }

    void showHome() {
        setup();
        showHeader();

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setVerticalFadingEdgeEnabled(true);
        scroll.setFadingEdgeLength(dp(46));
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
        handleWidth = dp(48);
        drawerWidth = getResources().getDisplayMetrics().widthPixels * 0.82f;
        closedOffset = drawerWidth - handleWidth;

        drawer = new FrameLayout(this);

        int bodyWidth = (int)drawerWidth - (int)handleWidth;

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(10), dp(12), dp(9), dp(10));
        GradientDrawable glassPanel = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0x55309AC8, 0x66202F59, 0x66401F68}
        );
        glassPanel.setCornerRadius(dp(5));
        glassPanel.setStroke(dp(1), 0xAA54DFFF);
        body.setBackground(glassPanel);

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
        GradientDrawable searchGlass = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0x44305D80, 0x55402C69}
        );
        searchGlass.setCornerRadius(dp(4));
        searchGlass.setStroke(dp(1), 0x8854DFFF);
        search.setBackground(searchGlass);

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
                (int)handleWidth, dp(160), Gravity.LEFT | Gravity.CENTER_VERTICAL
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

        int iconSize = height >= 100 ? 46 : 32;
        int frameSize = height >= 100 ? 60 : 43;
        int labelSize = height >= 100 ? 10 : 9;

        FrameLayout iconFrame = new FrameLayout(this);

        GradientDrawable iconPlate = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0x66174A69, 0x66171E42, 0x6635205C}
        );
        iconPlate.setCornerRadius(dp(5));
        iconPlate.setStroke(dp(1), 0xCC35DFFF);
        iconFrame.setBackground(iconPlate);
        iconFrame.setElevation(dp(6));

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.loadIcon(pm));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(3), dp(3), dp(3), dp(3));

        iconFrame.addView(icon,
            new FrameLayout.LayoutParams(
                dp(iconSize), dp(iconSize), Gravity.CENTER
            ));

        View topGlow = new View(this);
        GradientDrawable glowLine = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0x0028E8FF, 0xFF42E8FF, 0x0028E8FF}
        );
        topGlow.setBackground(glowLine);
        iconFrame.addView(topGlow,
            new FrameLayout.LayoutParams(
                dp(25), dp(2), Gravity.TOP | Gravity.CENTER_HORIZONTAL
            ));

        tile.addView(iconFrame,
            new LinearLayout.LayoutParams(dp(frameSize), dp(frameSize)));

        TextView label = text(
            app.loadLabel(pm).toString(), labelSize, 0xFFE8F8FF
        );
        label.setMaxLines(2);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setShadowLayer(dp(4), 0, 0, 0xAA19BFFF);
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

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(name)
            .setItems(options, (d, which) -> {
                if (which == 0) launchApp(app);
                else if (which == 1) togglePin(app);
                else if (which == 2) openAppInfo(app);
            })
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(d -> {
            android.graphics.drawable.GradientDrawable panel =
                new android.graphics.drawable.GradientDrawable();
            panel.setColor(0x66101F35);
            panel.setCornerRadius(0);
            panel.setStroke(dp(1), 0xFF19BFFF);

            android.view.Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(panel);
                window.setDimAmount(0.65f);
            }

            android.widget.ListView list = dialog.getListView();
            list.setDivider(new android.graphics.drawable.ColorDrawable(
                0x8830BFEF
            ));
            list.setDividerHeight(dp(1));

            list.post(() -> {
                for (int i = 0; i < list.getChildCount(); i++) {
                    android.view.View item = list.getChildAt(i);
                    if (item instanceof TextView) {
                        TextView option = (TextView) item;
                        option.setTextColor(0xFFEAFBFF);
                        option.setPadding(
                            dp(20), dp(12), dp(16), dp(12)
                        );
                    }
                }
            });

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(0xFF45D9FF);
        });

        dialog.show();
    }

    void launchApp(ResolveInfo app) {
        try {
            String packageName = app.activityInfo.packageName;

            Intent launch = getPackageManager()
                .getLaunchIntentForPackage(packageName);

            if (launch == null) {
                Toast.makeText(this,
                    "This app has no launch screen",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            launch.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );

            startActivity(launch);

        } catch (Exception e) {
            Toast.makeText(this,
                "Unable to open app: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
            android.util.Log.e("NEXUS-X",
                "App launch failed", e);
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
        long animationStart;
        boolean animationRunning;

        CyberBackground(Context c) {
            super(c);
            // Hardware rendering enabled for better performance.
            animationStart = android.os.SystemClock.uptimeMillis();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            animationStart = android.os.SystemClock.uptimeMillis();
            animationRunning = true;
            postInvalidate();
        }

        @Override
        protected void onDetachedFromWindow() {
            animationRunning = false;
            super.onDetachedFromWindow();
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

            long elapsed = android.os.SystemClock.uptimeMillis() - animationStart;
            float pulse = (float)(0.5 + 0.5 * Math.sin(elapsed / 850.0));

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

            // Layered cyber X emblem.
            float cx = w * 0.50f;
            float cy = h * 0.52f;
            float size = Math.min(w * 0.72f, h * 0.37f);

            Path beam = new Path();

            // Blue armored diagonal.
            beam.moveTo(cx - size * 0.50f, cy - size * 0.34f);
            beam.lineTo(cx - size * 0.30f, cy - size * 0.49f);
            beam.lineTo(cx + size * 0.50f, cy + size * 0.34f);
            beam.lineTo(cx + size * 0.30f, cy + size * 0.49f);
            beam.close();

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                cx - size * 0.5f, cy - size * 0.4f,
                cx + size * 0.5f, cy + size * 0.4f,
                new int[]{0xFF247DFF, 0xFF31E6FF, 0xFF7652FF},
                null, Shader.TileMode.CLAMP
            ));
            p.setShadowLayer(dp(17), 0, 0, 0xAA168CFF);
            canvas.drawPath(beam, p);
            p.clearShadowLayer();
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.5f));
            p.setColor(0xFF8AF5FF);
            canvas.drawPath(beam, p);

            // Purple opposing diagonal.
            beam.reset();
            beam.moveTo(cx + size * 0.30f, cy - size * 0.49f);
            beam.lineTo(cx + size * 0.50f, cy - size * 0.34f);
            beam.lineTo(cx - size * 0.30f, cy + size * 0.49f);
            beam.lineTo(cx - size * 0.50f, cy + size * 0.34f);
            beam.close();

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                cx + size * 0.5f, cy - size * 0.4f,
                cx - size * 0.5f, cy + size * 0.4f,
                new int[]{0xFF8A4DFF, 0xFF27CFFF, 0xFF267BFF},
                null, Shader.TileMode.CLAMP
            ));
            p.setShadowLayer(dp(17), 0, 0, 0xAA873DFF);
            canvas.drawPath(beam, p);
            p.clearShadowLayer();
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.5f));
            p.setColor(0xFFB8A0FF);
            canvas.drawPath(beam, p);

            // Illuminated inner rails.
            line(canvas, cx - size * 0.38f, cy - size * 0.31f,
                 cx + size * 0.38f, cy + size * 0.31f,
                 0xCCB8F8FF, dp(1.4f));
            line(canvas, cx + size * 0.38f, cy - size * 0.31f,
                 cx - size * 0.38f, cy + size * 0.31f,
                 0xCCDBC8FF, dp(1.4f));

            // Central energy core.
            Path core = new Path();
            core.moveTo(cx, cy - size * 0.17f);
            core.lineTo(cx + size * 0.17f, cy);
            core.lineTo(cx, cy + size * 0.17f);
            core.lineTo(cx - size * 0.17f, cy);
            core.close();

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                cx - size * 0.15f, cy - size * 0.15f,
                cx + size * 0.15f, cy + size * 0.15f,
                new int[]{0xFF142D55, 0xFF176C91, 0xFF30205C},
                null, Shader.TileMode.CLAMP
            ));
            p.clearShadowLayer();
            canvas.drawPath(core, p);
            p.clearShadowLayer();
            p.setShader(null);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setColor(0xFF9AF7FF);
            canvas.drawPath(core, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFB9FBFF);
            canvas.drawCircle(cx, cy, dp(3f + pulse * 1.5f), p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(0xAA8BDBFF);
            canvas.drawCircle(cx, cy, size * 0.23f, p);

            // HUD circles around the X.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(0x5545BFFF);
            canvas.drawCircle(cx, cy, size * 0.66f, p);
            p.setColor(0x664D64C8);
            canvas.drawCircle(cx, cy, size * 0.73f, p);

            p.setColor(0x5539CFFF);
            RectF arc = new RectF(
                cx - size * 0.78f, cy - size * 0.78f,
                cx + size * 0.78f, cy + size * 0.78f
            );
            canvas.save();
            canvas.rotate((elapsed / 1000f) * 4f, cx, cy);
            canvas.drawArc(arc, 205, 48, false, p);
            canvas.drawArc(arc, 25, 52, false, p);
            canvas.drawArc(arc, 115, 35, false, p);
            canvas.restore();

            // Small HUD tick marks.
            for (int i = 0; i < 32; i++) {
                double a = Math.PI * 2 * i / 32;
                float r1 = size * 0.70f;
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
            for (int i = 0; i < 30; i++) {
                float x = stars.nextFloat() * w;
                float baseY = stars.nextFloat() * h;
                float speed = 8f + (i % 5) * 3f;
                float y = (baseY + elapsed / 1000f * speed) % h;
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

            if (animationRunning) {
                postInvalidateDelayed(66);
            }
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
            p.setColor(0x5530A9D5);
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
