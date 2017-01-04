package com.hiepkhach9x.appcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.MessageType;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.Message;

import java.io.IOException;
import java.util.List;

/**
 * Created by hungh on 1/4/2017.
 */

public class LoginFragment extends BaseFragment {
    private Client mClient;
    private String[] serverNames;
    private String[] listServerAddress;

    private EditText edUserName;
    private EditText edPassword;
    private Spinner spServerCollections;

    private String userName, password, serverAddress;

    private MessageParser messageParser = new MessageParser();

    private IMessageListener iMessageListener = new IMessageListener() {
        @Override
        public void handleMessage(Message message) {
            if (message.isLoginType()) {
                String data = message.getData();
                if (data.contains("ketthuckhoitaohethong")) {
                    Log.d("HungHN", "finish receive data login");
                    dismissDialog();
                } else if (data.contains("cameralistbegin")) {
                    List<Device> devices = messageParser.parseDevice(message.getData());
                    Log.d("HungHN", "data devices receive: " + devices.size());
                } else if (data.contains("khoitaohethong")) {
                    Log.d("HungHN", "start receive data login");
                }

            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverNames = getResources().getStringArray(R.array.server_collections);
        listServerAddress = getResources().getStringArray(R.array.server_address);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edUserName = (EditText) view.findViewById(R.id.userName);
        edPassword = (EditText) view.findViewById(R.id.password);
        spServerCollections = (Spinner) view.findViewById(R.id.serverCollections);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, serverNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spServerCollections.setAdapter(adapter);
        spServerCollections.setSelection(0);

        view.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = edUserName.getText().toString();
                password = edPassword.getText().toString();
                serverAddress = listServerAddress[spServerCollections.getSelectedItemPosition()];
                showDialog();
                login();
            }
        });

        edUserName.setText("demo");
        edPassword.setText("123456");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mClient != null) {
            mClient.dispose();
            mClient = null;
        }
    }

    private void login() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mClient = new Client(serverAddress);
                    mClient.addIMessageListener(iMessageListener);
                    new Thread().sleep(100);
                    mClient.sendLoginMessage(userName, password);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_login;
    }
}
