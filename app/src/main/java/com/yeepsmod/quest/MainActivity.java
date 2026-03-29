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

public class MainActivity extends Activity {

    private int BG_COLOR = Color.parseColor("#141A24");
    private int ACCENT_COLOR = Color.parseColor("#2EC08B");
    private int BTN_COLOR = Color.parseColor("#262D3D");
    private int RED_COLOR = Color.parseColor("#CC2424");

    private LinearLayout[] tabContents;
    private boolean[] modStates = new boolean[10];
    private String[] modNames = {
        "God Mode", "Fly", "No Clip", "Speed Boost",
        "Spider Climb", "Invisible", "Big Hands",
        "Super Push", "Full Bright", "ESP"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackgroundColor(BG_COLOR);
        menu.setPadding(30, 30, 30, 30);

        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(600, FrameLayout.LayoutParams.WRAP_CONTENT);
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.topMargin = 20;
        menuParams.leftMargin = 20;
        root.addView(menu, menuParams);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView header = new TextView(this);
        header.setText("YeepsMod VR");
        header.setTextColor(ACCENT_COLOR);
        header.setTextSize(22);
        header.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        header.setLayoutParams(headerParams);
        headerRow.addView(header);

        TextView version = new TextView(this);
        version.setText("V1");
        version.setTextColor(Color.GRAY);
        version.setTextSize(12);
        version.setBackgroundColor(BTN_COLOR);
        version.setPadding(16, 8, 16, 8);
        headerRow.addView(version);

        menu.addView(headerRow);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#1A2030"));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        divParams.setMargins(0, 15, 0, 15);
        menu.addView(divider, divParams);

        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.parseColor("#0D1219"));

        String[] tabNames = {"Players", "Mods", "Profile"};
        tabContents = new LinearLayout[tabNames.length];
        Button[] tabBtns = new Button[tabNames.length];

        ScrollView scrollView = new ScrollView(this);
        LinearLayout contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(0, 10, 0, 10);
        scrollView.addView(contentArea);

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;

            Button tabBtn = new Button(this);
            tabBtn.setText(tabNames[i]);
            tabBtn.setTextColor(i == 0 ? ACCENT_COLOR : Color.GRAY);
            tabBtn.setBackgroundColor(i == 0 ? Color.parseColor("#1A2030") : Color.TRANSPARENT);
            tabBtn.setTextSize(13);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tabBtn.setLayoutParams(tp);
            tabBtns[i] = tabBtn;
            tabBar.addView(tabBtn);

