package com.hiepkhach9x.appcamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.util.ArrayList;

/**
 * Created by hungh on 1/4/2017.
 */

public class LoginFragment extends BaseFragment implements View.OnClickListener {
    private static final java.lang.String ARGS_HAS_LOGIN = "args.has.login";
    private final String TAG_LOGIN_LISTENER = "frgLogin_login_listener";
    Client mClient;
    private String[] serverNames;
    private String[] listServerAddress;

    private LinearLayout layoutLogin;
    private EditText edUserName;
    private EditText edPassword;
    private Spinner spServerCollections;
    private LinearLayout layoutControl;
    private Button btnViewAll,btnViewFavorites;

    private String userName, password, serverAddress;
    private boolean hasLogin;

    private ArrayList<Device> devices;
    private MessageParser messageParser = new MessageParser();

    private IMessageListener iLoginMessageListener = new IMessageListener() {
        @Override
        public String getLsTag() {
            return TAG_LOGIN_LISTENER;
        }

        @Override
        public void handleMessage(MessageClient messageClient) {
            Log.d("HungHN",messageClient.getDataToString());
            if (messageClient.isLoginType()) {
                String data = messageClient.getDataToString();
                if (data.contains("ketthuckhoitaohethong")) {
                    Log.d("HungHN", "finish receive data login");
                    hasLogin = true;
                    if (mClient != null)
                        mClient.sendCheckOnlineMessage(messageParser.genMessageCheckOnline(getListCameraIdFromDevice()));
                } else if (data.contains("cameralistbegin")) {
                    devices = messageParser.parseDevice(messageClient.getDataToString());
                } else if (data.contains("khoitaohethong")) {
                    Log.d("HungHN", "start receive data login");
                }
            }

            if (messageClient.isCheckOnline()) {
                Log.d("HungHN","Check online: " + messageClient.getDataToString());
                ArrayList<String> listOnline = messageParser.parseIdOnline(messageClient.getDataToString());
                updateDeviceOnline(listOnline);
                dismissDialog();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        viewLayout();
                    }
                });
                HomeFragment homeFragment = HomeFragment.newInstance(devices);
                if (mNavigateManager != null)
                    mNavigateManager.swapPage(homeFragment, MainActivity.TAG_HOME);
            }
        }
    };

    private void viewLayout() {
        if(hasLogin) {
            layoutLogin.setVisibility(View.INVISIBLE);
            layoutControl.setVisibility(View.VISIBLE);
        } else {
            layoutControl.setVisibility(View.INVISIBLE);
            layoutLogin.setVisibility(View.VISIBLE);
        }
    }

    private void updateDeviceOnline(ArrayList<String> listOnline) {
        if ((listOnline != null && !listOnline.isEmpty())
                && (devices != null & !devices.isEmpty())) {
            for (Device device : devices) {
                ArrayList<Device.Camera> cameras = device.getCameras();
                if (cameras == null || cameras.isEmpty())
                    continue;
                for (Device.Camera camera : cameras) {
                    for (String cameraId : listOnline) {
                        if (camera.getCameraId().equals(cameraId)) {
                            camera.setOnline(true);
                        }

                    }
                }
            }
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState !=null) {
            hasLogin = savedInstanceState.getBoolean(ARGS_HAS_LOGIN);
        }
        serverNames = getResources().getStringArray(R.array.server_collections);
        listServerAddress = getResources().getStringArray(R.array.server_address);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARGS_HAS_LOGIN,hasLogin);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutLogin = (LinearLayout) view.findViewById(R.id.layout_login);
        edUserName = (EditText) view.findViewById(R.id.userName);
        edPassword = (EditText) view.findViewById(R.id.password);
        spServerCollections = (Spinner) view.findViewById(R.id.serverCollections);

        layoutControl = (LinearLayout) view.findViewById(R.id.layout_control);
        btnViewAll = (Button) view.findViewById(R.id.view_all);
        btnViewFavorites = (Button) view.findViewById(R.id.view_favorites);
        btnViewAll.setOnClickListener(this);
        btnViewFavorites.setOnClickListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, serverNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spServerCollections.setAdapter(adapter);
        spServerCollections.setSelection(0);

        view.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = edUserName.getText().toString();
                password = edPassword.getText().toString();
                serverAddress = listServerAddress[spServerCollections.getSelectedItemPosition()];
                UserPref userPref = UserPref.getInstance();
                userPref.saveServerAddress(serverAddress);
                userPref.saveUserName(userName);
                userPref.savePassword(password);
                showDialog();
                login();
            }
        });

        edUserName.setText("demo");
        edPassword.setText("123456");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewLayout();
    }

    private ArrayList<String> getListCameraIdFromDevice() {
        ArrayList<String> strings = new ArrayList<>();
        for (Device device : devices) {
            ArrayList<Device.Camera> cameras = device.getCameras();
            if (cameras != null) {
                for (Device.Camera camera : cameras) {
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
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }
    }

    private void login() {
        final UserPref userPref = UserPref.getInstance();
        userPref.saveServerAddress(serverAddress);
        userPref.saveUserName(userName);
        userPref.savePassword(password);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mClient != null) {
                        mClient.dispose();
                        mClient = null;
                    }
                    mClient = new Client(serverAddress);
                    mClient.addIMessageListener(iLoginMessageListener);
                    new Thread().sleep(100);

                    mClient.sendLoginMessage(messageParser.genMessageLogin(userName,password));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_all:
                break;
            case R.id.view_favorites:
                break;
        }
    }
}
