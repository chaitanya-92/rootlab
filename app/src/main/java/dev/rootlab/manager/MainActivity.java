package dev.rootlab.manager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Minimal Root Lab dashboard. No donation / support-us / learn-KernelSU links.
 * Root status is not inferred from installation or device metadata.
 */
public class MainActivity extends Activity {
    private TextView status;
    private Button verify;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        final int space = dp(16);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(247, 247, 248));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(space, dp(32), space, space);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Root Lab");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        LinearLayout statusCard = card(root);
        addText(statusCard, "Root status", 16, true);
        status = addText(statusCard, "Not verified", 15, false);
        verify = new Button(this);
        verify.setText("Verify root");
        statusCard.addView(verify);
        verify.setOnClickListener(v -> verifyRoot());

        DeviceSnapshot device = DeviceSnapshot.read();
        LinearLayout deviceCard = card(root);
        addText(deviceCard, "Manager version", 15, true);
        addText(deviceCard, BuildConfig.VERSION_NAME, 14, false);
        addText(deviceCard, "Kernel version", 15, true);
        addText(deviceCard, device.kernel, 14, false);
        addText(deviceCard, "Device model", 15, true);
        addText(deviceCard, device.manufacturer + " " + device.model, 14, false);
        addText(deviceCard, "Fingerprint", 15, true);
        addText(deviceCard, device.fingerprint, 12, false);

        LinearLayout developerCard = card(root);
        addLink(developerCard, "Telegram: @Who_colki", "https://t.me/Who_colki");
        TextView credit = addText(developerCard, "Built by LORD H3X", 16, true);
        credit.setGravity(Gravity.CENTER);
        credit.setTextColor(Color.BLACK);
        addLink(developerCard, "GitHub: @chaitanya-92", "https://github.com/chaitanya-92");

        setContentView(scroll);
    }

    private void verifyRoot() {
        verify.setEnabled(false);
        status.setText("Checking existing root access...");
        new Thread(() -> {
            RootCheck.Result result = RootCheck.verify();
            runOnUiThread(() -> {
                switch (result.state) {
                    case GRANTED:
                        status.setText("Root granted (su verified). KernelSU backend not verified.");
                        break;
                    case DENIED:
                        status.setText("Root not granted.");
                        break;
                    case UNAVAILABLE:
                        status.setText("Root not available.");
                        break;
                    default:
                        status.setText("Root verification unsuccessful.");
                        break;
                }
                verify.setEnabled(true);
            });
        }, "root-check").start();
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(18));
        body.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(18);
        parent.addView(body, lp);
        return body;
    }

    private TextView addText(LinearLayout parent, String value, int size, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(bold ? Color.rgb(25,25,25) : Color.rgb(95,95,95));
        if (bold) text.setTypeface(null, Typeface.BOLD);
        text.setPadding(0, dp(5), 0, dp(5));
        parent.addView(text);
        return text;
    }

    private void addLink(LinearLayout parent, String label, String url) {
        Button link = new Button(this);
        link.setAllCaps(false);
        link.setText(label);
        parent.addView(link);
        link.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (android.content.ActivityNotFoundException error) {
                Toast.makeText(this, "No app available to open this link", Toast.LENGTH_LONG).show();
            }
        });
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
