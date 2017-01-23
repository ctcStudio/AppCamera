package com.hiepkhach9x.appcamera.ui;

import com.hiepkhach9x.appcamera.entities.Camera;

import java.util.ArrayList;

/**
 * Created by hungh on 1/10/2017.
 */

public interface LoginClient {

    void initClient();

    void sendLogin(String userName,String pass);

    void sendCheckOnline(ArrayList<String> strings);

    void closeClient();

    boolean isClientAlive();

    void setListCamera(ArrayList<Camera> cameras);

    ArrayList<Camera> getListCamera();
}
