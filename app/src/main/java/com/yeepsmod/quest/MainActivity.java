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
    private TextView selectedGameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(BG);
        root.addView(main, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // Header
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

        TextView sub = new TextView(this);
        sub.setText("Universal Quest Mod Menu");
        sub.setTextColor(Color.GRAY);
        sub.setTextSize(11);
        header.addView(sub);

        main.addView(header);

        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#222222"));
        main.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        // Tabs
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
        outputView.setText("emder.lol ready — select a game from the Games tab");
        outputView.setTextColor(ACCENT);
        outputView.setBackgroundColor(Color.parseColor("#050505"));
        outputView.setTextSize(10);
        outputView.setPadding(20, 10, 20, 10);
        outputView.setTypeface(android.graphics.Typeface.MONOSPACE);
        outputView.setMaxLines(6);
        main.addView(outputView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private String runCmd(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = out.readLine()) != null) sb.append(line).append("\n");
            while ((line = err.readLine()) != null) sb.append("[ERR] ").append(line).append("\n");
            p.waitFor();
            return sb.length() > 0 ? sb.toString().trim() : "No output.";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    private void runAndShow(String cmd) {
        new Thread(() -> {
            String result = runCmd(cmd);
            runOnUiThread(() -> outputView.setText("$ " + cmd + "\n" + result));
        }).start();
    }

    private void show(String msg) {
        runOnUiThread(() -> outputView.setText(msg));
    }

    // ── Games Tab ─────────────────────────────────────────────────────────
    private void buildGamesTab(LinearLayout c) {
        addSectionLabel(c, "Installed Games");
        addSubLabel(c, "Select a game to target with mods");

        selectedGameLabel = new TextView(this);
        selectedGameLabel.setText("No game selected");
        selectedGameLabel.setTextColor(Color.GRAY);
        selectedGameLabel.setTextSize(12);
        selectedGameLabel.setBackgroundColor(BTN);
        selectedGameLabel.setPadding(20, 15, 20, 15);
        c.addView(selectedGameLabel);

        addBtn(c, "↻ Scan for Games", ACCENT, Color.BLACK, v -> scanGames(c));

        gamesListContainer = new LinearLayout(this);
        gamesListContainer.setOrientation(LinearLayout.VERTICAL);
        c.addView(gamesListContainer);
    }

    private void scanGames(LinearLayout c) {
        gamesListContainer.removeAllViews();
        show("Scanning for installed games...");

        new Thread(() -> {
            List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
            List<PackageInfo> games = new ArrayList<>();

            // Filter for non-system apps that look like games
            String[] systemPrefixes = {"com.android", "com.google", "com.facebook",
                "com.oculus", "com.meta", "android", "com.qualcomm",
                "com.samsung", "com.motorola"};

            for (PackageInfo pkg : packages) {
                if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
                boolean isSystem = false;
                for (String prefix : systemPrefixes) {
                    if (pkg.packageName.startsWith(prefix)) { isSystem = true; break; }
                }
                if (!isSystem) games.add(pkg);
            }

            runOnUiThread(() -> {
                gamesListContainer.removeAllViews();
                if (games.isEmpty()) {
                    show("No games found");
                    return;
                }
                show("Found " + games.size() + " apps — tap one to select it");

                for (PackageInfo pkg : games) {
                    String label;
                    try {
                        label = (String) getPackageManager().getApplicationLabel(pkg.applicationInfo);
                    } catch (Exception e) {
                        label = pkg.packageName;
                    }
                    final String pkgName = pkg.packageName;
                    final String appLabel = label;

                    Button btn = makeBtn("🎮 " + label, BTN, Color.WHITE);
                    btn.setOnClickListener(v -> {
                        selectedPackage = pkgName;
                        selectedGameLabel.setText("Selected: " + appLabel + "\n" + pkgName);
                        selectedGameLabel.setTextColor(ACCENT);
                        show("Selected: " + appLabel + " (" + pkgName + ")");

                        // Highlight selected
                        for (int i = 0; i < gamesListContainer.getChildCount(); i++) {
                            gamesListContainer.getChildAt(i).setBackgroundColor(BTN);
                        }
                        btn.setBackgroundColor(Color.parseColor("#0D3D2A"));
                    });
                    gamesListContainer.addView(btn);
                }
            });
        }).start();
    }

    // ── Mods Tab ──────────────────────────────────────────────────────────
    private void buildModsTab(LinearLayout c) {
        addSectionLabel(c, "Game Mods");
        addSubLabel(c, "Select a game in the Games tab first");

        String[] mods = {"God Mode", "Fly", "No Clip", "Speed Boost",
            "Spider Climb", "Invisible", "Big Hands", "Super Push",
            "Full Bright", "ESP", "No Fall Damage", "Teleport"};
        boolean[] states = new boolean[mods.length];

        for (int i = 0; i < mods.length; i++) {
            final int idx = i;
            final String name = mods[i];
            Button btn = makeBtn(name + ": OFF", BTN, Color.WHITE);
            btn.setOnClickListener(v -> {
                if (selectedPackage == null) {
                    show("⚠ Select a game in the Games tab first!");
                    return;
                }
                states[idx] = !states[idx];
                btn.setText(name + (states[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(states[idx] ? Color.parseColor("#0D3D2A") : BTN);
                btn.setTextColor(states[idx] ? ACCENT : Color.WHITE);
                show(name + (states[idx] ? " ON" : " OFF") + " for " + selectedPackage +
                    "\nNote: requires patched APK to take effect");
            });
            c.addView(btn);
        }
    }

    // ── System Tab ────────────────────────────────────────────────────────
    private void buildSystemTab(LinearLayout c) {
        addSectionLabel(c, "Quest System");

        addSubLabel(c, "Settings");
        addBtn(c, "Developer Options", BTN, Color.WHITE, v -> {
            try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); }
            catch (Exception e) { show("Error: " + e.getMessage()); }
        });
        addBtn(c, "WiFi Settings", BTN, Color.WHITE, v -> {
            try { startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); }
            catch (Exception e) { show("Error: " + e.getMessage()); }
        });
        addBtn(c, "App Settings for Selected Game", BTN, Color.WHITE, v -> {
            if (selectedPackage == null) { show("Select a game first!"); return; }
            try {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(Uri.parse("package:" + selectedPackage));
                startActivity(i);
            } catch (Exception e) { show("Error: " + e.getMessage()); }
        });

        addSubLabel(c, "Device Info");
        addBtn(c, "Battery Level", BTN, Color.WHITE, v ->
            runAndShow("cat /sys/class/power_supply/battery/capacity"));
        addBtn(c, "CPU Temperature", BTN, Color.WHITE, v ->
            runAndShow("cat /sys/class/thermal/thermal_zone0/temp"));
        addBtn(c, "Available Storage", BTN, Color.WHITE, v ->
            runAndShow("df /sdcard | tail -1"));
        addBtn(c, "Free Memory", BTN, Color.WHITE, v ->
            runAndShow("cat /proc/meminfo | grep -E 'MemFree|MemAvailable'"));
        addBtn(c, "Quest Model", BTN, Color.WHITE, v ->
            runAndShow("getprop ro.product.model"));
        addBtn(c, "Android Version", BTN, Color.WHITE, v ->
            runAndShow("getprop ro.build.version.release"));
        addBtn(c, "IP Address", BTN, Color.WHITE, v ->
            runAndShow("ip addr show wlan0 | grep 'inet '"));
    }

    // ── Patcher Tab ───────────────────────────────────────────────────────
    private void buildPatcherTab(LinearLayout c) {
        addSectionLabel(c, "APK Patcher");
        addSubLabel(c, "Select a game in Games tab first");

        addBtn(c, "📋 Selected Game Info", BTN, Color.WHITE, v -> {
            if (selectedPackage == null) { show("Select a game first!"); return; }
            new Thread(() -> {
                try {
                    PackageInfo info = getPackageManager().getPackageInfo(selectedPackage, 0);
                    ApplicationInfo appInfo = info.applicationInfo;
                    String label = (String) getPackageManager().getApplicationLabel(appInfo);
                    show("Name: " + label +
                        "\nPackage: " + selectedPackage +
                        "\nVersion: " + info.versionName +
                        "\nAPK: " + appInfo.sourceDir +
                        "\nSize: " + new File(appInfo.sourceDir).length() / 1024 / 1024 + " MB");
                } catch (Exception e) {
                    show("Error: " + e.getMessage());
                }
            }).start();
        });

        addBtn(c, "💾 Backup Selected Game APK", ACCENT, Color.BLACK, v -> {
            if (selectedPackage == null) { show("Select a game first!"); return; }
            new Thread(() -> {
                try {
                    ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo(selectedPackage, 0);
                    String appLabel = (String) getPackageManager().getApplicationLabel(appInfo);
                    show("Backing up " + appLabel + "...");
                    File src = new File(appInfo.sourceDir);
                    File dst = new File("/sdcard/Download/" + selectedPackage + "_backup.apk");
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dst);
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    in.close(); out.close();
                    show("✓ Backed up to:\n/sdcard/Download/" + selectedPackage + "_backup.apk\nSize: " +
                        dst.length() / 1024 / 1024 + " MB");
                } catch (Exception e) {
                    show("Error: " + e.getMessage());
                }
            }).start();
        });

        addBtn(c, "📊 Backup ALL Games", BTN, Color.WHITE, v -> {
            new Thread(() -> {
                List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
                int count = 0;
                String[] systemPrefixes = {"com.android", "com.google", "com.facebook",
                    "com.oculus", "com.meta", "android", "com.qualcomm"};
                for (PackageInfo pkg : packages) {
                    if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
                    boolean isSystem = false;
                    for (String prefix : systemPrefixes) {
                        if (pkg.packageName.startsWith(prefix)) { isSystem = true; break; }
                    }
                    if (isSystem) continue;
                    try {
                        File src = new File(pkg.applicationInfo.sourceDir);
                        File dst = new File("/sdcard/Download/" + pkg.packageName + "_backup.apk");
                        InputStream in = new FileInputStream(src);
                        OutputStream out2 = new FileOutputStream(dst);
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) out2.write(buf, 0, len);
                        in.close(); out2.close();
                        count++;
                    } catch (Exception e) { /* skip */ }
                }
                final int finalCount = count;
                show("✓ Backed up " + finalCount + " games to /sdcard/Download/");
            }).start();
        });

        addBtn(c, "📂 List Downloads", BTN, Color.WHITE, v -> {
            File dir = new File("/sdcard/Download/");
            StringBuilder sb = new StringBuilder("Downloads:\n");
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files)
                        sb.append("• ").append(f.getName())
                          .append(" (").append(f.length() / 1024).append(" KB)\n");
                }
            }
            show(sb.toString());
        });
    }

    // ── ADB Tab ───────────────────────────────────────────────────────────
    private void buildADBTab(LinearLayout c) {
        addSectionLabel(c, "Command Runner");
        addSubLabel(c, "Run shell commands on the Quest");

        EditText cmdField = new EditText(this);
        cmdField.setHint("Enter command...");
        cmdField.setHintTextColor(Color.GRAY);
        cmdField.setTextColor(Color.WHITE);
        cmdField.setBackgroundColor(BTN);
        cmdField.setPadding(20, 15, 20, 15);
        cmdField.setTextSize(13);
        cmdField.setTypeface(android.graphics.Typeface.MONOSPACE);
        c.addView(cmdField);

        addBtn(c, "▶ Run", ACCENT, Color.BLACK, v -> {
            String cmd = cmdField.getText().toString().trim();
            if (!cmd.isEmpty()) runAndShow(cmd);
        });

        addSubLabel(c, "Quick Commands");
        String[][] quick = {
            {"Quest Model", "getprop ro.product.model"},
            {"Android Version", "getprop ro.build.version.release"},
            {"Build Number", "getprop ro.build.display.id"},
            {"CPU ABI", "getprop ro.product.cpu.abi"},
            {"Battery", "cat /sys/class/power_supply/battery/capacity"},
            {"Uptime", "uptime"},
            {"Storage", "df /sdcard | tail -1"},
        };
        for (String[] q : quick) {
            Button b = makeBtn(q[0], BTN, Color.WHITE);
            b.setOnClickListener(v -> runAndShow(q[1]));
            c.addView(b);
        }
    }

    // ── Settings Tab ──────────────────────────────────────────────────────
    private void buildSettingsTab(LinearLayout c) {
        addSectionLabel(c, "Settings");

        addSubLabel(c, "Theme");
        LinearLayout themeRow = makeRow();
        String[][] themes = {
            {"Default", "#2EC08B"},
            {"Galaxy", "#7B2FBE"},
            {"Hacker", "#00FF41"},
            {"Red", "#FF3333"}
        };
        for (String[] t : themes) {
            Button b = makeSmallBtn(t[0], BTN, Color.WHITE);
            b.setOnClickListener(v -> {
                ACCENT = Color.parseColor(t[1]);
                outputView.setTextColor(ACCENT);
                Toast.makeText(this, t[0] + " theme!", Toast.LENGTH_SHORT).show();
            });
            themeRow.addView(b);
        }
        c.addView(themeRow);

        addSubLabel(c, "Shizuku");
        addBtn(c, "What is Shizuku?", BTN, Color.WHITE, v ->
            show("Shizuku lets emder.lol run privileged commands\nwithout root. Get it at shizuku.rikka.app\nOnce Shizuku is running, most mods will work!"));
        addBtn(c, "Check if Shizuku is running", BTN, Color.WHITE, v ->
            runAndShow("getprop sys.shizuku.service.version 2>&1 || echo 'Shizuku not found'"));

        addSubLabel(c, "About");
        addBtn(c, "emder.lol — Universal Quest Mod Menu", BTN, Color.GRAY, v ->
            show("emder.lol\nUniversal Meta Quest Mod Menu\nVersion 1.0\nWorks with any Quest game"));
    }

    // ── UI Helpers ────────────────────────────────────────────────────────
    private void addSectionLabel(LinearLayout c, String text) {
        TextView lbl = new TextView(this);
        lbl.setText(text);
        lbl.setTextColor(ACCENT);
        lbl.setTextSize(15);
        lbl.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        lbl.setPadding(0, 10, 0, 8);
        c.addView(lbl);
    }

    private void addSubLabel(LinearLayout c, String text) {
        TextView lbl = new TextView(this);
        lbl.setText(text);
        lbl.setTextColor(Color.GRAY);
        lbl.setTextSize(11);
        lbl.setPadding(0, 12, 0, 4);
        c.addView(lbl);
    }

    private void addBtn(LinearLayout c, String text, int bg, int fg, View.OnClickListener l) {
        Button btn = makeBtn(text, bg, fg);
        btn.setOnClickListener(l);
        c.addView(btn);
    }

    private Button makeBtn(String text, int bg, int fg) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(fg);
        btn.setBackgroundColor(bg);
        btn.setTextSize(12);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 5, 0, 5);
        btn.setLayoutParams(p);
        return btn;
    }

    private Button makeSmallBtn(String text, int bg, int fg) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(fg);
        btn.setBackgroundColor(bg);
        btn.setTextSize(11);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        p.setMargins(4, 4, 4, 4);
        btn.setLayoutParams(p);
        return btn;
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }
}
