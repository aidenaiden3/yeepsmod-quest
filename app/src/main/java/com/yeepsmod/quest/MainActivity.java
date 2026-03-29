package com.yeepsmod.quest;

import android.app.Activity;
import android.graphics.Color;
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

public class MainActivity extends Activity {

    private int BG = Color.parseColor("#0A0A0A");
    private int ACCENT = Color.parseColor("#2EC08B");
    private int BTN = Color.parseColor("#1A1A1A");
    private int RED = Color.parseColor("#CC2424");
    private int BORDER = Color.parseColor("#333333");

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

        FrameLayout.LayoutParams mainParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);
        root.addView(main, mainParams);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(Color.parseColor("#111111"));
        header.setPadding(30, 20, 30, 20);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("YeepsMod");
        title.setTextColor(ACCENT);
        title.setTextSize(20);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        title.setLayoutParams(tlp);
        header.addView(title);

        TextView ver = new TextView(this);
        ver.setText("V2");
        ver.setTextColor(Color.GRAY);
        ver.setTextSize(11);
        ver.setBackgroundColor(BTN);
        ver.setPadding(14, 6, 14, 6);
        header.addView(ver);

        main.addView(header);

        // Tab bar
        String[] tabNames = {"Players", "Mods", "System", "Patcher", "ADB", "Settings"};
        tabContents = new LinearLayout[tabNames.length];
        tabBtns = new Button[tabNames.length];

        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.parseColor("#111111"));

        ScrollView scrollView = new ScrollView(this);
        LinearLayout contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(20, 20, 20, 20);
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

        View div = new View(this);
        div.setBackgroundColor(BORDER);
        main.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        main.addView(scrollView, scrollParams);

        // Output console at bottom
        outputView = new TextView(this);
        outputView.setText("YeepsMod ready. Run ADB commands above.");
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

    // ── Run ADB/shell command ─────────────────────────────────────────────
    private String runCmd(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = stdout.readLine()) != null) sb.append(line).append("\n");
            while ((line = stderr.readLine()) != null) sb.append("[ERR] ").append(line).append("\n");
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
        addSectionLabel(c, "Players in Lobby");
        addBtn(c, "↻ Refresh", ACCENT, Color.BLACK, v -> {
            Toast.makeText(this, "Join a Yeeps lobby first!", Toast.LENGTH_SHORT).show();
        });
        addBtn(c, "Show Photon Room Info", BTN, Color.WHITE, v ->
            runAndShow("dumpsys activity com.TrassGames.G2Companion"));
    }

    // ── Mods Tab ──────────────────────────────────────────────────────────
    private void buildModsTab(LinearLayout c) {
        addSectionLabel(c, "Yeeps Mods");
        addSubLabel(c, "These work after patching the Yeeps APK");

        String[][] mods = {
            {"God Mode", "setprop yeepsmod.godmode 1"},
            {"Fly", "setprop yeepsmod.fly 1"},
            {"No Clip", "setprop yeepsmod.noclip 1"},
            {"Speed Boost", "setprop yeepsmod.speed 1"},
            {"Spider Climb", "setprop yeepsmod.spider 1"},
            {"Invisible", "setprop yeepsmod.invisible 1"},
            {"Big Hands", "setprop yeepsmod.bighands 1"},
            {"Super Push", "setprop yeepsmod.push 1"},
            {"Full Bright", "setprop yeepsmod.fullbright 1"},
            {"ESP", "setprop yeepsmod.esp 1"}
        };

        boolean[] states = new boolean[mods.length];
        for (int i = 0; i < mods.length; i++) {
            final int idx = i;
            final String name = mods[i][0];
            final String onCmd = mods[i][1];
            final String offCmd = onCmd.replace(" 1", " 0");
            Button btn = makeBtn(name + ": OFF", BTN, Color.WHITE);
            btn.setOnClickListener(v -> {
                states[idx] = !states[idx];
                btn.setText(name + (states[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(states[idx] ? Color.parseColor("#0D3D2A") : BTN);
                btn.setTextColor(states[idx] ? ACCENT : Color.WHITE);
                runAndShow(states[idx] ? onCmd : offCmd);
            });
            c.addView(btn);
        }
    }

    // ── System Tab ────────────────────────────────────────────────────────
    private void buildSystemTab(LinearLayout c) {
        addSectionLabel(c, "Quest System Settings");

        addSubLabel(c, "Refresh Rate");
        String[][] rates = {
            {"120hz", "setprop debug.oculus.refreshRate 120"},
            {"90hz", "setprop debug.oculus.refreshRate 90"},
            {"72hz", "setprop debug.oculus.refreshRate 72"},
            {"60hz", "setprop debug.oculus.refreshRate 60"}
        };
        LinearLayout rateRow = makeRow();
        for (String[] r : rates) {
            Button b = makeSmallBtn(r[0], BTN, Color.WHITE);
            b.setOnClickListener(v -> runAndShow(r[1]));
            rateRow.addView(b);
        }
        c.addView(rateRow);

        addSubLabel(c, "Guardian");
        LinearLayout guardRow = makeRow();
        Button disableGuardian = makeSmallBtn("Disable", RED, Color.WHITE);
        disableGuardian.setOnClickListener(v -> runAndShow("setprop debug.oculus.guardian.enable 0"));
        Button enableGuardian = makeSmallBtn("Enable", BTN, Color.WHITE);
        enableGuardian.setOnClickListener(v -> runAndShow("setprop debug.oculus.guardian.enable 1"));
        guardRow.addView(disableGuardian);
        guardRow.addView(enableGuardian);
        c.addView(guardRow);

        addSubLabel(c, "Resolution");
        String[][] resolutions = {
            {"2048 (High)", "setprop debug.oculus.textureWidth 2048"},
            {"1536 (Default)", "setprop debug.oculus.textureWidth 1536"},
            {"1024 (Low)", "setprop debug.oculus.textureWidth 1024"}
        };
        for (String[] r : resolutions) {
            Button b = makeBtn(r[0], BTN, Color.WHITE);
            b.setOnClickListener(v -> runAndShow(r[1]));
            c.addView(b);
        }

        addSubLabel(c, "Other");
        addBtn(c, "Disable Chromatic Aberration", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.chromaticAberration 0"));
        addBtn(c, "Enable Chromatic Aberration", BTN, Color.WHITE, v ->
            runAndShow("setprop debug.oculus.chromaticAberration 1"));
        addBtn(c, "Reboot Headset", RED, Color.WHITE, v ->
            runAndShow("reboot"));
    }

    // ── Patcher Tab ───────────────────────────────────────────────────────
    private void buildPatcherTab(LinearLayout c) {
        addSectionLabel(c, "Yeeps APK Patcher");
        addSubLabel(c, "Extract, patch and reinstall Yeeps with mods built in");

        addBtn(c, "📦 Extract Yeeps APK", ACCENT, Color.BLACK, v -> {
            runAndShow("pm path com.TrassGames.Yeeps");
        });

        addBtn(c, "📋 List Installed Games", BTN, Color.WHITE, v ->
            runAndShow("pm list packages | grep -i yeep"));

        addBtn(c, "📊 Yeeps App Info", BTN, Color.WHITE, v ->
            runAndShow("dumpsys package com.TrassGames.Yeeps | grep -E 'versionName|versionCode|firstInstall'"));

        addBtn(c, "🗂 List Yeeps Files", BTN, Color.WHITE, v ->
            runAndShow("ls /data/app/com.TrassGames.Yeeps*/"));

        addBtn(c, "💾 Copy APK to Storage", ACCENT, Color.BLACK, v ->
            runAndShow("cp $(pm path com.TrassGames.Yeeps | cut -d: -f2) /sdcard/Yeeps_backup.apk && echo 'Saved to /sdcard/Yeeps_backup.apk'"));
    }

    // ── ADB Tab ───────────────────────────────────────────────────────────
    private void buildADBTab(LinearLayout c) {
        addSectionLabel(c, "ADB Command Runner");
        addSubLabel(c, "Run any shell command directly on the Quest");

        EditText cmdField = new EditText(this);
        cmdField.setHint("Enter command...");
        cmdField.setHintTextColor(Color.GRAY);
        cmdField.setTextColor(Color.WHITE);
        cmdField.setBackgroundColor(BTN);
        cmdField.setPadding(20, 15, 20, 15);
        cmdField.setTextSize(13);
        cmdField.setTypeface(android.graphics.Typeface.MONOSPACE);
        c.addView(cmdField);

        addBtn(c, "▶ Run Command", ACCENT, Color.BLACK, v -> {
            String cmd = cmdField.getText().toString().trim();
            if (!cmd.isEmpty()) runAndShow(cmd);
        });

        addSubLabel(c, "Quick Commands");
        String[][] quick = {
            {"List processes", "ps aux | grep -i yeep"},
            {"Free memory", "free -m"},
            {"CPU info", "cat /proc/cpuinfo | grep 'model name' | head -1"},
            {"Battery", "dumpsys battery | grep level"},
            {"IP Address", "ifconfig wlan0 | grep inet"},
            {"Running apps", "am stack list"},
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
        String[][] themes = {{"Default", "#2EC08B"}, {"Galaxy", "#7B2FBE"}, {"Hacker", "#00FF41"}, {"Red", "#FF3333"}};
        for (String[] t : themes) {
            Button b = makeSmallBtn(t[0], BTN, Color.WHITE);
            b.setOnClickListener(v -> {
                ACCENT = Color.parseColor(t[1]);
                Toast.makeText(this, t[0] + " theme applied — restart to see fully", Toast.LENGTH_SHORT).show();
            });
            themeRow.addView(b);
        }
        c.addView(themeRow);

        addSubLabel(c, "App Info");
        addBtn(c, "Open Quest Settings", BTN, Color.WHITE, v ->
            runAndShow("am start -a android.settings.SETTINGS"));
        addBtn(c, "Open File Manager", BTN, Color.WHITE, v ->
            runAndShow("am start -n com.android.documentsui/.files.FilesActivity"));
        addBtn(c, "App Version: 2.0", BTN, Color.GRAY, v -> {});
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
