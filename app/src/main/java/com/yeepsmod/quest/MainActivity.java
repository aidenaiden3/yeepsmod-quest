package com.yeepsmod.quest;

import android.app.Activity;
import android.content.Intent;
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
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private int BG = Color.parseColor("#0A0A0A");
    private int ACCENT = Color.parseColor("#2EC08B");
    private int BTN = Color.parseColor("#1A1A1A");
    private int RED = Color.parseColor("#CC2424");

    private LinearLayout[] tabContents;
    private Button[] tabBtns;
    private TextView outputView;

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
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(tlp);
        header.addView(title);

        TextView ver = new TextView(this);
        ver.setText("V1");
        ver.setTextColor(Color.GRAY);
        ver.setTextSize(11);
        ver.setBackgroundColor(BTN);
        ver.setPadding(14, 6, 14, 6);
        header.addView(ver);

        main.addView(header);

        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#222222"));
        main.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        String[] tabNames = {"Players", "Mods", "System", "Patcher", "ADB", "Settings"};
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

            if (i == 0) buildPlayersTab(content);
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

        main.addView(scrollView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        outputView = new TextView(this);
        outputView.setText("emder.lol ready.");
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
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void runAndShow(String cmd) {
        new Thread(() -> {
            String result = runCmd(cmd);
            runOnUiThread(() -> outputView.setText("$ " + cmd + "\n" + result));
        }).start();
    }

    private void show(String msg) {
        outputView.setText(msg);
    }

    // ── Players Tab ───────────────────────────────────────────────────────
    private void buildPlayersTab(LinearLayout c) {
        addSectionLabel(c, "Players");
        addSubLabel(c, "Requires Shizuku or patched Yeeps to show real players");

        addBtn(c, "Is Yeeps VR Running?", BTN, Color.WHITE, v ->
            runAndShow("ps -A | grep -i yeep | grep -v grep"));

        addBtn(c, "Is Companion Running?", BTN, Color.WHITE, v ->
            runAndShow("ps -A | grep -i G2Companion | grep -v grep"));

        addBtn(c, "Launch Yeeps VR", ACCENT, Color.BLACK, v -> {
            try {
                Intent i = getPackageManager().getLaunchIntentForPackage("com.TrassGames.Yeeps");
                if (i != null) startActivity(i);
                else show("Yeeps VR not found on this device");
            } catch (Exception e) {
                show("Error: " + e.getMessage());
            }
        });

        addBtn(c, "Launch Companion App", BTN, Color.WHITE, v -> {
            try {
                Intent i = getPackageManager().getLaunchIntentForPackage("com.TrassGames.G2Companion");
                if (i != null) startActivity(i);
                else show("Companion app not found");
            } catch (Exception e) {
                show("Error: " + e.getMessage());
            }
        });
    }

    // ── Mods Tab ──────────────────────────────────────────────────────────
    private void buildModsTab(LinearLayout c) {
        addSectionLabel(c, "Yeeps Mods");
        addSubLabel(c, "Patch Yeeps APK first via the Patcher tab");

        String[] mods = {"God Mode", "Fly", "No Clip", "Speed Boost",
            "Spider Climb", "Invisible", "Big Hands", "Super Push",
            "Full Bright", "ESP"};
        boolean[] states = new boolean[mods.length];

        for (int i = 0; i < mods.length; i++) {
            final int idx = i;
            final String name = mods[i];
            Button btn = makeBtn(name + ": OFF", BTN, Color.WHITE);
            btn.setOnClickListener(v -> {
                states[idx] = !states[idx];
                btn.setText(name + (states[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(states[idx] ? Color.parseColor("#0D3D2A") : BTN);
                btn.setTextColor(states[idx] ? ACCENT : Color.WHITE);
                show(name + (states[idx] ? " ON" : " OFF") + " — patch Yeeps first!");
            });
            c.addView(btn);
        }
    }

    // ── System Tab ────────────────────────────────────────────────────────
    private void buildSystemTab(LinearLayout c) {
        addSectionLabel(c, "Quest System");

        addSubLabel(c, "Settings Pages");
        addBtn(c, "Open Developer Options", BTN, Color.WHITE, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            } catch (Exception e) { show("Error: " + e.getMessage()); }
        });

        addBtn(c, "Open WiFi Settings", BTN, Color.WHITE, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) { show("Error: " + e.getMessage()); }
        });

        addBtn(c, "Open Yeeps App Settings", BTN, Color.WHITE, v -> {
            try {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(Uri.parse("package:com.TrassGames.Yeeps"));
                startActivity(i);
            } catch (Exception e) { show("Yeeps not found"); }
        });

        addBtn(c, "Open Companion App Settings", BTN, Color.WHITE, v -> {
            try {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(Uri.parse("package:com.TrassGames.G2Companion"));
                startActivity(i);
            } catch (Exception e) { show("Companion not found"); }
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
        addSubLabel(c, "Find and backup game APKs");

        addBtn(c, "📦 Find Yeeps VR APK", ACCENT, Color.BLACK, v ->
            runAndShow("pm path com.TrassGames.Yeeps 2>&1"));

        addBtn(c, "📦 Find Companion APK", BTN, Color.WHITE, v ->
            runAndShow("pm path com.TrassGames.G2Companion 2>&1"));

        addBtn(c, "📋 Yeeps Version", BTN, Color.WHITE, v -> {
            new Thread(() -> {
                try {
                    android.content.pm.PackageInfo info = getPackageManager()
                        .getPackageInfo("com.TrassGames.Yeeps", 0);
                    runOnUiThread(() -> show("Yeeps VR v" + info.versionName +
                        " (code " + info.versionCode + ")"));
                } catch (Exception e) {
                    runOnUiThread(() -> show("Yeeps VR not found on this device"));
                }
            }).start();
        });

        addBtn(c, "📋 Companion Version", BTN, Color.WHITE, v -> {
            new Thread(() -> {
                try {
                    android.content.pm.PackageInfo info = getPackageManager()
                        .getPackageInfo("com.TrassGames.G2Companion", 0);
                    runOnUiThread(() -> show("Companion v" + info.versionName +
                        " (code " + info.versionCode + ")"));
                } catch (Exception e) {
                    runOnUiThread(() -> show("Companion app not found on this device"));
                }
            }).start();
        });

        addBtn(c, "💾 Copy Yeeps APK to Downloads", ACCENT, Color.BLACK, v -> {
            new Thread(() -> {
                try {
                    android.content.pm.ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo("com.TrassGames.Yeeps", 0);
                    String apkPath = appInfo.sourceDir;
                    runOnUiThread(() -> show("Found APK at: " + apkPath + "\nCopying..."));

                    File src = new File(apkPath);
                    File dst = new File("/sdcard/Download/Yeeps_backup.apk");
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dst);
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    in.close();
                    out.close();

                    runOnUiThread(() -> show("✓ Saved to /sdcard/Download/Yeeps_backup.apk\nSize: " + dst.length() / 1024 / 1024 + " MB"));
                } catch (Exception e) {
                    runOnUiThread(() -> show("Error: " + e.getMessage() +
                        "\nYeeps VR may not be installed on this Quest"));
                }
            }).start();
        });

        addBtn(c, "💾 Copy Companion APK to Downloads", BTN, Color.WHITE, v -> {
            new Thread(() -> {
                try {
                    android.content.pm.ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo("com.TrassGames.G2Companion", 0);
                    String apkPath = appInfo.sourceDir;
                    File src = new File(apkPath);
                    File dst = new File("/sdcard/Download/YeepsCompanion_backup.apk");
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dst);
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    in.close();
                    out.close();
                    runOnUiThread(() -> show("✓ Saved to /sdcard/Download/YeepsCompanion_backup.apk\nSize: " + dst.length() / 1024 / 1024 + " MB"));
                } catch (Exception e) {
                    runOnUiThread(() -> show("Error: " + e.getMessage()));
                }
            }).start();
        });

        addBtn(c, "📊 List All Sideloaded Apps", BTN, Color.WHITE, v -> {
            new Thread(() -> {
                StringBuilder sb = new StringBuilder("Installed packages:\n");
                try {
                    java.util.List<android.content.pm.PackageInfo> packages =
                        getPackageManager().getInstalledPackages(0);
                    for (android.content.pm.PackageInfo pkg : packages) {
                        if ((pkg.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                            sb.append("• ").append(pkg.packageName).append("\n");
                        }
                    }
                } catch (Exception e) {
                    sb.append("Error: ").append(e.getMessage());
                }
                String result = sb.toString();
                runOnUiThread(() -> show(result.length() > 500 ? result.substring(0, 500) + "..." : result));
            }).start();
        });

        addBtn(c, "📂 List Downloads Folder", BTN, Color.WHITE, v -> {
            new Thread(() -> {
                File dir = new File("/sdcard/Download/");
                StringBuilder sb = new StringBuilder("Files in Downloads:\n");
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null && files.length > 0) {
                        for (File f : files) {
                            sb.append("• ").append(f.getName())
                              .append(" (").append(f.length() / 1024).append(" KB)\n");
                        }
                    } else {
                        sb.append("Empty folder");
                    }
                } else {
                    sb.append("Could not access Downloads folder");
                }
                runOnUiThread(() -> show(sb.toString()));
            }).start();
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
            {"CPU Info", "getprop ro.product.cpu.abi"},
            {"Battery", "cat /sys/class/power_supply/battery/capacity"},
            {"Uptime", "uptime"},
            {"Hostname", "getprop net.hostname"},
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

        addSubLabel(c, "About");
        addBtn(c, "emder.lol — Version 1.0", BTN, Color.GRAY, v ->
            show("emder.lol VR Mod Menu\nMade for Yeeps"));
        addBtn(c, "Get Shizuku for more features", ACCENT, Color.BLACK, v -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://shizuku.rikka.app/"));
                startActivity(i);
            } catch (Exception e) { show("Open shizuku.rikka.app in your browser"); }
        });
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
