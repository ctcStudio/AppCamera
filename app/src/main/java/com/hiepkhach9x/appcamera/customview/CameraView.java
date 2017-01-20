package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;
import com.hiepkhach9x.appcamera.preference.UserPref;

/**
 * Created by hungnh on 1/10/17.
 */

public class CameraView extends ImageView implements IMessageListener {
    private static final int ARGS_WHAT_SEND_LOGIN_REAL_TIME = 121;
    private final int ARGS_WHAT_REAL_TIME = 123;
    private static final int ARGS_WHAT_SEND_REAL_TIME = 124;
    private static final int ARGS_WHAT_ERROR_LOGIN = 125;

    private Client mClient;
    private RealTimeThread realTimeThread;
    private MessageParser parser;
    private boolean isConnectSuccess;
    private String cameraId;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message message) {
            switch (message.what) {
                case ARGS_WHAT_REAL_TIME:
                    if (message.obj instanceof RealTime) {
                        RealTime realTime = (RealTime) message.obj;
                        if (realTime != null && realTime.getPictureData() != null) {
                            Drawable drawable = new BitmapDrawable(getResources(), realTime.getPictureData());
                            if (Build.VERSION.SDK_INT > 16) {
                                setBackground(drawable);
                            } else {
                                setBackgroundDrawable(drawable);
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
                            && !TextUtils.isEmpty(cameraId)
                            && isConnectSuccess) {
                        String msg = parser.genMessageRealTime(cameraId);
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

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //parser = new MessageParser();
        //initClient();
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
        return null;
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



    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
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
                mClient.addIMessageListener(CameraView.this);
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_LOGIN_REAL_TIME, 200);
                }
                isConnectSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
