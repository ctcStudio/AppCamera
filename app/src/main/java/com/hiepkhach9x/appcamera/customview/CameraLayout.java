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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.GpsInfo;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hungh on 1/13/2017.
 */

public class CameraLayout extends FrameLayout implements IMessageListener, OnMapReadyCallback {
    public final static String TAG = "Camera_Layout";
    private final String INFO_FORMAT = "%s %s";
    private final String SPEED_FORMAT = "%d km/h";

    private static final int ARGS_WHAT_SEND_LOGIN_REAL_TIME = 121;
    private final int ARGS_WHAT_REAL_TIME = 123;
    private static final int ARGS_WHAT_SEND_REAL_TIME = 124;
    private static final int ARGS_WHAT_ERROR_LOGIN = 125;

    private Client mClient;
    private RealTimeThread realTimeThread;
    private MessageParser parser;
    private boolean isConnectSuccess;
    private ClickCameraInterface cameraListener;
    private UpdateCameraInfo updateCameraInfo;

    private RealTime lastRealTimeData;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message message) {
            switch (message.what) {
                case ARGS_WHAT_REAL_TIME:
                    if (message.obj instanceof RealTime) {
                        RealTime realTime = (RealTime) message.obj;
                        if (realTime != null) {
                            if (realTime.getPictureData() != null) {
                                Drawable drawable = new BitmapDrawable(getResources(), realTime.getPictureData());
                                if (Build.VERSION.SDK_INT > 16) {
                                    mCameraView.setBackground(drawable);
                                } else {
                                    mCameraView.setBackgroundDrawable(drawable);
                                }
                            }
                            if (realTime.getGpsData() != null) {
                                GpsInfo gpsInfo = realTime.getGpsData();
                                setCameraSpeed((int) gpsInfo.getSpeedKm());
                                showGpsLocation(gpsInfo.getLat(), gpsInfo.getLog());
                                if(TextUtils.isEmpty(gpsInfo.getAddress())) {
                                    mTxtCameraAddress.setText(gpsInfo.getAddress());
                                }
                            }
                        }
                        if (updateCameraInfo != null) {
                            updateCameraInfo.onUpdateInfo(realTime);
                        }
                        lastRealTimeData = realTime;
                    }
                    return true;
                case ARGS_WHAT_SEND_LOGIN_REAL_TIME:
                    if (mClient != null) {
                        UserPref userPref = UserPref.getInstance();
                        String msg = parser.genLoginRealTime(MyApplication.get().getUserName(), MyApplication.get().getPassword());
                        mClient.sendLoginReadTimeMessage(msg);
                    }
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_REAL_TIME, 2000);
                    }
                    return true;
                case ARGS_WHAT_SEND_REAL_TIME:
                    if (mClient != null
                            && mCamera != null
                            && !TextUtils.isEmpty(mCamera.getCameraId())
                            && isConnectSuccess) {
                        String msg = parser.genMessageRealTime(mCamera.getCameraId());
                        mClient.sendGetReadTimeIdMessage(msg);
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
    private MapView mapView;
    private GoogleMap gMap;
    private Bundle savedInstanceState = null;

    public CameraLayout(Context context, Camera camera, Bundle bundle) {
        super(context);
        this.mCamera = camera;
        savedInstanceState = bundle;
        initializeViews(context);
    }

    public CameraLayout(Context context) {
        super(context);
        initializeViews(context);
    }

    public CameraLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public CameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setCameraInfo();

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
        MapsInitializer.initialize(getContext());

        mGps = (Button) findViewById(R.id.gps);
        mGps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapView.getVisibility() == VISIBLE) {
                    mapView.setVisibility(GONE);
                } else {
                    mapView.setVisibility(VISIBLE);
                }
            }
        });

        mCameraView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraListener != null) {
                    cameraListener.onClickCamera(mCamera.getCameraId());
                }
            }
        });
    }

    private void setCameraSpeed(int speed) {
        String speedStr = String.format(Locale.getDefault(),SPEED_FORMAT, speed);
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        mTxtCameraSpeed.setText(speedStr + "  " + df.format(date));
    }

    private void setCameraInfo() {
        String info = String.format(INFO_FORMAT, mCamera.getCameraGroup(), mCamera.getCameraName());
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
        if (realTimeThread != null) {
            realTimeThread.interrupt();
            realTimeThread = null;
        }
        realTimeThread = new RealTimeThread();
        realTimeThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }

        if (realTimeThread != null) {
            realTimeThread.interrupt();
            realTimeThread = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public boolean sendRealTimeMessage() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(ARGS_WHAT_SEND_REAL_TIME);
        }
        return false;
    }


    @Override
    public String getLsTag() {
        return TAG;
    }

    @Override
    public void handleMessage(MessageClient messageClient) {
        if (messageClient.isLoginRealTime()) {
            Log.d("Camera: ", "HungHN: " + messageClient.getDataToString());
            if (!TextUtils.isEmpty(messageClient.getDataToString())) {
                isConnectSuccess = false;
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(ARGS_WHAT_ERROR_LOGIN);
                }
            }
        } else if (messageClient.isRealTime()) {
            android.os.Message realTimeMsg = new android.os.Message();
            realTimeMsg.what = ARGS_WHAT_REAL_TIME;
            realTimeMsg.obj = parser.parseRealTimeMessage(messageClient);
            if (mHandler != null) {
                mHandler.sendMessage(realTimeMsg);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("HungHN", "init map for camera : " + mCamera.getCameraId());
        gMap = googleMap;
        //showGpsLocation(21.131, 105.803);
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (cameraListener != null) {
                    cameraListener.onClickMap(mCamera.getCameraId());
                }
            }
        });
    }

    private void showGpsLocation(double lat, double log) {
        gMap.clear();
        Log.d("HungHN", "Gps: " + lat + " ----- log: " + log);
        LatLng cam = new LatLng(lat, log);
        MarkerOptions markerOptions = new MarkerOptions().position(cam)
                .title(mCamera.getCameraName());

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_oto));
        gMap.addMarker(markerOptions);
        gMap.setMinZoomPreference(15.0f);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(cam));
    }

    class RealTimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                if (mClient != null) {
                    mClient.dispose();
                    mClient = null;
                }
                UserPref userPref = UserPref.getInstance();
                mClient = new Client(userPref.getServerAddress(), Config.SERVER_PORT);
                mClient.addIMessageListener(CameraLayout.this);

                String msg = parser.genLoginRealTime(MyApplication.get().getUserName(), MyApplication.get().getPassword());
                mClient.sendLoginReadTimeMessage(msg);

                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_REAL_TIME, 2000);
                }
                isConnectSuccess = true;
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

    public void mapViewPause() {
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

    public boolean hasCamera(String cameraId) {
        if (mCamera == null) {
            return false;
        }
        return mCamera.getCameraId().equals(cameraId);
    }

    public void setCameraListener(ClickCameraInterface cameraListener) {
        this.cameraListener = cameraListener;
    }

    public void setUpdateCameraInfo(UpdateCameraInfo updateCameraInfo) {
        this.updateCameraInfo = updateCameraInfo;
        if (updateCameraInfo != null && lastRealTimeData != null) {
            updateCameraInfo.onUpdateInfo(lastRealTimeData);
        }
    }

    public void removeUpdateCameraInfo() {
        this.updateCameraInfo = null;
    }

    public interface UpdateCameraInfo {
        void onUpdateInfo(RealTime realTime);
    }
}
