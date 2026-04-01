package com.yeepsmod.quest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    private int BG = Color.parseColor("#0A0A0A");
    private int ACCENT = Color.parseColor("#2EC08B");
    private int BTN = Color.parseColor("#1A1A1A");
    private int RED = Color.parseColor("#CC2424");

    private LinearLayout[] tabContents;
    private Button[] tabBtns;
    private TextView outputView;
    private LinearLayout gamesListContainer;
    private String selectedPackage = null;
    private String selectedAppLabel = null;
    private TextView selectedGameLabel;
    private boolean shizukuAvailable = false;

    private static final int SHIZUKU_REQUEST_CODE = 1001;

    private final Shizuku.OnRequestPermissionResultListener permissionResultListener =
        (requestCode, grantResult) -> {
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                shizukuAvailable = grantResult == PackageManager.PERMISSION_GRANTED;
                show(shizukuAvailable ?
                    "✓ Shizuku connected! Full access enabled." :
                    "✗ Shizuku permission denied.");
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Shizuku.addRequestPermissionResultListener(permissionResultListener);
        checkShizuku();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(BG);
        root.addView(main, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(Color.parseColor("#111111"));
        header.setPadding(30, 20, 30, 20);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("emder.lol");
        title.setTextColor(ACCENT);
        title.setTextSize(22);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        header.addView(title);

        TextView shizukuStatus = new TextView(this);
        shizukuStatus.setText(shizukuAvailable ? "⚡ Shizuku" : "○ No Shizuku");
        shizukuStatus.setTextColor(shizukuAvailable ? ACCENT : Color.GRAY);
        shizukuStatus.setTextSize(11);
        shizukuStatus.setBackgroundColor(BTN);
        shizukuStatus.setPadding(14, 6, 14, 6);
        header.addView(shizukuStatus);

        main.addView(header);

        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#222222"));
        main.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        String[] tabNames = {"Games", "Mods", "System", "Patcher", "ADB", "Settings"};
        tabContents = new LinearLayout[tabNames.length];
        tabBtns = new Button[tabNames.length];

        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.parseColor("#111111"));

        ScrollView scrollView = new ScrollView(this);
        LinearLayout contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(24, 24, 24, 24);
        scrollView.addView(contentArea);

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;
            Button tb = new Button(this);
            tb.setText(tabNames[i]);
            tb.setTextColor(i == 0 ? ACCENT : Color.GRAY);
            tb.setBackgroundColor(i == 0 ? Color.parseColor("#1A1A1A") : Color.TRANSPARENT);
            tb.setTextSize(11);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tb.setLayoutParams(tp);
            tabBtns[i] = tb;
            tabBar.addView(tb);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            tabContents[i] = content;

            if (i == 0) buildGamesTab(content);
            else if (i == 1) buildModsTab(content);
            else if (i == 2) buildSystemTab(content);
            else if (i == 3) buildPatcherTab(content);
            else if (i == 4) buildADBTab(content);
            else buildSettingsTab(content);

            contentArea.addView(content);

            tb.setOnClickListener(v -> {
                for (int j = 0; j < tabContents.length; j++) {
                    tabContents[j].setVisibility(j == idx ? View.VISIBLE : View.GONE);
                    tabBtns[j].setTextColor(j == idx ? ACCENT : Color.GRAY);
                    tabBtns[j].setBackgroundColor(j == idx ? Color.parseColor("#1A1A1A") : Color.TRANSPARENT);
                }
            });
        }

        main.addView(tabBar);
        View div2 = new View(this);
        div2.setBackgroundColor(Color.parseColor("#222222"));
        main.addView(div2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        main.addView(scrollView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        outputView = new TextView(this);
        outputView.setText("emder.lol ready — connect Shizuku then scan apps");
        outputView.setTextColor(ACCENT);
        outputView.setBackgroundColor(Color.parseColor("#050505"));
        outputView.setTextSize(10);
        outputView.setPadding(20, 10, 20, 10);
        outputView.setTypeface(android.graphics.Typeface.MONOSPACE);
        outputView.setMaxLines(8);
        main.addView(outputView);

        setContentView(root);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(permissionResultListener);
    }

    private void checkShizuku() {
        try {
            shizukuAvailable = Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            shizukuAvailable = false;
        }
    }

    private String runCmd(String cmd) {
        try {
            Process p;
            if (shizukuAvailable) {
                p = Shizuku.newProcess(new String[]{"sh", "-c", cmd}, null, null);
            } else {
                p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            }

            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = out.readLine()) != null) sb.append(line).append("\n");
            while ((line = err.readLine()) != null) sb.append("[ERR] ").append(line).append("\n");

            p.waitFor();
            return sb.length() > 0 ? sb.toString().trim() : "No output.";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void show(String msg) {
        runOnUiThread(() -> outputView.setText(msg));
    }
}
