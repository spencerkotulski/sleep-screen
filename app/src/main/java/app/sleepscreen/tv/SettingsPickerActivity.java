package app.sleepscreen.tv;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** A small Activity Launcher-style picker for the device's accessible Settings screens. */
public class SettingsPickerActivity extends Activity {
    private static final String[] PACKAGES = {
        "com.android.settings", "com.android.tv.settings", "com.google.android.tv.settings"
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 48), Ui.dp(this, 30), Ui.dp(this, 48), Ui.dp(this, 30));
        root.setBackgroundColor(0xff0b0d12);
        TextView title = Ui.text(this, 30, Color.WHITE);
        title.setText("Find accessibility settings");
        root.addView(title);
        TextView help = Ui.text(this, 17, 0xffdddddd);
        help.setText("Try an accessibility screen below, then enable Sleep Screen. " +
            "If the switch turns off, try another screen. Some devices block access or disable the service themselves.");
        root.addView(help);

        PackageManager pm = getPackageManager();
        ArrayList<ActivityInfo> found = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String pkg : PACKAGES) {
            try {
                PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
                if (info.activities == null) continue;
                for (ActivityInfo a : info.activities) {
                    String label = String.valueOf(a.loadLabel(pm));
                    String name = (a.name + " " + label).toLowerCase(Locale.ROOT);
                    if (a.exported && a.enabled && a.applicationInfo.enabled &&
                        (name.contains("accessibility") || name.contains("accessibilitysettings"))) {
                        if (seen.add(a.packageName + "/" + a.name)) found.add(a);
                    }
                }
            } catch (PackageManager.NameNotFoundException ignored) { }
        }
        Collections.sort(found, Comparator.comparing(a -> a.packageName + a.name));
        for (ActivityInfo a : found) {
            TextView row = Ui.row(root, 900);
            row.setText(a.loadLabel(pm) + "\n" + a.packageName + "/" + a.name);
            row.setTextSize(16);
            row.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName(a.packageName, a.name));
                    startActivity(intent);
                } catch (RuntimeException e) {
                    Toast.makeText(this, "This screen is blocked on this device. Try another.", Toast.LENGTH_LONG).show();
                }
            });
        }
        if (found.isEmpty()) {
            TextView none = Ui.text(this, 17, 0xffdddddd);
            none.setText("No alternate accessibility screens were found. Try the standard settings button below.");
            root.addView(none);
        }
        TextView standard = Ui.row(root, 900);
        standard.setText("Open standard accessibility settings");
        standard.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (RuntimeException e) { Toast.makeText(this, "Settings is unavailable", Toast.LENGTH_LONG).show(); }
        });
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}

