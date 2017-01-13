package com.hiepkhach9x.appcamera.connection;

import android.util.Log;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.connection.listener.IClient;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private InputStream inputStream;
    private OutputStream outputStream;
    private ReadSocketThread socketThread;
    private List<IMessageListener> listenerList;
    private MessageType mCurrentType;
    private MessageParser mMessageParser;

    public Client(String sever) {
        try {
            mSocket = new Socket(sever, Config.SERVER_PORT);
            mSocket.setSoTimeout(Config.SOCKET_TIMOUT);

            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
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
    public synchronized boolean sendMessage(String message) {
        Log.d("HungHN","send message: " + message);
        if (mSocket != null) {
            try {
                byte[] data = message.getBytes();
                outputStream.write(data);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public synchronized boolean sendLoginMessage(String msg) {
        mCurrentType = MessageType.LOGIN;
        //String msg = "@haicuong@:" + userName + ":" + pass;
        return sendMessage(msg);
    }

    @Override
    public synchronized boolean sendCheckOnlineMessage(String msg) {
        mCurrentType = MessageType.ONLINE;
        //String msg = "@messageClient@checkonline@messageClient@////1600000000000020////1500000000010001////1600000000000025////1600000000000026////";
        return sendMessage(msg);
    }

    @Override
    public synchronized boolean sendLoginReadTimeMessage(String msg) {
        mCurrentType = MessageType.LOGIN_REALTIME;
        //String msg = "@haicuongplayer@:demo:123456";
        return sendMessage(msg);
    }

    @Override
    public synchronized boolean sendGetReadTimeIdMessage(String msg) {
        mCurrentType = MessageType.REALTIME;
        //String msg = "@messageClient@yeucaulive@messageClient@////1600000000000025////";
        return sendMessage(msg);
    }

    @Override
    public synchronized boolean sendGetDataMessage(String msg) {
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
            if (listener.getLsTag().equalsIgnoreCase(messageListener.getLsTag())) {
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
                    int length = inputStream.read(buffer);
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
                MessageClient messageClient = new MessageClient();
                messageClient.setMessageType(mCurrentType);
                messageClient.setData(dataResult);
                for (IMessageListener messageListener : listenerList) {
                    Deliver deliver = new Deliver(messageClient, messageListener);
                    deliver.start();
                }
            }
        }

        class Deliver extends Thread {
            MessageClient messageClient;
            IMessageListener listener;

            public Deliver(MessageClient messageClient, IMessageListener listener) {
                this.messageClient = messageClient;
                this.listener = listener;
            }

            public void run() {
                this.listener.handleMessage(messageClient);
            }
        }
    }
}
