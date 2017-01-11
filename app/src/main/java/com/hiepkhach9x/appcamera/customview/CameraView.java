package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

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
    private final int ARGS_WHAT_REAL_TIME = 123;

    private Client mClient;
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
                            setImageBitmap(realTime.getPictureData());
                        }
                    }
                    return true;
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        parser = new MessageParser();
        initClient();
    }

    public void initClient() {
        new RealTimeThread().start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }
    }

    public boolean sendRealTimeMessage() {
        if (mClient != null && isConnectSuccess && !TextUtils.isEmpty(cameraId)) {
            String msg = parser.genMessageRealTime(cameraId);
            return mClient.sendGetReadTimeIdMessage(msg);
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
            if (!TextUtils.isEmpty(messageClient.getDataToString())) {
                isConnectSuccess = false;
            }
        } else if (messageClient.isRealTime()) {
            android.os.Message realTimeMsg = new android.os.Message();
            realTimeMsg.what = ARGS_WHAT_REAL_TIME;
            realTimeMsg.obj = parser.parseRealTimeMessage(messageClient);
            mHandler.sendMessage(realTimeMsg);
        }
    }


    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    class RealTimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (mClient != null) {
                mClient.dispose();
                mClient = null;
            }
            UserPref userPref = UserPref.getInstance();
            mClient = new Client(userPref.getServerAddress());
            mClient.addIMessageListener(CameraView.this);
            try {
                sleep(200);
                String msg = parser.genLoginRealTime(userPref.getUserName(), userPref.getPassword());
                mClient.sendLoginReadTimeMessage(msg);
                isConnectSuccess = true;
                sleep(2000);
                sendRealTimeMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
