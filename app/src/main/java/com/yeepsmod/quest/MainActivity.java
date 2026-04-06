package com.yeepsmod.quest;

import android.app.Activity;
import android.app.AlertDialog;
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
    private boolean adbConnected = false;
    private String adbBinaryPath = null;

    private static final int SHIZUKU_REQUEST_CODE = 1001;

    private final Shizuku.OnRequestPermissionResultListener permissionResultListener =
        (requestCode, grantResult) -> {
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                shizukuAvailable = grantResult == PackageManager.PERMISSION_GRANTED;
                show(shizukuAvailable ?
                    "✓ Shizuku connected!\nNow tap Scan All Apps in the Games tab." :
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

        TextView statusView = new TextView(this);
        statusView.setText(ModAccessibilityService.instance != null ? "⚡ Service Active" :
            shizukuAvailable ? "⚡ Shizuku" : "○ Limited");
        statusView.setTextColor(ModAccessibilityService.instance != null || shizukuAvailable ? ACCENT : Color.GRAY);
        statusView.setTextSize(11);
        statusView.setBackgroundColor(BTN);
        statusView.setPadding(14, 6, 14, 6);
        header.addView(statusView);

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
        outputView.setText("emder.lol ready — enable accessibility service in Settings");
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh status when coming back to app
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
                show("Shizuku not running.\nOpen Shizuku app and start the service first.");
                return;
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                shizukuAvailable = true;
                show("✓ Shizuku already connected!");
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

    private void buildGamesTab(LinearLayout c) {
        addSectionLabel(c, "All Apps");
        addSubLabel(c, "Tap any app to select it as the mod target");

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
        show("Scanning...");

        new Thread(() -> {
            List<String[]> apps = new ArrayList<>();
            try {
                List<PackageInfo> packages = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
                for (PackageInfo pkg : packages) {
                    String label;
                    try {
                        label = (String) getPackageManager().getApplicationLabel(pkg.applicationInfo);
                    } catch (Exception e) {
                        label = pkg.packageName;
                    }
                    apps.add(new String[]{pkg.packageName, label});
                }
            } catch (Exception e) { /* ignore */ }

            runOnUiThread(() -> {
                gamesListContainer.removeAllViews();
                if (apps.isEmpty()) {
                    show("No apps found.");
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

    private void buildModsTab(LinearLayout c) {
        addSectionLabel(c, "Game Mods");
        addSubLabel(c, "Enable accessibility service in Settings first");

        TextView accStatus = new TextView(this);
        accStatus.setText(ModAccessibilityService.instance != null ?
            "✓ Mod Service Active" : "✗ Mod Service Not Active — go to Settings");
        accStatus.setTextColor(ModAccessibilityService.instance != null ? ACCENT : RED);
        accStatus.setTextSize(12);
        accStatus.setBackgroundColor(BTN);
        accStatus.setPadding(20, 15, 20, 15);
        c.addView(accStatus);

        addBtn(c, "⚡ Enable Mod Service", ACCENT, Color.BLACK, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception e) {
                show("Error: " + e.getMessage());
            }
        });

        addBtn(c, "📖 Read Screen Content", BTN, Color.WHITE, v -> {
            String content = ModAccessibilityService.getScreenContent();
            show("Screen:\n" + (content.length() > 500 ?
                content.substring(0, 500) + "..." : content));
        });

        addBtn(c, "📋 Yeeps Event Log", BTN, Color.WHITE, v -> {
            String log = ModAccessibilityService.screenLog.toString();
            show(log.isEmpty() ? "No Yeeps events yet — open Yeeps first" :
                log.length() > 500 ? log.substring(log.length() - 500) : log);
        });

        addBtn(c, "🔄 Clear Event Log", BTN, Color.GRAY, v -> {
            ModAccessibilityService.screenLog = new StringBuilder();
            show("Log cleared");
        });

        addSubLabel(c, "Gesture Controls (Yeeps must be open)");

        addBtn(c, "👆 Tap Center Screen", BTN, Color.WHITE, v -> {
            ModAccessibilityService.tap(960, 540);
            show("Tapped center screen");
        });

        addBtn(c, "👆 Tap Top Left", BTN, Color.WHITE, v -> {
            ModAccessibilityService.tap(100, 100);
            show("Tapped top left");
        });

        addBtn(c, "👆 Swipe Up", BTN, Color.WHITE, v -> {
            ModAccessibilityService.swipe(960, 700, 960, 300);
            show("Swiped up");
        });

        addBtn(c, "👆 Swipe Down", BTN, Color.WHITE, v -> {
            ModAccessibilityService.swipe(960, 300, 960, 700);
            show("Swiped down");
        });

        addSubLabel(c, "System Mods (needs Shizuku/ADB)");

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
                states[idx] = !states[idx];
                btn.setText(name + (states[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(states[idx] ? Color.parseColor("#0D3D2A") : BTN);
                btn.setTextColor(states[idx] ? ACCENT : Color.WHITE);
                show(name + (states[idx] ? " ON" : " OFF"));
            });
            c.addView(btn);
        }
    }

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
        addBtn(c, "Accessibility Settings", BTN, Color.WHITE, v -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
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

        addSubLabel(c, "Quest Performance (needs Shizuku)");
        addBtn(c, "Set 120hz", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.refreshRate 120"));
        addBtn(c, "Set 90hz", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.refreshRate 90"));
        addBtn(c, "Set 72hz", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.refreshRate 72"));
        addBtn(c, "Disable Guardian", RED, Color.WHITE, v -> runAndShow("setprop debug.oculus.guardian.enable 0"));
        addBtn(c, "Enable Guardian", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.guardian.enable 1"));
        addBtn(c, "High Resolution (2048)", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.textureWidth 2048"));
        addBtn(c, "Default Resolution (1536)", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.textureWidth 1536"));
        addBtn(c, "Low Resolution (1024)", BTN, Color.WHITE, v -> runAndShow("setprop debug.oculus.textureWidth 1024"));

        addSubLabel(c, "Device Info");
        addBtn(c, "Battery Level", BTN, Color.WHITE, v -> runAndShow("cat /sys/class/power_supply/battery/capacity"));
        addBtn(c, "Available Storage", BTN, Color.WHITE, v -> runAndShow("df /sdcard | tail -1"));
        addBtn(c, "Free Memory", BTN, Color.WHITE, v -> runAndShow("cat /proc/meminfo | grep -E 'MemFree|MemAvailable'"));
        addBtn(c, "Quest Model", BTN, Color.WHITE, v -> runAndShow("getprop ro.product.model"));
        addBtn(c, "Android Version", BTN, Color.WHITE, v -> runAndShow("getprop ro.build.version.release"));
        addBtn(c, "IP Address", BTN, Color.WHITE, v -> runAndShow("ip addr show wlan0 | grep 'inet '"));
    }

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
                    show("Error: " + e.getMessage());
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
                    show("Error: " + e.getMessage());
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
            {"Storage", "df /sdcard | tail -1"},
            {"Memory", "cat /proc/meminfo | grep MemAvailable"},
            {"Find Yeeps", "pm list packages 2>&1 | grep -i yeep"},
            {"Find Gorilla Tag", "pm list packages 2>&1 | grep -i gorilla"},
            {"Running processes", "ps -A | head -30"},
        };
        for (String[] q : quick) {
            Button b = makeBtn(q[0], BTN, Color.WHITE);
            b.setOnClickListener(v -> runAndShow(q[1]));
            c.addView(b);
        }
    }

    private void buildSettingsTab(LinearLayout c) {
        addSectionLabel(c, "Settings");

        addSubLabel(c, "Accessibility Service");
        addBtn(c, "⚡ Enable Mod Service", ACCENT, Color.BLACK, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception e) {
                show("Error: " + e.getMessage());
            }
        });
        addBtn(c, "Service Status", BTN, Color.WHITE, v ->
            show(ModAccessibilityService.instance != null ?
                "✓ Mod service is running!\nReady to interact with games." :
                "✗ Mod service not running.\nGo to Accessibility Settings and enable emder.lol"));

        addSubLabel(c, "Shizuku");
        addBtn(c, "⚡ Connect Shizuku", ACCENT, Color.BLACK, v -> requestShizuku());
        addBtn(c, "Check Shizuku Status", BTN, Color.WHITE, v -> {
            checkShizuku();
            show(shizukuAvailable ?
                "✓ Shizuku connected!" :
                "✗ Shizuku not connected.\nOpen Shizuku app and start the service.");
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
