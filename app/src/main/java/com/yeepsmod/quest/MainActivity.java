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
import rikka.shizuku.ShizukuRemoteProcess;

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

        // Register Shizuku permission listener
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
        main.addView(outputView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

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

    private void requestShizuku() {
        try {
            if (!Shizuku.pingBinder()) {
                show("Shizuku is not running.\nOpen the Shizuku app and start the service first.");
                return;
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                shizukuAvailable = true;
                show("✓ Shizuku already connected!\nFull access enabled.\n\nNow tap Scan All Apps in the Games tab.");
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                show("Go to Shizuku app and manually grant permission to emder.lol");
            } else {
                Shizuku.requestPermission(SHIZUKU_REQUEST_CODE);
            }
        } catch (Exception e) {
            show("Shizuku error: " + e.getMessage());
        }
    }

    private String runCmd(String cmd) {
        try {
            Process p;
            if (shizukuAvailable) {
                // Use Shizuku for elevated permissions
                p = new ShizukuRemoteProcess("sh", new String[]{"-c", cmd}, null);
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
        addSectionLabel(c, "All Apps");
        addSubLabel(c, "Connect Shizuku first, then scan to see all games");

        selectedGameLabel = new TextView(this);
        selectedGameLabel.setText("No app selected");
        selectedGameLabel.setTextColor(Color.GRAY);
        selectedGameLabel.setTextSize(12);
        selectedGameLabel.setBackgroundColor(BTN);
        selectedGameLabel.setPadding(20, 15, 20, 15);
        c.addView(selectedGameLabel);

        addBtn(c, "↻ Scan All Apps", ACCENT, Color.BLACK, v -> scanGames());

        gamesListContainer = new LinearLayout(this);
        gamesListContainer.setOrientation(LinearLayout.VERTICAL);
        c.addView(gamesListContainer);
    }

    private void scanGames() {
        gamesListContainer.removeAllViews();
        show("Scanning for apps" + (shizukuAvailable ? " (elevated)" : " (limited — connect Shizuku for games)") + "...");

        new Thread(() -> {
            List<String[]> apps = new ArrayList<>();

            // Use Shizuku elevated pm list packages to find ALL apps including VR games
            String pmOutput = runCmd("pm list packages -f 2>&1");
            if (pmOutput != null && pmOutput.contains("package:")) {
                for (String line : pmOutput.split("\n")) {
                    if (line.startsWith("package:") || line.contains("=")) {
                        try {
                            // Format: package:/path/to/apk=com.package.name
                            String pkgName = line.contains("=") ?
                                line.substring(line.lastIndexOf("=") + 1).trim() :
                                line.replace("package:", "").trim();

                            if (pkgName.isEmpty()) continue;

                            String label;
                            try {
                                PackageInfo info = getPackageManager().getPackageInfo(pkgName, 0);
                                label = (String) getPackageManager().getApplicationLabel(info.applicationInfo);
                            } catch (Exception e) {
                                label = pkgName;
                            }
                            apps.add(new String[]{pkgName, label});
                        } catch (Exception e) { /* skip */ }
                    }
                }
            }

            // Fallback — also add what PackageManager can see
            try {
                List<PackageInfo> pmApps = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
                List<String> existing = new ArrayList<>();
                for (String[] app : apps) existing.add(app[0]);
                for (PackageInfo pkg : pmApps) {
                    if (!existing.contains(pkg.packageName)) {
                        String label;
                        try {
                            label = (String) getPackageManager().getApplicationLabel(pkg.applicationInfo);
                        } catch (Exception e) {
                            label = pkg.packageName;
                        }
                        apps.add(new String[]{pkg.packageName, label});
                    }
                }
            } catch (Exception e) { /* ignore */ }

            runOnUiThread(() -> {
                gamesListContainer.removeAllViews();
                if (apps.isEmpty()) {
                    show("No apps found. Make sure Shizuku is connected.");
                    return;
                }
                show("Found " + apps.size() + " apps — tap one to select");

                for (String[] app : apps) {
                    final String pkgName = app[0];
                    final String appLabel = app[1];

                    Button btn = makeBtn("🎮 " + appLabel, BTN, Color.WHITE);
                    btn.setOnClickListener(v -> {
                        selectedPackage = pkgName;
                        selectedAppLabel = appLabel;
                        selectedGameLabel.setText("✓ " + appLabel);
                        selectedGameLabel.setTextColor(ACCENT);
                        show("Selected: " + appLabel + "\n" + pkgName);
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
        addSubLabel(c, "Select an app first. Requires Shizuku for full effect.");

        String[] mods = {
            "God Mode", "Fly", "No Clip", "Speed Boost",
            "Spider Climb", "Invisible", "Big Hands", "Super Push",
            "Full Bright", "ESP", "No Fall Damage", "Teleport"
        };
        boolean[] states = new boolean[mods.length];

        for (int i = 0; i < mods.length; i++) {
            final int idx = i;
            final String name = mods[i];
            Button btn = makeBtn(name + ": OFF", BTN, Color.WHITE);
            btn.setOnClickListener(v -> {
                if (selectedPackage == null) {
                    show("⚠ Select an app in the Games tab first!");
                    return;
                }
                states[idx] = !states[idx];
                btn.setText(name + (states[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(states[idx] ? Color.parseColor("#0D3D2A") : BTN);
                btn.setTextColor(states[idx] ? ACCENT : Color.WHITE);

                if (shizukuAvailable) {
                    String propName = "mod." + name.toLowerCase().replace(" ", "_");
                    runAndShow("setprop " + propName + " " + (states[idx] ? "1" : "0"));
                } else {
                    show(name + (states[idx] ? " ON" : " OFF") + " for " + selectedAppLabel +
                        "\n⚠ Connect Shizuku in Settings for this to work!");
                }
            });
            c.addView(btn);
        }
    }

    // ── System Tab ────────────────────────────────────────────────────────
    private void buildSystemTab(LinearLayout c) {
        addSectionLabel(c, "Quest System");

        addSubLabel(c, "Settings Pages");
        addBtn(c, "Developer Options", BTN, Color.WHITE, v -> {
            try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); }
            catch (Exception e) { show("Error: " + e.getMessage()); }
        });
        addBtn(c, "WiFi Settings", BTN, Color.WHITE, v -> {
            try { startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); }
            catch (Exception e) { show("Error: " + e.getMessage()); }
        });
        addBtn(c, "Selected App Settings", BTN, Color.WHITE, v -> {
            if (selectedPackage == null) { show("Select an app first!"); return; }
            try {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(Uri.parse("package:" + selectedPackage));
                startActivity(i);
            } catch (Exception e) { show("Error: " + e.getMessage()); }
        });

        addSubLabel(c, "Quest Commands (needs Shizuku)");
        addBtn(c, "Set 120hz", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.refreshRate 120"));
        addBtn(c, "Set 90hz", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.refreshRate 90"));
        addBtn(c, "Set 72hz", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.refreshRate 72"));
        addBtn(c, "Disable Guardian", RED, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.guardian.enable 0"));
        addBtn(c, "Enable Guardian", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.guardian.enable 1"));
        addBtn(c, "High Resolution (2048)", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.textureWidth 2048"));
        addBtn(c, "Default Resolution (1536)", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.textureWidth 1536"));
        addBtn(c, "Low Resolution (1024)", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.textureWidth 1024"));

        addSubLabel(c, "Device Info");
        addBtn(c, "Battery Level", BTN, Color.WHITE, v ->
            runAndShow("cat /sys/class/power_supply/battery/capacity"));
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
        addSubLabel(c, "Select an app in Games tab first");

        addBtn(c, "📋 Selected App Info", BTN, Color.WHITE, v -> {
            if (selectedPackage == null) { show("Select an app first!"); return; }
            new Thread(() -> {
                try {
                    PackageInfo info = getPackageManager().getPackageInfo(selectedPackage, 0);
                    show("Name: " + selectedAppLabel +
                        "\nPackage: " + selectedPackage +
                        "\nVersion: " + info.versionName +
                        "\nAPK: " + info.applicationInfo.sourceDir +
                        "\nSize: " + new File(info.applicationInfo.sourceDir).length() / 1024 / 1024 + " MB");
                } catch (Exception e) {
                    // Try via shell
                    String path = runCmd("pm path " + selectedPackage + " 2>&1");
                    show("Name: " + selectedAppLabel +
                        "\nPackage: " + selectedPackage +
                        "\n" + path);
                }
            }).start();
        });

        addBtn(c, "💾 Backup Selected App", ACCENT, Color.BLACK, v -> {
            if (selectedPackage == null) { show("Select an app first!"); return; }
            new Thread(() -> {
                try {
                    ApplicationInfo appInfo = getPackageManager().getApplicationInfo(selectedPackage, 0);
                    show("Backing up " + selectedAppLabel + "...");
                    File src = new File(appInfo.sourceDir);
                    File dst = new File("/sdcard/Download/" + selectedPackage + "_backup.apk");
                    copyFile(src, dst);
                    show("✓ Saved!\n/sdcard/Download/" + selectedPackage + "_backup.apk\n" +
                        dst.length() / 1024 / 1024 + " MB");
                } catch (Exception e) {
                    // Try via shell with elevated perms
                    String path = runCmd("pm path " + selectedPackage + " 2>&1 | cut -d: -f2 | tr -d ' '");
                    if (path.startsWith("/")) {
                        String result = runCmd("cp " + path + " /sdcard/Download/" + selectedPackage + "_backup.apk 2>&1");
                        show("✓ Backed up via shell!\n" + result);
                    } else {
                        show("Error: " + e.getMessage() + "\n" + path);
                    }
                }
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

    private void copyFile(File src, File dst) throws Exception {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        in.close();
        out.close();
    }

    // ── ADB Tab ───────────────────────────────────────────────────────────
    private void buildADBTab(LinearLayout c) {
        addSectionLabel(c, "Command Runner");
        addSubLabel(c, "Elevated with Shizuku when connected");

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
            {"Battery", "cat /sys/class/power_supply/battery/capacity"},
            {"Uptime", "uptime"},
            {"Storage", "df /sdcard | tail -1"},
            {"Memory", "cat /proc/meminfo | grep MemAvailable"},
            {"List ALL packages", "pm list packages -f 2>&1 | head -50"},
            {"List user packages only", "pm list packages -3 -f 2>&1"},
            {"Find Yeeps", "pm list packages 2>&1 | grep -i yeep"},
            {"Find Gorilla Tag", "pm list packages 2>&1 | grep -i gorilla"},
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

        addSubLabel(c, "Shizuku — Required for game scanning");
        addBtn(c, "⚡ Connect Shizuku", ACCENT, Color.BLACK, v -> requestShizuku());
        addBtn(c, "Check Shizuku Status", BTN, Color.WHITE, v -> {
            checkShizuku();
            show(shizukuAvailable ?
                "✓ Shizuku connected!\nGame scanning and mods enabled." :
                "✗ Shizuku not connected.\n\n1. Open Shizuku app\n2. Start the service\n3. Tap Connect Shizuku above\n4. Grant permission when prompted");
        });

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
        addBtn(c, "emder.lol v1.0", BTN, Color.GRAY, v ->
            show("emder.lol\nUniversal Meta Quest Mod Menu\nVersion 1.0"));
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
