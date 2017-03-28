package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
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
import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.GpsInfo;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.VOData;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hungh on 1/13/2017.
 */

public class PlayBackLayout extends FrameLayout implements IMessageListener, OnMapReadyCallback {
    public final static String TAG = "Camera_Layout";
    private final String INFO_FORMAT = "%s %s %s";
    private final String SPEED_FORMAT = "%s  %d km/h";

    private final int ARGS_WHAT_PLAY_BACK = 123;
    private static final int ARGS_WHAT_SEND_PLAY_BACK = 124;
    private static final int ARGS_WHAT_ERROR_LOGIN = 125;

    private final short TIME_PLAY = 1000; // from 0 - 3600s độ dài 2 bytes dạng unsigned integer 16
    private final char PLAY_SPEED = '5'; // from x1-x9 độ dài 1 bytes dạng unsigned integer 8

    private Client mClient;
    private PlayBackThread playBackThread;
    private MessageParser parser;
    private ClickCameraInterface cameraListener;
    private UpdateVodInfo updateVodInfo;
    private boolean isConnectSuccess;
    private VOData lastVodData;
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
                        if (voData.getGpsData() != null) {
                            GpsInfo gpsInfo = voData.getGpsData();
                            setCameraSpeed((int) gpsInfo.getSpeedKm(), voData.getFileName());
                            showGpsLocation(gpsInfo.getLat(), gpsInfo.getLog());
                            if (TextUtils.isEmpty(gpsInfo.getAddress())) {
                                mTxtCameraAddress.setText(gpsInfo.getAddress());
                            }
                        }
                        lastVodData = voData;
                        if (updateVodInfo != null) {
                            updateVodInfo.onUpdateInfo(voData);
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
        setCameraInfo();

        mTxtCameraAddress = (TextView) findViewById(R.id.camera_address);
        mTxtCameraAddress.setText("Ha Noi");

        mTxtCameraSpeed = (TextView) findViewById(R.id.camera_speed);
        Date date = Calendar.getInstance().getTime();
        java.text.DateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        setCameraSpeed(0,df.format(date));

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

        mCameraView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraListener != null) {
                    cameraListener.onClickCamera(mCamera.getCameraId());
                }
            }
        });
    }

    private void setCameraSpeed(int speed, String date) {
        String speedStr = String.format(Locale.getDefault(),SPEED_FORMAT, date, speed);
        mTxtCameraSpeed.setText(speedStr);
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
        try {
            LatLng cam = new LatLng(lat, log);
            MarkerOptions markerOptions = new MarkerOptions().position(cam)
                    .title(mCamera.getCameraName());

            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_oto));
            gMap.addMarker(markerOptions);
            gMap.setMinZoomPreference(15.0f);
            gMap.moveCamera(CameraUpdateFactory.newLatLng(cam));
        } catch (Exception ex) {
        }
    }

    public void removeUpdateVodInfo() {
        this.updateVodInfo = null;
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

                String msg = parser.genLoginCallBack(MyApplication.get().getUserName(), MyApplication.get().getPassword());
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

    public boolean hasCamera(String cameraId) {
        if (mCamera == null) {
            return false;
        }

        return mCamera.getCameraId().equals(cameraId);
    }

    public void setCameraListener(ClickCameraInterface cameraListener) {
        this.cameraListener = cameraListener;
    }

    public void setUpdateVodInfo(UpdateVodInfo updateVodInfo) {
        this.updateVodInfo = updateVodInfo;
        if (updateVodInfo != null && lastVodData != null) {
            updateVodInfo.onUpdateInfo(lastVodData);
        }
    }

    public interface UpdateVodInfo {
        void onUpdateInfo(VOData realTime);
    }
}
