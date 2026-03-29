package com.yeepsmod.quest;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private boolean menuVisible = true;

    private int BG_COLOR = Color.parseColor("#141A24");
    private int ACCENT_COLOR = Color.parseColor("#2EC08B");
    private int BTN_COLOR = Color.parseColor("#262D3D");
    private int RED_COLOR = Color.parseColor("#CC2424");

    private boolean[] modStates = new boolean[10];
    private String[] modNames = {
        "God Mode", "Fly", "No Clip", "Speed Boost",
        "Spider Climb", "Invisible", "Big Hands",
        "Super Push", "Full Bright", "ESP"
    };

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createOverlay();
    }

    private void createOverlay() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);

        // Menu panel - top left, small size
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackgroundColor(BG_COLOR);
        menu.setPadding(20, 20, 20, 20);

        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(550, FrameLayout.LayoutParams.WRAP_CONTENT);
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.topMargin = 10;
        menuParams.leftMargin = 10;
        root.addView(menu, menuParams);

        // Header
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView header = new TextView(this);
        header.setText("YeepsMod");
        header.setTextColor(ACCENT_COLOR);
        header.setTextSize(18);
        header.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        header.setLayoutParams(hlp);
        headerRow.addView(header);

        Button hideBtn = new Button(this);
        hideBtn.setText("—");
        hideBtn.setTextColor(Color.WHITE);
        hideBtn.setBackgroundColor(BTN_COLOR);
        hideBtn.setTextSize(12);
        hideBtn.setLayoutParams(new LinearLayout.LayoutParams(80, 60));
        hideBtn.setOnClickListener(v -> {
            menuVisible = !menuVisible;
            // toggle content visibility
        });
        headerRow.addView(hideBtn);
        menu.addView(headerRow);

        // Tabs
        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setPadding(0, 10, 0, 10);

        String[] tabNames = {"Players", "Mods", "Profile"};
        LinearLayout[] tabContents = new LinearLayout[tabNames.length];
        Button[] tabBtns = new Button[tabNames.length];

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
            tabBtn.setTextSize(11);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tabBtn.setLayoutParams(tp);
            tabBtns[i] = tabBtn;
            tabBar.addView(tabBtn);

            LinearLayout tabContent = new LinearLayout(this);
            tabContent.setOrientation(LinearLayout.VERTICAL);
            tabContent.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            tabContents[i] = tabContent;

            if (i == 0) buildPlayersTab(tabContent);
            else if (i == 1) buildModsTab(tabContent);
            else buildProfileTab(tabContent);

            contentArea.addView(tabContent);

            tabBtn.setOnClickListener(v -> {
                for (int j = 0; j < tabContents.length; j++) {
                    tabContents[j].setVisibility(j == idx ? View.VISIBLE : View.GONE);
                    tabBtns[j].setTextColor(j == idx ? ACCENT_COLOR : Color.GRAY);
                }
            });
        }

        menu.addView(tabBar);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500);
        menu.addView(scrollView, scrollParams);

        // Window params - NOT_FOCUSABLE so Oculus menu still works
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 10;
        params.y = 10;

        overlayView = root;
        windowManager.addView(root, params);
    }

    private void buildPlayersTab(LinearLayout container) {
        Button refreshBtn = makeButton("↻ Refresh Players", ACCENT_COLOR, Color.BLACK);
        refreshBtn.setOnClickListener(v -> Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show());
        container.addView(refreshBtn);
    }

    private void buildModsTab(LinearLayout container) {
        for (int i = 0; i < modNames.length; i++) {
            final int idx = i;
            Button btn = makeButton(modNames[i] + ": OFF", BTN_COLOR, Color.WHITE);
            btn.setOnClickListener(v -> {
                modStates[idx] = !modStates[idx];
                btn.setText(modNames[idx] + (modStates[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(modStates[idx] ? Color.parseColor("#1A4A35") : BTN_COLOR);
                btn.setTextColor(modStates[idx] ? ACCENT_COLOR : Color.WHITE);
            });
            container.addView(btn);
        }
    }

    private void buildProfileTab(LinearLayout container) {
        EditText nameField = new EditText(this);
        nameField.setHint("Enter name...");
        nameField.setHintTextColor(Color.GRAY);
        nameField.setTextColor(Color.WHITE);
        nameField.setBackgroundColor(BTN_COLOR);
        nameField.setPadding(20, 10, 20, 10);
        container.addView(nameField);

        Button setBtn = makeButton("Set Name", ACCENT_COLOR, Color.BLACK);
        setBtn.setOnClickListener(v -> Toast.makeText(this, "Name set!", Toast.LENGTH_SHORT).show());
        container.addView(setBtn);
    }

    private Button makeButton(String text, int bgColor, int textColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(textColor);
        btn.setBackgroundColor(bgColor);
        btn.setTextSize(11);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 4, 0, 4);
        btn.setLayoutParams(params);
        return btn;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
    }
}
