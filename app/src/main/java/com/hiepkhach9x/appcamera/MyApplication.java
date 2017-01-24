package com.hiepkhach9x.appcamera;

import android.app.Application;
import android.content.Context;

/**
 * Created by hungh on 1/6/2017.
 */

public class MyApplication extends Application {

    private static MyApplication mApp;
    private String userName;
    private String password;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static MyApplication get() {
        return mApp;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
