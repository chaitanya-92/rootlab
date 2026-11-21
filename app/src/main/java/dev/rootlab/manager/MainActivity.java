package dev.rootlab.manager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        int padding = Math.round(18 * getResources().getDisplayMetrics().density);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        scroll.addView(root);
        TextView title = new TextView(this);
        title.setText("Root Lab — Universal Manager");
        title.setTextSize(23);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);
        addText(root, "\nDevice profile (automatic, read-only)", 18);
        addText(root, DeviceSnapshot.read().display(), 14);
        addText(root, "\nRoot status: NOT VERIFIED", 18);
        status = addText(root, "A manager APK cannot install root by itself. Tap below to request a real root check. This may display a superuser prompt.", 14);
        Button verify = new Button(this);
        verify.setText("Verify existing root access");
        root.addView(verify);
        verify.setOnClickListener(v -> {
            verify.setEnabled(false);
            status.setText("Checking existing root authorization...");
            new Thread(() -> {
                RootCheck.Result result = RootCheck.verify();
                runOnUiThread(() -> {
                    status.setText(result.state + ": " + result.detail);
                    verify.setEnabled(true);
                });
            }, "root-check").start();
        });
        addText(root, "\nBackend integrations", 18);
        for (BackendAdapter backend : BackendRegistry.available()) {
            addText(root, backend.displayName() + " — " + backend.readiness().name(), 14);
        }
        addText(root, "\nImportant: generic root verification does NOT enable superuser policy management, modules, kernel installation, or support for every device. Backend-specific authenticated APIs and device-tested installation paths are required.", 14);
        addText(root, "\\nConnect with the developer", 18);
        addExternalLink(root, "Telegram: @Who_colki", "https://t.me/Who_colki");
        addExternalLink(root, "GitHub: @chaitanya-92", "https://github.com/chaitanya-92");
        setContentView(scroll);
    }
    private void addExternalLink(LinearLayout parent, String label, String url) {
        Button link = new Button(this);
        link.setText(label);
        parent.addView(link);
        link.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException ignored) {
                android.widget.Toast.makeText(this, "No app available to open link", android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }
    private TextView addText(LinearLayout parent, String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setPadding(0, 8, 0, 8);
        parent.addView(t);
        return t;
    }
}
