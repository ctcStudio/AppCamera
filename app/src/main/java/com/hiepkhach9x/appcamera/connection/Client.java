package com.hiepkhach9x.appcamera.connection;

import android.text.TextUtils;
import android.util.Log;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.connection.listener.IClient;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hungh on 12/27/2016.
 */
public class Client implements IClient {
    private final String TAG = "Client";
    private Socket mSocket;
    private InputStream stream;
    private OutputStreamWriter streamWriter;
    private StringBuilder stringBuilder;
    private ReadSocketThread socketThread;
    private List<IMessageListener> listenerList;
    private MessageType mCurrentType;

    public Client(String sever) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(sever);
        mSocket = new Socket(serverAddress, Config.SERVER_PORT);
        mSocket.setSoTimeout(Config.SOCKET_TIMOUT);

        stream = mSocket.getInputStream();
        streamWriter = new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8");
        stringBuilder = new StringBuilder();
        socketThread = new ReadSocketThread();
        socketThread.start();
        listenerList = new ArrayList<>();
        mCurrentType = MessageType.LOGIN;
    }

    public boolean sendMessage(String message) {
        if (mSocket != null) {
            try {
                streamWriter.write(message);
                streamWriter.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean sendLoginMessage(String userName, String pass) {
        mCurrentType = MessageType.LOGIN;
        String msg = "@haicuong@:" + userName + ":" + pass;
        return sendMessage(msg);
    }

    @Override
    public boolean sendCheckOnlineMessage(ArrayList<String> listId) {
        mCurrentType = MessageType.ONLINE;
        String msg = "@message@checkonline@message@////1600000000000020////1500000000010001////1600000000000025////1600000000000026////";
        return sendMessage(msg);
    }

    @Override
    public boolean sendLoginReadTimeMessage(String userName, String pass) {
        mCurrentType = MessageType.LOGIN_REALTIME;
        String msg = "@haicuongplayer@:demo:123456";
        return sendMessage(msg);
    }

    @Override
    public boolean sendGetReadTimeIdMessage(String id) {
        mCurrentType = MessageType.REALTIME;
        String msg = "@message@yeucaulive@message@////1600000000000025////";
        return sendMessage(msg);
    }

    @Override
    public boolean sendGetDataMessage(String userName, String pass, ArrayList<String> listId) {
        mCurrentType = MessageType.GETDATA;
        return false;
    }

    public void dispose() {
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            if (socketThread != null) {
                socketThread.interrupt();
                socketThread = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addIMessageListener(IMessageListener messageListener) {
        if (messageListener != null) {
            listenerList.add(messageListener);
        }
    }

    private class ReadSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                if (mSocket == null || mSocket.isClosed()) {
                    dispose();
                    return;
                }
                String msg = null;
                try {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length = stream.read(buffer);
                    if (length != -1) {
                        result.write(buffer, 0, length);
                        String str = result.toString("UTF-8").trim();
                        stringBuilder.append(str);
                    }
                } catch (IOException ex) {
                    msg = stringBuilder.toString();
                    Log.d(TAG, msg);
                    stringBuilder = new StringBuilder();
                }

                if (TextUtils.isEmpty(msg)) {
                    continue;
                }
                Message message = new Message();
                message.setMessageType(mCurrentType);
                message.setData(msg);
                for (IMessageListener messageListener : listenerList) {
                    Deliver deliver = new Deliver(message, messageListener);
                    deliver.start();
                }
            }
        }

        class Deliver extends Thread {
            Message message;
            IMessageListener listener;

            public Deliver(Message message, IMessageListener listener) {
                this.message = message;
                this.listener = listener;
            }

            public void run() {
                this.listener.handleMessage(message);
            }
        }
    }
}
