package com.yeepsmod.quest;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private View menuPanel;
    private boolean menuVisible = false;

    private int BG_COLOR = Color.parseColor("#141A24");
    private int ACCENT_COLOR = Color.parseColor("#2EC08B");
    private int BTN_COLOR = Color.parseColor("#262D3D");
    private int RED_COLOR = Color.parseColor("#CC2424");

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createOverlay();
    }

    private void createOverlay() {
        // Root container
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);

        // Y Button
        Button yBtn = new Button(this);
        yBtn.setText("Y");
        yBtn.setTextColor(Color.BLACK);
        yBtn.setTextSize(20);
        yBtn.setBackgroundColor(ACCENT_COLOR);
        yBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        FrameLayout.LayoutParams yParams = new FrameLayout.LayoutParams(120, 120);
        yParams.gravity = Gravity.TOP | Gravity.START;
        yParams.topMargin = 60;
        yParams.leftMargin = 20;
        root.addView(yBtn, yParams);

        // Menu Panel
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackgroundColor(BG_COLOR);
        menu.setVisibility(View.GONE);
        menu.setPadding(20, 20, 20, 20);

        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(700, WindowManager.LayoutParams.WRAP_CONTENT);
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.topMargin = 200;
        menuParams.leftMargin = 20;
        root.addView(menu, menuParams);
        menuPanel = menu;

        // Header
        TextView header = new TextView(this);
        header.setText("YeepsMod VR");
        header.setTextColor(ACCENT_COLOR);
        header.setTextSize(18);
        header.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        menu.addView(header);

        // Tab buttons
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, 10, 0, 10);

        String[] tabNames = {"Players", "Mods", "Profile"};
        LinearLayout[] tabContents = new LinearLayout[tabNames.length];

        // Content area
        ScrollView scrollView = new ScrollView(this);
        LinearLayout contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(contentArea);

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;
            Button tabBtn = new Button(this);
            tabBtn.setText(tabNames[i]);
            tabBtn.setTextColor(i == 0 ? ACCENT_COLOR : Color.GRAY);
            tabBtn.setBackgroundColor(Color.TRANSPARENT);
            tabBtn.setTextSize(12);

            LinearLayout tabContent = new LinearLayout(this);
            tabContent.setOrientation(LinearLayout.VERTICAL);
            tabContent.setPadding(0, 10, 0, 10);
            tabContent.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            tabContents[i] = tabContent;

            // Add content to each tab
            if (i == 0) buildPlayersTab(tabContent);
            else if (i == 1) buildModsTab(tabContent);
            else if (i == 2) buildProfileTab(tabContent);

            contentArea.addView(tabContent);

            tabBtn.setOnClickListener(v -> {
                for (int j = 0; j < tabContents.length; j++) {
                    tabContents[j].setVisibility(j == idx ? View.VISIBLE : View.GONE);
                }
            });

            tabs.addView(tabBtn);
        }

        menu.addView(tabs);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600);
        menu.addView(scrollView, scrollParams);

        // Toggle menu
        yBtn.setOnClickListener(v -> {
            menuVisible = !menuVisible;
            menu.setVisibility(menuVisible ? View.VISIBLE : View.GONE);
        });

        // Window params
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        overlayView = root;
        windowManager.addView(root, params);
    }

    private void buildPlayersTab(LinearLayout container) {
        TextView info = new TextView(this);
        info.setText("Players in lobby:");
        info.setTextColor(Color.GRAY);
        info.setTextSize(12);
        container.addView(info);

        Button refreshBtn = makeButton("↻ Refresh Players", ACCENT_COLOR, Color.BLACK);
        refreshBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing players...", Toast.LENGTH_SHORT).show();
        });
        container.addView(refreshBtn);
    }

    private void buildModsTab(LinearLayout container) {
        String[] mods = {"God Mode", "Fly", "No Clip", "Speed Boost", "Spider Climb", "Invisible", "Big Hands", "Super Push", "Full Bright", "ESP"};
        for (String mod : mods) {
            Button btn = makeButton(mod + ": OFF", BTN_COLOR, Color.WHITE);
            btn.setOnClickListener(v -> {
                boolean isOn = btn.getText().toString().contains("ON");
                btn.setText(mod + (isOn ? ": OFF" : ": ON"));
                btn.setBackgroundColor(isOn ? BTN_COLOR : Color.parseColor("#1A4A35"));
                Toast.makeText(this, mod + (isOn ? " disabled" : " enabled"), Toast.LENGTH_SHORT).show();
            });
            container.addView(btn);
        }
    }

    private void buildProfileTab(LinearLayout container) {
        TextView lbl = new TextView(this);
        lbl.setText("Change Display Name");
        lbl.setTextColor(ACCENT_COLOR);
        lbl.setTextSize(13);
        lbl.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        container.addView(lbl);

        android.widget.EditText nameField = new android.widget.EditText(this);
        nameField.setHint("Enter name...");
        nameField.setHintTextColor(Color.GRAY);
        nameField.setTextColor(Color.WHITE);
        nameField.setBackgroundColor(BTN_COLOR);
        nameField.setPadding(20, 15, 20, 15);
        container.addView(nameField);

        Button setBtn = makeButton("Set Name", ACCENT_COLOR, Color.BLACK);
        setBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString();
            if (!name.isEmpty()) {
                Toast.makeText(this, "Name set to: " + name, Toast.LENGTH_SHORT).show();
            }
        });
        container.addView(setBtn);

        TextView lbl2 = new TextView(this);
        lbl2.setText("Change Color");
        lbl2.setTextColor(ACCENT_COLOR);
        lbl2.setTextSize(13);
        lbl2.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        lbl2.setPadding(0, 20, 0, 0);
        container.addView(lbl2);

        String[] colors = {"Red", "Blue", "Green", "Pink", "Gold", "White", "Orange", "Purple"};
        int[] colorVals = {
            Color.parseColor("#E63333"), Color.parseColor("#3366E6"),
            Color.parseColor("#33BF4D"), Color.parseColor("#E666B3"),
            Color.parseColor("#E6BF1A"), Color.parseColor("#E6E6E6"),
            Color.parseColor("#E67F1A"), Color.parseColor("#9933E6")
        };

        LinearLayout colorRow1 = new LinearLayout(this);
        colorRow1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout colorRow2 = new LinearLayout(this);
        colorRow2.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < colors.length; i++) {
            final String colorName = colors[i];
            final int colorVal = colorVals[i];
            Button cb = new Button(this);
            cb.setText(colorName);
            cb.setTextColor(i == 5 ? Color.BLACK : Color.WHITE);
            cb.setBackgroundColor(colorVal);
            cb.setTextSize(10);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 80, 1);
            p.setMargins(4, 4, 4, 4);
            cb.setLayoutParams(p);
            cb.setOnClickListener(v -> {
                Toast.makeText(this, "Color set to: " + colorName, Toast.LENGTH_SHORT).show();
            });
            if (i < 4) colorRow1.addView(cb);
            else colorRow2.addView(cb);
        }

        container.addView(colorRow1);
        container.addView(colorRow2);
    }

    private Button makeButton(String text, int bgColor, int textColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(textColor);
        btn.setBackgroundColor(bgColor);
        btn.setTextSize(12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 6, 0, 6);
        btn.setLayoutParams(params);
        return btn;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
    }
}
