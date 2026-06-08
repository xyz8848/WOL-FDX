package de.florianisme.wakeonlan.shortcuts;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.content.pm.ShortcutManagerCompat;

import java.util.List;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.util.AppLogger;

public class DynamicShortcutManager {

    private static final String TAG = "DynamicShortcutManager";
    public static final int SHORTCUT_AMOUNT_LIMIT = 4;

    public void updateShortcuts(Context context, List<Device> devices) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            AppLogger.i(TAG, "Shortcuts not supported on API " + Build.VERSION.SDK_INT);
            return;
        }

        try {
            removeOldShortcuts(context);
            publishShortcuts(context, devices);
            AppLogger.i(TAG, "Shortcuts updated successfully, count: " + Math.min(devices.size(), SHORTCUT_AMOUNT_LIMIT));
        } catch (Exception e) {
            AppLogger.w(TAG, "Failed to update shortcuts (GMS may not be available)", e);
        }
    }

    private void publishShortcuts(Context context, List<Device> devices) {
        devices.stream()
                .sorted((device1, device2) -> Integer.compare(device2.id, device1.id))
                .map(device -> DeviceShortcutMapper.buildShortcut(device, context))
                .limit(SHORTCUT_AMOUNT_LIMIT)
                .forEach(shortcut -> ShortcutManagerCompat.pushDynamicShortcut(context, shortcut));
    }

    private void removeOldShortcuts(Context context) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context);
    }

}
