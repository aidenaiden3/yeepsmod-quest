package com.yeepsmod.quest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.parseColor("#141A24"));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView text = new TextView(this);
        text.setText("YeepsMod VR");
        text.setTextColor(Color.parseColor("#2EC08B"));
        text.setTextSize(24);
        layout.addView(text);

        TextView text2 = new TextView(this);
        text2.setText("Loading...");
        text2.setTextColor(Color.WHITE);
        text2.setTextSize(16);
        layout.addView(text2);

        setContentView(layout);
    }
}
