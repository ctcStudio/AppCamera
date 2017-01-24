package com.hiepkhach9x.appcamera.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.util.ArrayList;

/**
 * Created by hungh on 1/4/2017.
 */

public class LoginFragment extends BaseFragment {
    private static final java.lang.String ARGS_HAS_LOGIN = "args.has.login";

    private EditText edUserName;
    private EditText edPassword;
    private EditText edServer;
    private CheckBox cbSavePass;

    private String userName, password, serverAddress;
    private boolean hasLogin;

    private ArrayList<Device> mDevices;
    private ArrayList<String> listCamOnline;
    private MessageParser messageParser = new MessageParser();

    private void updateDeviceOnline(ArrayList<String> listOnline) {
        if ((listOnline != null && !listOnline.isEmpty())
                && (mDevices != null & !mDevices.isEmpty())) {
            for (Device device : mDevices) {
                ArrayList<Camera> cameras = device.getCameras();
                if (cameras == null || cameras.isEmpty())
                    continue;
                for (Camera camera : cameras) {
                    for (String cameraId : listOnline) {
                        if (camera.getCameraId().equals(cameraId)) {
                            camera.setOnline(true);
                        }

                    }
                }
            }
        }
    }

    private ArrayList<Camera> getListCamera() {
        ArrayList<Camera> cameras = new ArrayList<>();
        if (mDevices != null) {
            for (Device device : mDevices) {
                ArrayList<Camera> cameraArrayList = device.getCameras();
                if (cameraArrayList == null || cameraArrayList.isEmpty())
                    continue;
                cameras.addAll(cameraArrayList);
            }
            return cameras;
        }
        return cameras;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hasLogin = savedInstanceState.getBoolean(ARGS_HAS_LOGIN);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARGS_HAS_LOGIN, hasLogin);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edUserName = (EditText) view.findViewById(R.id.userName);
        edPassword = (EditText) view.findViewById(R.id.password);
        edServer = (EditText) view.findViewById(R.id.server);
        cbSavePass = (CheckBox) view.findViewById(R.id.save_password);


        view.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = edUserName.getText().toString();
                password = edPassword.getText().toString();
                serverAddress = edServer.getText().toString();
                login();
            }
        });

        UserPref userPref = UserPref.getInstance();
        edUserName.setText(userPref.getUserName());
        edPassword.setText(userPref.getPassword());
        edServer.setText(userPref.getServerAddress());
        cbSavePass.setChecked(!TextUtils.isEmpty(userPref.getUserName()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private ArrayList<String> getListCameraIdFromDevice() {
        ArrayList<String> strings = new ArrayList<>();
        for (Device device : mDevices) {
            ArrayList<Camera> cameras = device.getCameras();
            if (cameras != null) {
                for (Camera camera : cameras) {
                    if (!TextUtils.isEmpty(camera.getCameraId())) {
                        strings.add(camera.getCameraId());
                    }
                }
            }
        }
        return strings;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void login() {
        if (mNavigateManager != null) {
            if (!mNavigateManager.checkNetworkConnected()) {
                return;
            }
        }
        showDialog();
        Thread loginThread = new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    if (mLoginClient != null) {
                        mLoginClient.initClient();
                    }
                    sleep(200);
                    mLoginClient.sendLogin(userName, password);
                    UserPref userPref = UserPref.getInstance();
                    if (cbSavePass.isChecked()) {
                        userPref.saveUserName(userName);
                        userPref.savePassword(password);
                    }
                    MyApplication.get().setUserName(userName);
                    MyApplication.get().setPassword(password);
                    userPref.saveServerAddress(serverAddress);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        loginThread.start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {
        Log.d("HungHN", messageClient.getDataToString());
        if (messageClient.isLoginType()) {
            String data = messageClient.getDataToString();
            if (data.contains("ketthuckhoitaohethong")) {
                hasLogin = true;
                if (mLoginClient != null && mLoginClient.isClientAlive())
                    mLoginClient.sendCheckOnline(getListCameraIdFromDevice());

            } else if (data.contains("cameralistbegin")) {
                mDevices = messageParser.parseDevice(messageClient.getDataToString());
            } else if (data.contains("khoitaohethong")) {
            }
        }

        if (messageClient.isCheckOnline()) {
            if (TextUtils.isEmpty(messageClient.getDataToString())) {
                return;
            }
            listCamOnline = messageParser.parseIdOnline(messageClient.getDataToString());
            updateDeviceOnline(listCamOnline);
            dismissDialog();

            HomeFragment homeFragment = HomeFragment.newInstance(mDevices);
            if (mNavigateManager != null)
                mNavigateManager.swapPage(homeFragment, MainActivity.TAG_HOME);
        }
    }
}
