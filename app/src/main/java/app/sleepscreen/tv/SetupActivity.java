package app.sleepscreen.tv;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SetupActivity extends Activity {
    private static final String OEM_SERVICE_ACTION =
            "android.settings.ACCESSIBILITY_SHORTCUT_SERVICES_TV_OEM_LINK";
    private TextView status;
    private final Handler handler = new Handler();
    private final Runnable updateStatus = this::showStatus;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 48), Ui.dp(this, 30), Ui.dp(this, 48), Ui.dp(this, 30));
        root.setBackgroundColor(0xff0b0d12);
        TextView title = Ui.text(this, 30, Color.WHITE);
        title.setText("Set up Sleep Screen");
        root.addView(title);
        TextView text = Ui.text(this, 17, 0xffdddddd);
        text.setText("Enable Sleep Screen accessibility for the sleep timer and screen filters. " +
                "Then allow display over other apps and alarms if offered. " +
                "Remote button mapping uses the optional Sleep Screen Remote service.");
        root.addView(text);
        status = Ui.text(this, 17, 0xffcccccc);
        root.addView(status);

        TextView direct = Ui.row(root, 650);
        direct.setText("Open Sleep Screen accessibility");
        direct.setOnClickListener(v -> openServiceSettings());
        TextView standard = Ui.row(root, 650);
        standard.setText("Open accessibility settings");
        standard.setOnClickListener(v -> open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        TextView alt = Ui.row(root, 650);
        alt.setText("Find alternate accessibility settings");
        alt.setOnClickListener(v -> startActivity(new Intent(this, SettingsPickerActivity.class)));
        TextView info = Ui.row(root, 650);
        info.setText("Open Sleep Screen app info");
        info.setOnClickListener(v -> open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()))));
        TextView overlay = Ui.row(root, 650);
        overlay.setText("Allow display over other apps");
        overlay.setOnClickListener(v -> open(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()))));
        TextView alarm = Ui.row(root, 650);
        alarm.setText("Allow alarms and reminders");
        alarm.setOnClickListener(v -> open(new Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM",
                Uri.parse("package:" + getPackageName()))));
        TextView map = Ui.row(root, 650);
        map.setText("Configure remote button");
        map.setOnClickListener(v -> startActivity(new Intent(this, MapperActivity.class)));
        ScrollView sc = new ScrollView(this);
        sc.addView(root);
        setContentView(sc);
        direct.requestFocus();
    }

    private void openServiceSettings() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo service = null;
        if (manager != null) {
            for (AccessibilityServiceInfo candidate : manager.getInstalledAccessibilityServiceList()) {
                if (candidate.getResolveInfo() == null) continue;
                ServiceInfo info = candidate.getResolveInfo().serviceInfo;
                if (info != null && getPackageName().equals(info.packageName)
                        && SleepService.class.getName().equals(info.name)) {
                    service = candidate;
                    break;
                }
            }
        }
        if (service != null) {
            Intent direct = new Intent(OEM_SERVICE_ACTION);
            direct.setPackage("com.android.tv.settings");
            direct.putExtra("accessibilityServiceInfo", service);
            try {
                startActivity(direct);
                return;
            } catch (RuntimeException ignored) { }
        }
        Toast.makeText(this, "Direct service settings unavailable; opening accessibility settings.",
                Toast.LENGTH_LONG).show();
        open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    @Override protected void onResume() {
        super.onResume();
        showStatus();
        handler.postDelayed(updateStatus, 1500);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(updateStatus);
        super.onPause();
    }

    private void showStatus() {
        if (status == null) return;
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        ComponentName target = new ComponentName(this, SleepService.class);
        boolean selected = false;
        if (enabled != null) {
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
            splitter.setString(enabled);
            for (String entry : splitter) {
                if (target.equals(ComponentName.unflattenFromString(entry))) { selected = true; break; }
            }
        }
        if (SleepService.running()) status.setText("Sleep Screen accessibility: connected");
        else if (selected) status.setText("Sleep Screen accessibility: enabled, waiting for connection");
        else status.setText("Sleep Screen accessibility: off");
    }

    private void open(Intent intent) {
        try { startActivity(intent); }
        catch (RuntimeException e) {
            Toast.makeText(this, "This settings page is not available on this device", Toast.LENGTH_LONG).show();
        }
    }
}

