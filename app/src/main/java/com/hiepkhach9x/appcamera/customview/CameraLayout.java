package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hungh on 1/13/2017.
 */

public class CameraLayout extends FrameLayout implements IMessageListener {
    public final static String TAG = "Camera_Layout";
    private final String INFO_FORMAT = "%s %s %s";
    private final String SPEED_FORMAT = "%d km/h";

    private static final int ARGS_WHAT_SEND_LOGIN_REAL_TIME = 121;
    private final int ARGS_WHAT_REAL_TIME = 123;
    private static final int ARGS_WHAT_SEND_REAL_TIME = 124;
    private static final int ARGS_WHAT_ERROR_LOGIN = 125;

    private Client mClient;
    private RealTimeThread realTimeThread;
    private MessageParser parser;
    private boolean isConnectSuccess;
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
                                setCameraInfo();
                            }
                            if (realTime.getGpsData() != null) {
                                setCameraSpeed((int) realTime.getGpsData().getSpeedKm());
                            }
                        }
                    }
                    return true;
                case ARGS_WHAT_SEND_LOGIN_REAL_TIME:
                    if (mClient != null) {
                        UserPref userPref = UserPref.getInstance();
                        String msg = parser.genLoginRealTime(userPref.getUserName(), userPref.getPassword());
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
    private View mapView;

    public CameraLayout(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
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

        mapView = findViewById(R.id.map_view);
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
    }

    private void setCameraSpeed(int speed) {
        String speedStr = String.format(SPEED_FORMAT, speed);
        mTxtCameraSpeed.setText(speedStr);
    }

    private void setCameraInfo() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String info = String.format(INFO_FORMAT, df.format(date), mCamera.getCameraName(), mCamera.getCameraId());
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
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_LOGIN_REAL_TIME, 200);
                }
                isConnectSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface CamViewListener {
        void clickFavorite(String cameraId);
    }
}