            LinearLayout tabContent = new LinearLayout(this);
            tabContent.setOrientation(LinearLayout.VERTICAL);
            tabContent.setPadding(0, 10, 0, 10);
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
                    tabBtns[j].setBackgroundColor(j == idx ? Color.parseColor("#1A2030") : Color.TRANSPARENT);
                }
            });
        }

        menu.addView(tabBar);

        View divider2 = new View(this);
        divider2.setBackgroundColor(Color.parseColor("#1A2030"));
        LinearLayout.LayoutParams div2Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        menu.addView(divider2, div2Params);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500);
        menu.addView(scrollView, scrollParams);

        setContentView(root);
    }

    private void buildPlayersTab(LinearLayout container) {
        TextView info = makeLabel("Players in Lobby", 14, true);
        info.setTextColor(ACCENT_COLOR);
        container.addView(info);

        TextView sub = makeLabel("Join a Yeeps lobby to see players", 11, false);
        sub.setTextColor(Color.GRAY);
        sub.setPadding(0, 4, 0, 16);
        container.addView(sub);

        Button refreshBtn = makeButton("↻ Refresh Players", ACCENT_COLOR, Color.BLACK);
        refreshBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Player list coming soon!", Toast.LENGTH_SHORT).show();
        });
        container.addView(refreshBtn);

        String[] fakePlayers = {"🥽 Player1 • Actor #1", "🥽 Player2 • Actor #2", "📱 MobileUser • Actor #3"};
        for (String player : fakePlayers) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(BTN_COLOR);
            row.setPadding(20, 15, 20, 15);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 4, 0, 4);
            row.setLayoutParams(rp);

            TextView nameLbl = makeLabel(player, 12, false);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            nameLbl.setLayoutParams(nlp);
            row.addView(nameLbl);

            Button colorBtn = new Button(this);
            colorBtn.setText("🎨");
            colorBtn.setBackgroundColor(ACCENT_COLOR);
            colorBtn.setTextColor(Color.BLACK);
            colorBtn.setTextSize(12);
            LinearLayout.LayoutParams cbp = new LinearLayout.LayoutParams(100, 70);
            cbp.setMargins(8, 0, 8, 0);
            colorBtn.setLayoutParams(cbp);
            row.addView(colorBtn);

            Button kickBtn = new Button(this);
            kickBtn.setText("Kick");
            kickBtn.setBackgroundColor(RED_COLOR);
            kickBtn.setTextColor(Color.WHITE);
            kickBtn.setTextSize(12);
            kickBtn.setLayoutParams(new LinearLayout.LayoutParams(120, 70));
            row.addView(kickBtn);

            container.addView(row);
        }
    }

    private void buildModsTab(LinearLayout container) {
        TextView lbl = makeLabel("Game Mods", 14, true);
        lbl.setTextColor(ACCENT_COLOR);
        lbl.setPadding(0, 0, 0, 16);
        container.addView(lbl);

        for (int i = 0; i < modNames.length; i++) {
            final int idx = i;
            Button btn = makeButton(modNames[i] + ": OFF", BTN_COLOR, Color.WHITE);
            btn.setOnClickListener(v -> {
                modStates[idx] = !modStates[idx];
                btn.setText(modNames[idx] + (modStates[idx] ? ": ON" : ": OFF"));
                btn.setBackgroundColor(modStates[idx] ? Color.parseColor("#1A4A35") : BTN_COLOR);
                btn.setTextColor(modStates[idx] ? ACCENT_COLOR : Color.WHITE);
                Toast.makeText(this, modNames[idx] + (modStates[idx] ? " enabled" : " disabled"), Toast.LENGTH_SHORT).show();
            });
            container.addView(btn);
        }
    }

    private void buildProfileTab(LinearLayout container) {
        TextView lbl1 = makeLabel("Change Display Name", 14, true);
        lbl1.setTextColor(ACCENT_COLOR);
        container.addView(lbl1);

        TextView sub1 = makeLabel("Changes your name in the lobby", 11, false);
        sub1.setTextColor(Color.GRAY);
        sub1.setPadding(0, 4, 0, 12);
        container.addView(sub1);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText nameField = new EditText(this);
        nameField.setHint("Enter name...");
        nameField.setHintTextColor(Color.GRAY);
        nameField.setTextColor(Color.WHITE);
        nameField.setBackgroundColor(BTN_COLOR);
        nameField.setPadding(20, 15, 20, 15);
        nameField.setTextSize(13);
        LinearLayout.LayoutParams nfp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nfp.setMargins(0, 0, 8, 0);
        nameField.setLayoutParams(nfp);
        nameRow.addView(nameField);

        Button setBtn = makeButton("Set", ACCENT_COLOR, Color.BLACK);
        setBtn.setLayoutParams(new LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT));
        setBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString();
            if (!name.isEmpty()) {
                Toast.makeText(this, "Name set to: " + name, Toast.LENGTH_SHORT).show();
                nameField.setText("");
            }
        });
        nameRow.addView(setBtn);
        container.addView(nameRow);

        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#1A2030"));
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dp.setMargins(0, 20, 0, 20);
        container.addView(div, dp);

        TextView lbl2 = makeLabel("Change My Color", 14, true);
        lbl2.setTextColor(ACCENT_COLOR);
        container.addView(lbl2);

        TextView sub2 = makeLabel("Tap a color to apply it to your Yeep", 11, false);
        sub2.setTextColor(Color.GRAY);
        sub2.setPadding(0, 4, 0, 12);
        container.addView(sub2);

        String[] colorNames = {"Red", "Blue", "Green", "Pink", "Gold", "White", "Orange", "Purple", "Cyan", "Black"};
        int[] colorVals = {
            Color.parseColor("#E63333"), Color.parseColor("#3366E6"),
            Color.parseColor("#33BF4D"), Color.parseColor("#E666B3"),
            Color.parseColor("#E6BF1A"), Color.parseColor("#E6E6E6"),
            Color.parseColor("#E67F1A"), Color.parseColor("#9933E6"),
            Color.parseColor("#00E5E5"), Color.parseColor("#111111")
        };

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < colorNames.length; i++) {
            final String cn = colorNames[i];
            final int cv = colorVals[i];
            Button cb = new Button(this);
            cb.setText(cn);
            cb.setTextColor(i == 5 || i == 9 ? Color.BLACK : Color.WHITE);
            cb.setBackgroundColor(cv);
            cb.setTextSize(11);
            cb.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, 90, 1);
            cp.setMargins(4, 4, 4, 4);
            cb.setLayoutParams(cp);
            cb.setOnClickListener(v -> Toast.makeText(this, "Color set to: " + cn, Toast.LENGTH_SHORT).show());
            if (i < 5) row1.addView(cb);
            else row2.addView(cb);
        }

        container.addView(row1);
        container.addView(row2);

        View div2 = new View(this);
        div2.setBackgroundColor(Color.parseColor("#1A2030"));
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dp2.setMargins(0, 20, 0, 20);
        container.addView(div2, dp2);

        Button emptyBtn = makeButton("👻  Become Empty Yeep", RED_COLOR, Color.WHITE);
        emptyBtn.setOnClickListener(v -> Toast.makeText(this, "Empty Yeep applied!", Toast.LENGTH_SHORT).show());
        container.addView(emptyBtn);
    }

    private TextView makeLabel(String text, float size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(size);
        if (bold) tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return tv;
    }

    private Button makeButton(String text, int bgColor, int textColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(textColor);
        btn.setBackgroundColor(bgColor);
        btn.setTextSize(13);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 6, 0, 6);
        btn.setLayoutParams(params);
        return btn;
    }
}
