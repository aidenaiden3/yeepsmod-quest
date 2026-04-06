package com.yeepsmod.quest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class ModAccessibilityService extends AccessibilityService {

    public static ModAccessibilityService instance = null;
    public static String lastPackage = null;
    public static StringBuilder screenLog = new StringBuilder();

    @Override
    public void onServiceConnected() {
        instance = this;
        screenLog.append("✓ Accessibility service connected\n");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        lastPackage = String.valueOf(event.getPackageName());

        // Log events from Yeeps
        if ("com.TrassGames.Yeeps".equals(lastPackage)) {
            String text = String.valueOf(event.getText());
            if (!text.isEmpty() && !text.equals("[]")) {
                screenLog.append("Yeeps: " + text + "\n");
                if (screenLog.length() > 2000) {
                    screenLog.delete(0, 500);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        instance = null;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    // Get all text visible on screen
    public static String getScreenContent() {
        if (instance == null) return "Service not connected";
        try {
            AccessibilityNodeInfo root = instance.getRootInActiveWindow();
            if (root == null) return "No window content";
            StringBuilder sb = new StringBuilder();
            extractText(root, sb);
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static void extractText(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) return;
        if (node.getText() != null) {
            sb.append(node.getText()).append("\n");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            extractText(node.getChild(i), sb);
        }
    }

    // Simulate a tap at x,y coordinates
    public static void tap(float x, float y) {
        if (instance == null) return;
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        instance.dispatchGesture(builder.build(), null, null);
    }

    // Simulate a swipe
    public static void swipe(float x1, float y1, float x2, float y2) {
        if (instance == null) return;
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        instance.dispatchGesture(builder.build(), null, null);
    }
}
