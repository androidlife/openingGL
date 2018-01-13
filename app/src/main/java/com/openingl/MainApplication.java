package com.openingl;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

/**
 * Created by laaptu on 12/3/17.
 */

public class MainApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        context = this;
    }
}
