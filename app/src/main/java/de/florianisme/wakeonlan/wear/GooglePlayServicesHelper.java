package de.florianisme.wakeonlan.wear;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Helper class to check Google Play Services availability for Wear OS features.
 * Uses PackageManager first to check if GMS is installed and enabled,
 * then falls back to reflection for the actual availability check.
 * This avoids touching GMS classes on devices where GMS is disabled,
 * preventing system process kills on platforms like HyperOS3.
 */
public class GooglePlayServicesHelper {

    private static final String TAG = "GmsAvailability";
    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static Boolean sIsAvailable = null;

    private GooglePlayServicesHelper() {
        // Utility class
    }

    /**
     * Check if Google Play Services (GMS) is available on this device.
     * Caches the result after the first call.
     * First verifies the GMS package is installed and enabled via PackageManager
     * (no GMS class access), then uses reflection to check actual availability.
     *
     * @param context Application or Activity context
     * @return true if GMS is available and functional
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        if (sIsAvailable == null) {
            if (!isGmsPackageEnabled(context)) {
                Log.w(TAG, "GMS package not installed or disabled");
                sIsAvailable = false;
                return false;
            }
            try {
                int result = checkViaReflection(context.getApplicationContext());
                sIsAvailable = (result == 0); // ConnectionResult.SUCCESS == 0
            } catch (Throwable t) {
                Log.w(TAG, "Google Play Services not available on this device", t);
                sIsAvailable = false;
            }
        }
        return sIsAvailable;
    }

    /**
     * Checks if the GMS package is installed and enabled using only PackageManager.
     * This makes no calls into GMS classes and is safe on all devices.
     */
    private static boolean isGmsPackageEnabled(Context context) {
        try {
            return context.getPackageManager()
                    .getApplicationInfo(GMS_PACKAGE, 0)
                    .enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Uses reflection to call GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable()
     * without direct class imports that would fail class verification on GMS-free devices.
     */
    private static int checkViaReflection(Context context) throws Exception {
        Class<?> googleApiAvailabilityClass = Class.forName(
                "com.google.android.gms.common.GoogleApiAvailability");
        Object instance = googleApiAvailabilityClass.getMethod("getInstance").invoke(null);
        return (int) googleApiAvailabilityClass
                .getMethod("isGooglePlayServicesAvailable", Context.class)
                .invoke(instance, context);
    }

    /**
     * Reset the cached availability status (useful for testing).
     */
    public static void resetCache() {
        sIsAvailable = null;
    }
}
