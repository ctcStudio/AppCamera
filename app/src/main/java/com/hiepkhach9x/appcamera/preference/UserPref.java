package com.hiepkhach9x.appcamera.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.connection.MessageParser;

import java.util.ArrayList;

/**
 * Created by hungh on 1/6/2017.
 */

public class UserPref {
    private static final String PREFS_NAME = "AppPrefsFile";

    private final String KEY_SERVER_ADDRESS = "key.server.address";
    private final String KEY_USER_NAME = "key.server.user.name";
    private final String KEY_PASSWORD = "key.password";
    private final String KEY_CAMERA_FAVORITE = "key.camera.favorite";


    public static UserPref userPref;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public UserPref(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public static UserPref getInstance() {
        if (userPref == null) {
            userPref = new UserPref(MyApplication.get());
        }
        return userPref;
    }

    public boolean saveServerAddress(String address) {
        return mEditor.putString(KEY_SERVER_ADDRESS, address).commit();
    }

    public String getServerAddress() {
        return mSharedPreferences.getString(KEY_SERVER_ADDRESS, "");
    }

    public boolean saveUserName(String address) {
        return mEditor.putString(KEY_USER_NAME, address).commit();
    }

    public String getUserName() {
        return mSharedPreferences.getString(KEY_USER_NAME, "demo");
    }

    public boolean savePassword(String address) {
        return mEditor.putString(KEY_PASSWORD, address).commit();
    }

    public String getPassword() {
        return mSharedPreferences.getString(KEY_PASSWORD, "123456");
    }

    public boolean saveListCameraFavorite(ArrayList<String> cameraList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cameraList.size(); i++) {
            builder.append(cameraList.get(i));
            if (i != cameraList.size() - 1)
                builder.append(MessageParser.SPERATER1);
        }
        return mEditor.putString(KEY_CAMERA_FAVORITE, builder.toString()).commit();
    }

    public boolean saveCameraFavorite(String cameraId) {
        if (!hasCameraFavorite(cameraId)) {
            String cameras = mSharedPreferences.getString(KEY_CAMERA_FAVORITE, "");
            StringBuilder builder = new StringBuilder(cameras);
            if (!TextUtils.isEmpty(cameras)) {
                builder.append(MessageParser.SPERATER1);
                builder.append(cameraId);
            } else {
                builder.append(cameraId);
            }
            return mEditor.putString(KEY_CAMERA_FAVORITE, builder.toString()).commit();
        }
        return false;
    }

    public ArrayList<String> getListCameraFavorite() {
        ArrayList<String> result = new ArrayList<>();
        String[] cameras = mSharedPreferences.getString(KEY_CAMERA_FAVORITE, "").split(MessageParser.SPERATER1);
        if (cameras != null && cameras.length > 0) {
            for (String cameraId : cameras) {
                if (!result.contains(cameraId))
                    result.add(cameraId);
            }
        }
        return result;
    }

    public boolean hasCameraFavorite(String cameraId) {
        ArrayList<String> cameras = getListCameraFavorite();
        return cameras.contains(cameraId);
    }

    public void removeCameraFavorite(String cameraId) {
        ArrayList<String> cameras = getListCameraFavorite();
        cameras.remove(cameraId);
        saveListCameraFavorite(cameras);
    }
}
