package com.hiepkhach9x.appcamera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.hiepkhach9x.appcamera.adapter.CameraAdapter;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hungh on 1/4/2017.
 */

public class HomeFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARGS_CAMERA = "args.devices";

    public static HomeFragment newInstance(ArrayList<Camera> cameras) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_CAMERA, cameras);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<Camera> mCameras;
    private MessageParser mMessageParser;

    private void updateDeviceOnline(ArrayList<String> listOnline) {
        if ((listOnline != null && !listOnline.isEmpty())
                && (mCameras != null & !mCameras.isEmpty())) {
            for (Camera camera : mCameras) {
                for (String cameraId : listOnline) {
                    if (camera.getCameraId().equals(cameraId)) {
                        camera.setOnline(true);
                    }
                }
            }
        }

    }

    private ListView lisCamera;
    private CameraAdapter deviceAdapter;
    private boolean isRealTime = false;
    private Timer mTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCameras = savedInstanceState.getParcelableArrayList(ARGS_CAMERA);
        } else if (getArguments() != null) {
            mCameras = getArguments().getParcelableArrayList(ARGS_CAMERA);
        }
        if (mCameras == null) {
            mCameras = new ArrayList<>();
        }
        mMessageParser = new MessageParser();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARGS_CAMERA, mCameras);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.real_time).setOnClickListener(this);
        view.findViewById(R.id.play_back).setOnClickListener(this);
        lisCamera = (ListView) view.findViewById(R.id.list_camera);
        deviceAdapter = new CameraAdapter(getContext(), mCameras);
        lisCamera.setAdapter(deviceAdapter);
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
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDeviceOnline(listOnline);
                        if (deviceAdapter != null) {
                            deviceAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
            if(isRealTime) {
                isRealTime = false;
                ListCameraFragment listCameraFragment = ListCameraFragment.newInstance(listOnline);
                if(mNavigateManager!=null) {
                    mNavigateManager.addPage(listCameraFragment,MainActivity.TAG_CAMERA);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.real_time:
                ArrayList<Camera> cameras = deviceAdapter.getCameraSelected();
                if (mLoginClient != null && mLoginClient.isClientAlive()
                        && !cameras.isEmpty()) {
                    mLoginClient.sendCheckOnline(getListCameraIdFromDevice(cameras));
                    isRealTime = true;
                }
                break;
            case R.id.play_back:
                break;
        }
    }
}
