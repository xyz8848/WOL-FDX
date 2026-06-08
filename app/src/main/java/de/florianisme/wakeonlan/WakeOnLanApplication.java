package de.florianisme.wakeonlan;

import android.app.Application;

import de.florianisme.wakeonlan.util.AppLogger;

public class WakeOnLanApplication extends Application {

    private static final String TAG = "WakeOnLanApp";

    private Thread.UncaughtExceptionHandler originalExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        AppLogger.init(this);
        AppLogger.i(TAG, "Application onCreate started");

        originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            AppLogger.e(TAG, "Unhandled exception in thread: " + thread.getName(), throwable);
            AppLogger.flush();

            if (originalExceptionHandler != null) {
                originalExceptionHandler.uncaughtException(thread, throwable);
            }
        });

        AppLogger.i(TAG, "Application initialized successfully");
    }
}
