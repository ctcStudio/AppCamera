package com.hiepkhach9x.appcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.hiepkhach9x.appcamera.connection.Client;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    EditText inputMessage;
    private Client mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputMessage = (EditText) findViewById(R.id.input_message);
        inputMessage.setText("@haicuong@:demo:123456");
        initClient();
    }

    private void initClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mClient = new Client();
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

    boolean hasLogin = false;
    public void send(View view) {
        if (mClient != null) {
            if(!hasLogin) {
              hasLogin =  mClient.sendLoginMessage("demo", "123456");
            } else {
                hasLogin = !mClient.sendCheckOnlineMessage(null);
            }
        }

    }
}
