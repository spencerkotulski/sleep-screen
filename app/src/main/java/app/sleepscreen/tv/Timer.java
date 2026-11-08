package app.sleepscreen.tv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * The one timer: when it ends, and the two alarms behind it. The warning alarm fires a minute early and opens
 * WarningActivity; the sleep alarm, a moment after the end, is the backstop that puts the TV to sleep even if
 * the warning never showed.
 */
final class Timer {
    private Timer() { }

    static final int[] MINUTES = {15, 30, 60, 90, 120};
    static final long WARN_MS = 60_000;
    static final String ACTION_WARN = "app.sleepscreen.tv.WARN";
    static final String ACTION_SLEEP = "app.sleepscreen.tv.SLEEP";
    static final String ACTION_TEST = "app.sleepscreen.tv.TEST";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences("timer", Context.MODE_PRIVATE);
    }

    /** When the TV goes to sleep, in wall-clock millis, or 0 when no timer is on. */
    static long end(Context ctx) {
        long end = prefs(ctx).getLong("end", 0);
        return end > System.currentTimeMillis() - 5_000 ? end : 0;
    }

    static boolean paused(Context ctx) { return prefs(ctx).getLong("paused_ms", 0) > 0; }
    static long remaining(Context ctx) { return paused(ctx) ? prefs(ctx).getLong("paused_ms", 0) : Math.max(0, end(ctx) - System.currentTimeMillis()); }
    static void pause(Context ctx) { long left = remaining(ctx); if (left <= 0 || paused(ctx)) return; AlarmManager am = ctx.getSystemService(AlarmManager.class); am.cancel(pending(ctx, ACTION_WARN)); am.cancel(pending(ctx, ACTION_SLEEP)); prefs(ctx).edit().remove("end").putLong("paused_ms", left).apply(); SleepService.refresh(); }
    static void resume(Context ctx) { long left = prefs(ctx).getLong("paused_ms", 0); if (left > 0) startSeconds(ctx, (int)Math.max(1, (left + 999) / 1000)); }

    static void start(Context ctx, int minutes) {
        startSeconds(ctx, minutes * 60);
    }

    static void startSeconds(Context ctx, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        prefs(ctx).edit().remove("paused_ms").putLong("end", end).apply();
        schedule(ctx, ACTION_WARN, Math.max(System.currentTimeMillis(), end - WARN_MS));
        schedule(ctx, ACTION_SLEEP, end + 2_000);
        SleepService.refresh();
    }

    static void cancel(Context ctx) {
        prefs(ctx).edit().remove("end").remove("paused_ms").apply();
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        am.cancel(pending(ctx, ACTION_WARN));
        am.cancel(pending(ctx, ACTION_SLEEP));
        SleepService.refresh();
    }

    /** Ends the timer and puts the TV to sleep. False when the accessibility service is off. */
    static boolean sleepNow(Context ctx) {
        cancel(ctx);
        return SleepService.sleep();
    }

    static boolean canSleep() {
        return SleepService.running();
    }

    private static void schedule(Context ctx, String action, long at) {
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        PendingIntent pi = pending(ctx, action);
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    private static PendingIntent pending(Context ctx, String action) {
        Intent intent = new Intent(ctx, AlarmReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
