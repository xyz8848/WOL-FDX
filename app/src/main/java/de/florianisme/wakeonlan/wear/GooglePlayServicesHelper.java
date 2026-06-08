package de.florianisme.wakeonlan.wear;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Helper class to check Google Play Services availability for Wear OS features.
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
     *
     * @param context Application or Activity context
     * @return true if GMS is available and functional
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        if (sIsAvailable == null) {
            try {
                int result = GoogleApiAvailability.getInstance()
                        .isGooglePlayServicesAvailable(context.getApplicationContext());
                sIsAvailable = (result == ConnectionResult.SUCCESS);
            } catch (NoClassDefFoundError e) {
                Log.w(TAG, "Google Play Services not available on this device", e);
                sIsAvailable = false;
            } catch (Exception e) {
                Log.w(TAG, "Failed to check Google Play Services availability", e);
                sIsAvailable = false;
            }
        }
        return sIsAvailable;
    }

    /**
     * Reset the cached availability status (useful for testing).
     */
    public static void resetCache() {
        sIsAvailable = null;
    }
}
