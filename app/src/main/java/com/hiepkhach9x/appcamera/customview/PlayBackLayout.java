package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.GpsInfo;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.VOData;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hungh on 1/13/2017.
 */

public class PlayBackLayout extends FrameLayout implements IMessageListener, OnMapReadyCallback {
    public final static String TAG = "Camera_Layout";
    private final String INFO_FORMAT = "%s %s %s";
    private final String SPEED_FORMAT = "%d km/h";

    private final int ARGS_WHAT_PLAY_BACK = 123;
    private static final int ARGS_WHAT_SEND_PLAY_BACK = 124;
    private static final int ARGS_WHAT_ERROR_LOGIN = 125;

    private final short TIME_PLAY = 1000; // from 0 - 3600s độ dài 2 bytes dạng unsigned integer 16
    private final char PLAY_SPEED = '5'; // from x1-x9 độ dài 1 bytes dạng unsigned integer 8

    private Client mClient;
    private PlayBackThread playBackThread;
    private MessageParser parser;
    private boolean isConnectSuccess;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message message) {
            switch (message.what) {
                case ARGS_WHAT_PLAY_BACK:
                    if (message.obj instanceof VOData) {
                        VOData voData = (VOData) message.obj;
                        if (voData.getPictureData() != null) {
                            Drawable drawable = new BitmapDrawable(getResources(), voData.getPictureData());
                            if (Build.VERSION.SDK_INT > 16) {
                                mCameraView.setBackground(drawable);
                            } else {
                                mCameraView.setBackgroundDrawable(drawable);
                            }
                        }
                        if(TextUtils.isEmpty(voData.getFileName())) {
                            setCameraInfo(voData.getFileName());
                        }
                        if (voData.getGpsData() != null) {
                            GpsInfo gpsInfo = voData.getGpsData();
                            setCameraSpeed((int) gpsInfo.getSpeedKm());
                            showGpsLocation(gpsInfo.getLat(), gpsInfo.getLog());
                            mTxtCameraAddress.setText(gpsInfo.getAddress());
                        }
                    }
                    return true;
                case ARGS_WHAT_SEND_PLAY_BACK:
                    if (mClient != null
                            && mCamera != null
                            && !TextUtils.isEmpty(mCamera.getCameraId())
                            && isConnectSuccess) {
                        byte[] msg = parser.genMessageVODData(mCamera.getCameraId(), fileName, TIME_PLAY, PLAY_SPEED);
                        mClient.sendGetVODDataMessage(msg);
                    }
                    return true;
                case ARGS_WHAT_ERROR_LOGIN:
                    setBackgroundResource(R.drawable.ic_connect_error);
                default:
                    return false;
            }
        }
    });


    private CameraView mCameraView;
    private TextView mTxtCameraInfo, mTxtCameraAddress, mTxtCameraSpeed;
    private Button mGps;
    private Camera mCamera;
    private String fileName;
    private MapView mapView;
    private GoogleMap gMap;
    private Bundle savedInstanceState = null;

    public PlayBackLayout(Context context, Camera camera, String fileName, Bundle bundle) {
        super(context);
        this.mCamera = camera;
        savedInstanceState = bundle;
        this.fileName = fileName;
        initializeViews(context);
    }

    public PlayBackLayout(Context context) {
        super(context);
        initializeViews(context);
    }

    public PlayBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public PlayBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        parser = new MessageParser();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_list_camera, this);

        mCameraView = (CameraView) findViewById(R.id.camera);
        mCameraView.setCameraId(mCamera.getCameraId());

        mTxtCameraInfo = (TextView) findViewById(R.id.camera_info);
        setCameraInfo(fileName);

        mTxtCameraAddress = (TextView) findViewById(R.id.camera_address);
        mTxtCameraAddress.setText("Ha Noi");

        mTxtCameraSpeed = (TextView) findViewById(R.id.camera_speed);
        setCameraSpeed(0);

        mapView = (MapView) findViewById(R.id.map_view);
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mGps = (Button) findViewById(R.id.gps);
        mGps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapView.getVisibility() == VISIBLE) {
                    mGps.setText("Show Gps");
                    mapView.setVisibility(GONE);
                } else {
                    mapView.setVisibility(VISIBLE);
                    mGps.setText("hide Gps");
                }
            }
        });
    }

    private void setCameraSpeed(int speed) {
        String speedStr = String.format(SPEED_FORMAT, speed);
        mTxtCameraSpeed.setText(speedStr);
    }

    private void setCameraInfo(String fileName) {
        String info = String.format(INFO_FORMAT, fileName, mCamera.getCameraName(), mCamera.getCameraId());
        mTxtCameraInfo.setText(info);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mCamera != null)
            mTxtCameraAddress.setText(mCamera.getCameraId());
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
        if (mCameraView != null) {
            mCameraView.initClient();
        }
        if (mCamera != null)
            mTxtCameraAddress.setText(camera.getCameraId());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (parser == null) {
            parser = new MessageParser();
        }
    }

    public void initClient() {
        if (playBackThread != null) {
            playBackThread.interrupt();
            playBackThread = null;
        }
        playBackThread = new PlayBackThread();
        playBackThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }

        if (playBackThread != null) {
            playBackThread.interrupt();
            playBackThread = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }


    @Override
    public String getLsTag() {
        return TAG;
    }

    @Override
    public void handleMessage(MessageClient messageClient) {
        if (messageClient.isLoginGetData()) {
            Log.d("Camera: ", "HungHN: " + messageClient.getDataToString());
            if (!TextUtils.isEmpty(messageClient.getDataToString())
                    && !isConnectSuccess) {
                isConnectSuccess = true;
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_PLAY_BACK, 2000);
                }
            }
        } else if (messageClient.isVOData()) {
            android.os.Message message = new android.os.Message();
            message.what = ARGS_WHAT_PLAY_BACK;
            message.obj = parser.parseVODMessage(messageClient);
            if (mHandler != null) {
                mHandler.sendMessage(message);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("HungHN", "init map for camera : " + mCamera.getCameraId());
        gMap = googleMap;
        //showGpsLocation(21.131, 105.803);
    }

    private void showGpsLocation(double lat, double log) {
        LatLng cam = new LatLng(lat, log);
        MarkerOptions markerOptions = new MarkerOptions().position(cam)
                .title(mCamera.getCameraName());

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_oto));
        gMap.addMarker(markerOptions);
        gMap.setMinZoomPreference(15.0f);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(cam));
    }

    class PlayBackThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                if (mClient != null) {
                    mClient.dispose();
                    mClient = null;
                }
                UserPref userPref = UserPref.getInstance();
                mClient = new Client(userPref.getServerAddress(), Config.SERVER_STORE_PORT);
                mClient.addIMessageListener(PlayBackLayout.this);

                String msg = parser.genLoginCallBack(userPref.getUserName(), userPref.getPassword());
                mClient.sendLoginGetDataMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mapViewOnResume() {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    public void mapViewOnPause() {
        if (mapView != null) {
            mapView.onPause();
        }
    }

    public void mapViewOnDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    public void mapViewOnLowMemory() {
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
