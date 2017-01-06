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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hungh on 12/27/2016.
 */
public class Client implements IClient {
    private final String TAG = "Client";
    private Socket mSocket;
    private InputStream stream;
    private OutputStreamWriter streamWriter;
    private ReadSocketThread socketThread;
    private List<IMessageListener> listenerList;
    private MessageType mCurrentType;
    private MessageParser mMessageParser;

    public Client(String sever) {
        try {
            mSocket = new Socket(sever, Config.SERVER_PORT);
            mSocket.setSoTimeout(Config.SOCKET_TIMOUT);

            stream = mSocket.getInputStream();
            streamWriter = new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8");
            socketThread = new ReadSocketThread();
            socketThread.start();
            listenerList = new ArrayList<>();
            mCurrentType = MessageType.LOGIN;
            mMessageParser = new MessageParser();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
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
    public boolean sendLoginMessage(String msg) {
        mCurrentType = MessageType.LOGIN;
        //String msg = "@haicuong@:" + userName + ":" + pass;
        return sendMessage(msg);
    }

    @Override
    public boolean sendCheckOnlineMessage(String msg) {
        mCurrentType = MessageType.ONLINE;
        //String msg = "@message@checkonline@message@////1600000000000020////1500000000010001////1600000000000025////1600000000000026////";
        return sendMessage(msg);
    }

    @Override
    public boolean sendLoginReadTimeMessage(String msg) {
        mCurrentType = MessageType.LOGIN_REALTIME;
        //String msg = "@haicuongplayer@:demo:123456";
        return sendMessage(msg);
    }

    @Override
    public boolean sendGetReadTimeIdMessage(String msg) {
        mCurrentType = MessageType.REALTIME;
        //String msg = "@message@yeucaulive@message@////1600000000000025////";
        return sendMessage(msg);
    }

    @Override
    public boolean sendGetDataMessage(String msg) {
        mCurrentType = MessageType.GETDATA;
        return sendMessage(msg);
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
        Log.d(TAG,"Client is closed!");
    }

    @Override
    public void addIMessageListener(IMessageListener messageListener) {
        if (messageListener != null) {
            listenerList.add(messageListener);
        }
    }

    @Override
    public void removeIMessageListener(IMessageListener messageListener) {
        if (listenerList == null || listenerList.isEmpty()) {
            return;
        }
        Iterator<IMessageListener> iterator = listenerList.iterator();
        while (iterator.hasNext()) {
            IMessageListener listener = iterator.next();
            if (listener.getTag().equalsIgnoreCase(messageListener.getTag())) {
                iterator.remove();
            }
        }
    }

    private class ReadSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            while (true) {
                if (mSocket == null || mSocket.isClosed()) {
                    dispose();
                    return;
                }
                byte[] dataResult = null;
                try {
                    byte[] buffer = new byte[1024];
                    int length = stream.read(buffer);
                    if (length != -1) {
                        result.write(buffer, 0, length);
                    }
                } catch (IOException ex) {
                    dataResult = result.toByteArray();
                    result.reset();
                }

                if (dataResult == null || dataResult.length < 1) {
                    continue;
                }
                Message message = new Message();
                message.setMessageType(mCurrentType);
                message.setData(dataResult);
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
