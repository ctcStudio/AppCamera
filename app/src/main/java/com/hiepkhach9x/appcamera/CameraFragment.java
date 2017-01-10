package com.hiepkhach9x.appcamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;

import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.customview.CameraView;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;
import com.hiepkhach9x.appcamera.preference.UserPref;

/**
 * Created by hungh on 1/6/2017.
 */

public class CameraFragment extends BaseFragment {

    private static final String ARGS_CAMERA = "args.camera";
    private final String TAG_CAMERA_LISTENER = "fragCameraTag_camera_listener";
    private Device.Camera camera;

    public static CameraFragment newInstance(Device.Camera camera) {

        Bundle args = new Bundle();
        args.putParcelable(ARGS_CAMERA, camera);
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private Client mClient;
    private MessageParser mMessageParser;
    private CameraView mImageCamera;

    private IMessageListener iRealTimeMessage = new IMessageListener() {
        @Override
        public String getLsTag() {
            return TAG_CAMERA_LISTENER;
        }

        @Override
        public void handleMessage(MessageClient messageClient) {
            if(messageClient.isRealTime()) {
                final RealTime realTime = mMessageParser.parseRealTimeMessage(messageClient);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (realTime.getPictureData() != null) {
                            mImageCamera.setImageBitmap(realTime.getPictureData());
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            camera = savedInstanceState.getParcelable(ARGS_CAMERA);
        } else if (getArguments() != null) {
            camera = getArguments().getParcelable(ARGS_CAMERA);
        }
        mMessageParser = new MessageParser();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARGS_CAMERA, camera);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageCamera = (CameraView) view.findViewById(R.id.img_camera);
        mImageCamera.setCameraId(camera.getCameraId());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if (camera != null) {
//            initClient();
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if(mClient!=null) {
//            mClient.dispose();
//            mClient = null;
//        }
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
                    mClient.addIMessageListener(iRealTimeMessage);
                    mClient.sendLoginReadTimeMessage(mMessageParser.genLoginRealTime(userName, password));
                    new Thread().sleep(1000);
                    mClient.sendGetReadTimeIdMessage(mMessageParser.genMessageRealTime(camera.getCameraId()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_camera;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {

    }
}
