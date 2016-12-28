package com.hiepkhach9x.appcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.listener.IRealTimeListener;
import com.hiepkhach9x.appcamera.entities.RealTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Client mClient;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);
        initClient();
    }

    private IRealTimeListener realTimeListener = new IRealTimeListener() {
        @Override
        public void handleMessage(Object message) {
            if(message instanceof RealTime) {
                final RealTime realTime = (RealTime) message;
                image.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] decodedString = realTime.getPictureData().getBytes();
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            image.setImageBitmap(decodedByte);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private void initClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mClient = new Client();
                    mClient.addReadTimeListener(realTimeListener);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }
    }

    public void send(View view) {
        switch (view.getId()) {
            case R.id.login:
            if (mClient != null) {
                mClient.sendLoginMessage("demo", "123456");
            }
                break;
            case R.id.check_online:
                if (mClient != null) {
                    mClient.sendCheckOnlineMessage(null);
                }
                break;
            case R.id.login_real_time:
                if (mClient != null) {
                    mClient.sendLoginReadTimeMessage("","");
                }
                break;
            case R.id.real_time:
                if (mClient != null) {
                    mClient.sendGetReadTimeIdMessage(null);
                }
                break;
        }

    }
}
