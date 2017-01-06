package com.hiepkhach9x.appcamera.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.hiepkhach9x.appcamera.MyApplication;

/**
 * Created by hungh on 1/6/2017.
 */

public class UserPref {
    private static final String PREFS_NAME = "AppPrefsFile";

    private final String KEY_SERVER_ADDRESS = "key.server.address";
    private final String KEY_USER_NAME = "key.server.user.name";
    private final String KEY_PASSWORD = "key.password";


    public static UserPref userPref;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public UserPref(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public static UserPref getInstance() {
        if(userPref == null) {
            userPref = new UserPref(MyApplication.get());
        }
        return userPref;
    }

    public boolean saveServerAddress(String address) {
        return mEditor.putString(KEY_SERVER_ADDRESS,address).commit();
    }

    public String getServerAddress(){
        return mSharedPreferences.getString(KEY_SERVER_ADDRESS,"");
    }

    public boolean saveUserName(String address) {
        return mEditor.putString(KEY_USER_NAME,address).commit();
    }

    public String getUserName(){
        return mSharedPreferences.getString(KEY_USER_NAME,"");
    }

    public boolean savePassword(String address) {
        return mEditor.putString(KEY_PASSWORD,address).commit();
    }

    public String getPassword(){
        return mSharedPreferences.getString(KEY_PASSWORD,"");
    }
}
