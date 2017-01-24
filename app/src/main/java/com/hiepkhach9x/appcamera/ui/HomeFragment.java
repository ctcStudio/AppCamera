package com.hiepkhach9x.appcamera.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.adapter.CameraAdapter;
import com.hiepkhach9x.appcamera.adapter.DeviceAdapter;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hungh on 1/4/2017.
 */

public class HomeFragment extends BaseFragment {
    private static final String ARGS_DEVICE = "args.devices";

    public static HomeFragment newInstance(ArrayList<Device> devices) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_DEVICE, devices);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<Device> mDevices;
    private ArrayList<Camera> mCameras;
    private MessageParser mMessageParser;

    private void updateDeviceOnline(ArrayList<String> listOnline) {
        if ((listOnline != null && !listOnline.isEmpty())
                && (mDevices != null & !mDevices.isEmpty())) {
            for (Device device : mDevices) {
                ArrayList<Camera> cameras = device.getCameras();
                if (cameras == null || cameras.isEmpty())
                    continue;
                for (Camera camera : cameras) {
                    camera.setOnline(listOnline.contains(camera.getCameraId()));
                }
            }
        }
    }

    private ArrayList<Camera> getCameraOnline(ArrayList<String> listOnline) {
        ArrayList<Camera> cameras = new ArrayList<>();
        if ((listOnline != null && !listOnline.isEmpty())
                && (mDevices != null & !mDevices.isEmpty())) {
            for (Device device : mDevices) {
                ArrayList<Camera> cameraList = device.getCameras();
                if (cameraList == null || cameraList.isEmpty())
                    continue;
                for (Camera camera : cameraList) {
                    if (listOnline.contains(camera.getCameraId())) {
                        cameras.add(camera);
                    }
                }
            }
        }
        return cameras;
    }

    private ExpandableListView listDevice;
    private DeviceAdapter deviceAdapter;
    private boolean isRealTime = false;
    private Timer mTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDevices = savedInstanceState.getParcelableArrayList(ARGS_DEVICE);
        } else if (getArguments() != null) {
            mDevices = getArguments().getParcelableArrayList(ARGS_DEVICE);
        }
        if (mDevices == null) {
            mDevices = new ArrayList<>();
        }
        mCameras = getListCameraFromDevice(mDevices);
        mMessageParser = new MessageParser();
    }

    private ArrayList<Camera> getListCameraFromDevice(ArrayList<Device> mDevices) {
        ArrayList<Camera> cameras = new ArrayList<>();
        if ((mDevices != null & !mDevices.isEmpty())) {
            for (Device device : mDevices) {
                ArrayList<Camera> cameraList = device.getCameras();
                if (cameraList == null || cameraList.isEmpty())
                    continue;
                cameras.addAll(cameraList);

            }
        }
        return cameras;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARGS_DEVICE, mDevices);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listDevice = (ExpandableListView) view.findViewById(R.id.list_device);
        deviceAdapter = new DeviceAdapter(getContext(), mDevices);
        listDevice.setAdapter(deviceAdapter);

        for (int i = 0; i < deviceAdapter.getGroupCount(); i++) {
            listDevice.expandGroup(i);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mLoginClient != null && mLoginClient.isClientAlive()) {
                        mLoginClient.sendCheckOnline(getListCameraIdFromDevice(mCameras));
                    }
                }
            }, 10000, 20000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private ArrayList<String> getListCameraIdFromDevice(ArrayList<Camera> cameras) {
        ArrayList<String> strings = new ArrayList<>();
        for (Camera camera : cameras) {
            if (!TextUtils.isEmpty(camera.getCameraId())) {
                strings.add(camera.getCameraId());
            }
        }
        return strings;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {
        if (messageClient.isCheckOnline()) {
            Log.d("HungHN", "online: " + messageClient.getDataToString());
            final ArrayList<String> listOnline = mMessageParser.parseIdOnline(messageClient.getDataToString());
            if (isRealTime) {
                isRealTime = false;
                ArrayList<Camera> cameras = getCameraOnline(listOnline);
                if (cameras != null) {
                    ListCameraFragment listCameraFragment = ListCameraFragment.newInstance(cameras);
                    if (mNavigateManager != null) {
                        mNavigateManager.addPage(listCameraFragment, MainActivity.TAG_CAMERA);
                    }
                } else {
                    Toast.makeText(getContext(), "Camera lựa chọn không online", Toast.LENGTH_SHORT).show();
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDeviceOnline(listOnline);
                            if (deviceAdapter != null) {
                                deviceAdapter.notifyDataSetChanged();
                            }
                            if (mLoginClient != null) {
                                mLoginClient.setListCamera(mCameras);
                                mLoginClient.setListDevice(mDevices);
                            }
                        }
                    });
                }
            }
        }
    }

    public void gotoRealTime() {
        ArrayList<Camera> cameras = deviceAdapter.getCameraSelected();
        if (mLoginClient != null && mLoginClient.isClientAlive()
                && !cameras.isEmpty()) {
            mLoginClient.sendCheckOnline(getListCameraIdFromDevice(cameras));
            isRealTime = true;
        }
    }

    public void gotoPlayBack() {
        PlayBackFragment playBackFragment = PlayBackFragment.newInstance(mCameras);
        if (mNavigateManager != null) {
            mNavigateManager.addPage(playBackFragment, MainActivity.TAG_PLAY_BACK);
        }
    }
}
