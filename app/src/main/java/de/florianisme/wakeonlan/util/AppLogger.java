package de.florianisme.wakeonlan.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 应用日志工具类，将日志输出到 /storage/emulated/0/Android/data/包名/files/logs/latest.log
 *
 * {@link #init(Context)} 初始化日志文件，创建目录。
 * {@link #flush()} 刷新缓冲区。
 * {@link #getLogFilePath()} 获取当前日志文件路径。
 */
public class AppLogger {

    private static final String TAG = "AppLogger";
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "latest.log";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String LOG_FORMAT = "%s [%s/%s]: %s%n";

    private static volatile File logFile;
    private static volatile FileWriter fileWriter;
    private static volatile boolean initialized = false;

    private AppLogger() {
    }

    /**
     * 在应用启动时初始化日志。
     */
    public static synchronized void init(Context context) {
        if (initialized) {
            return;
        }
        try {
            // 日志路径: /storage/emulated/0/Android/data/<packageName>/logs/latest.log
            File dataDir = context.getExternalFilesDir(null).getParentFile();
            File logDir = new File(dataDir, LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            logFile = new File(logDir, LOG_FILE);
            fileWriter = new FileWriter(logFile, false); // 覆盖上一次运行日志
            initialized = true;

            info(TAG, "AppLogger initialized, log file: " + logFile.getAbsolutePath());

            String appInfo = String.format(Locale.US,
                    "App package=%s, version=%s, versionCode=%s, android=%d",
                    context.getPackageName(),
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName,
                    getVersionCode(context),
                    android.os.Build.VERSION.SDK_INT);
            info(TAG, appInfo);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AppLogger", e);
        }
    }

    /**
     * 刷新缓冲区。
     */
    public static synchronized void flush() {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to flush log file", e);
            }
        }
    }

    /**
     * 获取当前的日志文件路径。
     */
    public static String getLogFilePath() {
        if (logFile != null) {
            return logFile.getAbsolutePath();
        }
        return null;
    }

    public static void v(String tag, String message) {
        log(Log.VERBOSE, tag, message, null);
    }

    public static void d(String tag, String message) {
        log(Log.DEBUG, tag, message, null);
    }

    public static void i(String tag, String message) {
        log(Log.INFO, tag, message, null);
    }

    public static void w(String tag, String message) {
        log(Log.WARN, tag, message, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        log(Log.WARN, tag, message, throwable);
    }

    public static void e(String tag, String message) {
        log(Log.ERROR, tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(Log.ERROR, tag, message, throwable);
    }

    public static void info(String tag, String message) {
        i(tag, message);
    }

    private static void log(int priority, String tag, String message, Throwable throwable) {
        String fullMessage = buildLogMessage(tag, message, throwable);

        // 输出到 logcat
        switch (priority) {
            case Log.VERBOSE:
                Log.v(tag, fullMessage);
                break;
            case Log.DEBUG:
                Log.d(tag, fullMessage);
                break;
            case Log.INFO:
                Log.i(tag, fullMessage);
                break;
            case Log.WARN:
                Log.w(tag, fullMessage);
                break;
            case Log.ERROR:
            default:
                Log.e(tag, fullMessage);
                break;
        }

        // 输出到文件
        writeToFile(fullMessage);
    }

    private static String buildLogMessage(String tag, String message, Throwable throwable) {
        String timestamp = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());
        String priorityStr = getPriorityString(Log.INFO);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, LOG_FORMAT, timestamp, tag, priorityStr, message));

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.flush();
            sb.append(sw.toString());
        }

        return sb.toString().trim();
    }

    private static String getPriorityString(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            default:
                return "I";
        }
    }

    private static synchronized void writeToFile(String message) {
        if (fileWriter != null) {
            try {
                fileWriter.write(message + "\n");
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to log file", e);
            }
        }
    }

    private static String getVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return String.valueOf(info.getLongVersionCode());
            }
            // noinspection deprecation
            return String.valueOf(info.versionCode);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
