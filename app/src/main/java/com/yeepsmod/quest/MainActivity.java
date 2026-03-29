package com.yeepsmod.quest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.InputStreamReader;
import java.io.File;

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

        // Divider
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#222222"));
        main.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        // Tabs
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

        // Output console
        outputView = new TextView(this);
        outputView.setText("emder.lol ready.");
        outputView.setTextColor(ACCENT);
        outputView.setBackgroundColor(Color.parseColor("#050505"));
        outputView.setTextSize(10);
        outputView.setPadding(20, 10, 20, 10);
        outputView.setTypeface(android.graphics.Typeface.MONOSPACE);
        outputView.setMaxLines(4);
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
            return sb.length() > 0 ? sb.toString().trim() : "Done.";
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

    // ── Players Tab ───────────────────────────────────────────────────────
    private void buildPlayersTab(LinearLayout c) {
        addSectionLabel(c, "Players");
        addSubLabel(c, "Player list requires Yeeps to be patched first");
        addBtn(c, "↻ Refresh Players", ACCENT, Color.BLACK, v ->
            Toast.makeText(this, "Patch Yeeps first via the Patcher tab!", Toast.LENGTH_LONG).show());
        addBtn(c, "Check if Yeeps is running", BTN, Color.WHITE, v ->
            runAndShow("ps aux | grep -i yeep | grep -v grep"));
        addBtn(c, "Launch Yeeps VR", BTN, Color.WHITE, v ->
            runAndShow("am start -n com.TrassGames.Yeeps/com.unity3d.player.UnityPlayerActivity"));
        addBtn(c, "Launch Yeeps Companion", BTN, Color.WHITE, v ->
            runAndShow("am start -n com.TrassGames.G2Companion/com.unity3d.player.UnityPlayerActivity"));
    }

    // ── Mods Tab ──────────────────────────────────────────────────────────
    private void buildModsTab(LinearLayout c) {
        addSectionLabel(c, "Yeeps Mods");
        addSubLabel(c, "Patch Yeeps APK first — then these will work");

        String[] mods = {"God Mode", "Fly", "No Clip", "Speed Boost",
            "Spider Climb", "Invisible", "Big Hands", "Super Push", "Full Bright", "ESP"};
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
                outputView.setText(name + (states[idx] ? " enabled" : " disabled") + " — requires patched APK");
            });
            c.addView(btn);
        }
    }

    // ── System Tab ────────────────────────────────────────────────────────
    private void buildSystemTab(LinearLayout c) {
        addSectionLabel(c, "Quest System");

        addSubLabel(c, "Open Settings Pages");
        addBtn(c, "Open Quest Settings", BTN, Color.WHITE, v -> {
            try {
                Intent i = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivity(i);
            } catch (Exception e) {
                outputView.setText("Error: " + e.getMessage());
            }
        });

        addBtn(c, "Open WiFi Settings", BTN, Color.WHITE, v -> {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) {
                outputView.setText("Error: " + e.getMessage());
            }
        });

        addBtn(c, "Open App Settings for Yeeps", BTN, Color.WHITE, v -> {
            try {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(Uri.parse("package:com.TrassGames.Yeeps"));
                startActivity(i);
            } catch (Exception e) {
                outputView.setText("Error: " + e.getMessage());
            }
        });

        addSubLabel(c, "Device Info");
        addBtn(c, "Battery Level", BTN, Color.WHITE, v ->
            runAndShow("cat /sys/class/power_supply/battery/capacity"));
        addBtn(c, "CPU Temperature", BTN, Color.WHITE, v ->
            runAndShow("cat /sys/class/thermal/thermal_zone0/temp"));
        addBtn(c, "Available Storage", BTN, Color.WHITE, v ->
            runAndShow("df /sdcard | tail -1"));
        addBtn(c, "IP Address", BTN, Color.WHITE, v ->
            runAndShow("ip addr show wlan0 | grep 'inet '"));
        addBtn(c, "Free Memory", BTN, Color.WHITE, v ->
            runAndShow("cat /proc/meminfo | grep -E 'MemTotal|MemFree|MemAvailable'"));
    }

    // ── Patcher Tab ───────────────────────────────────────────────────────
    private void buildPatcherTab(LinearLayout c) {
        addSectionLabel(c, "Yeeps APK Patcher");
        addSubLabel(c, "Extract and backup the Yeeps APK");

        addBtn(c, "📦 Find Yeeps APK Path", ACCENT, Color.BLACK, v ->
            runAndShow("pm path com.TrassGames.Yeeps"));

        addBtn(c, "📋 Yeeps Version Info", BTN, Color.WHITE, v ->
            runAndShow("pm dump com.TrassGames.Yeeps | grep versionName"));

        addBtn(c, "💾 Copy Yeeps APK to Downloads", ACCENT, Color.BLACK, v -> {
            new Thread(() -> {
                String path = runCmd("pm path com.TrassGames.Yeeps | cut -d: -f2 | tr -d ' \n'");
                if (path.startsWith("/")) {
                    String result = runCmd("cp " + path + " /sdcard/Download/Yeeps_backup.apk");
                    runOnUiThread(() -> outputView.setText("Saved to /sdcard/Download/Yeeps_backup.apk\n" + result));
                } else {
                    runOnUiThread(() -> outputView.setText("Could not find Yeeps APK: " + path));
                }
            }).start();
        });

        addBtn(c, "📊 List All Games", BTN, Color.WHITE, v ->
            runAndShow("pm list packages | grep -v android | grep -v com.oculus | grep -v com.facebook | grep -v com.meta"));

        addBtn(c, "📂 Open Downloads Folder", BTN, Color.WHITE, v -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"), "resource/folder");
                startActivity(i);
            } catch (Exception e) {
                outputView.setText("Use a file manager to access /sdcard/Download/");
            }
        });
    }

    // ── ADB Tab ───────────────────────────────────────────────────────────
    private void buildADBTab(LinearLayout c) {
        addSectionLabel(c, "Command Runner");
        addSubLabel(c, "Run shell commands directly on the Quest");

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
            {"List running processes", "ps aux | grep -v grep | head -20"},
            {"List all packages", "pm list packages"},
            {"Free memory", "cat /proc/meminfo | grep Mem"},
            {"CPU info", "cat /proc/cpuinfo | grep 'Hardware' | head -1"},
            {"Android version", "getprop ro.build.version.release"},
            {"Quest model", "getprop ro.product.model"},
            {"List files in Downloads", "ls /sdcard/Download/"},
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
                Toast.makeText(this, t[0] + " theme applied!", Toast.LENGTH_SHORT).show();
            });
            themeRow.addView(b);
        }
        c.addView(themeRow);

        addSubLabel(c, "About");
        addBtn(c, "emder.lol — Version 1.0", BTN, Color.GRAY, v ->
            Toast.makeText(this, "emder.lol VR Mod Menu", Toast.LENGTH_SHORT).show());
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
