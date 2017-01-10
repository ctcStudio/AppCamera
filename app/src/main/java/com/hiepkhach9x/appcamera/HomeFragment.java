package com.hiepkhach9x.appcamera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.hiepkhach9x.appcamera.adapter.DeviceAdapter;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hungh on 1/4/2017.
 */

public class HomeFragment extends BaseFragment {
    private static final String ARGS_DEVICES = "args.devices";
    private final String TAG_LOGIN_LISTENER = "frgHome_login_listener";

    public static HomeFragment newInstance(ArrayList<Device> devices) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_DEVICES, devices);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<Device> mDevices;
    private Client mClient;
    private MessageParser mMessageParser;
    private IMessageListener iLoginMessageListener = new IMessageListener() {
        @Override
        public String getLsTag() {
            return TAG_LOGIN_LISTENER;
        }

        @Override
        public void handleMessage(MessageClient messageClient) {
            if (messageClient.isCheckOnline()) {
                Log.d("HungHN","online: "  + messageClient.getDataToString());
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
            }
        }
    };

    private void updateDeviceOnline(ArrayList<String> listOnline) {
        if ((listOnline != null && !listOnline.isEmpty())
                && (mDevices != null & !mDevices.isEmpty())) {
            for (Device device : mDevices) {
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

    private ExpandableListView listDevice;
    private DeviceAdapter deviceAdapter;
    private Timer mTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDevices = savedInstanceState.getParcelableArrayList(ARGS_DEVICES);
        } else if (getArguments() != null) {
            mDevices = getArguments().getParcelableArrayList(ARGS_DEVICES);
        }
        if (mDevices == null) {
            mDevices = new ArrayList<>();
        }
        mMessageParser = new MessageParser();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARGS_DEVICES, mDevices);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listDevice = (ExpandableListView) view.findViewById(R.id.list_device);
        deviceAdapter = new DeviceAdapter(getContext(), mDevices);
        listDevice.setAdapter(deviceAdapter);

        listDevice.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Device.Camera camera = mDevices.get(groupPosition).getCameras().get(childPosition);
                CameraFragment cameraFragment = CameraFragment.newInstance(camera);
                if(!camera.isOnline()) {
                    Toast.makeText(getContext(),"Camera is offline!",Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(mNavigateManager !=null) {
                    mNavigateManager.addPage(cameraFragment,MainActivity.TAG_CAMERA);
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mClient != null) {
                        mClient.sendCheckOnlineMessage(mMessageParser.genMessageCheckOnline(getListCameraIdFromDevice()));
                    }
                }
            }, 5000, 5000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initClient();
    }

    private void initClient() {
        final String serverAddress = UserPref.getInstance().getServerAddress();
        final String userName = UserPref.getInstance().getUserName();
        final String password = UserPref.getInstance().getPassword();
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
                    mClient.sendLoginMessage(mMessageParser.genMessageLogin(userName, password));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private ArrayList<String> getListCameraIdFromDevice() {
        ArrayList<String> strings = new ArrayList<>();
        for (Device device : mDevices) {
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
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home;
    }
}
