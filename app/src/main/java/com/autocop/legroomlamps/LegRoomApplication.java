package com.autocop.legroomlamps;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class LegRoomApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /* access modifiers changed from: protected */
    public void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
    private class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            // Handle the uncaught exception here
            Log.e("mytag", "Unhandled exception caught: " + throwable.getMessage(), throwable);

            // Perform any necessary cleanup or recovery tasks

            // Optionally, restart the application or take appropriate action
            // Note: Restarting the application after an uncaught exception is generally not recommended.
        }
    }
}
