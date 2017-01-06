package com.hiepkhach9x.appcamera;

import android.app.Application;
import android.content.Context;

/**
 * Created by hungh on 1/6/2017.
 */

public class MyApplication extends Application {

    private static MyApplication mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static Context get() {
        return mApp;
    }
}
