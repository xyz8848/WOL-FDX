package de.florianisme.wakeonlan.wear;

import android.content.Context;
import android.util.Log;

/**
 * Helper class to check Google Play Services availability for Wear OS features.
 * Uses reflection to avoid NoClassDefFoundError on devices without GMS.
 */
public class GooglePlayServicesHelper {

    private static final String TAG = "GmsAvailability";

    private static Boolean sIsAvailable = null;

    private GooglePlayServicesHelper() {
        // Utility class
    }

    /**
     * Check if Google Play Services (GMS) is available on this device.
     * Caches the result after the first call.
     * Uses reflection to safely check without causing class loading failures on GMS-free devices.
     *
     * @param context Application or Activity context
     * @return true if GMS is available and functional
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        if (sIsAvailable == null) {
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
