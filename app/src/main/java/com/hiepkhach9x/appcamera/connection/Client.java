package com.hiepkhach9x.appcamera.connection;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.connection.listener.IClient;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hungh on 12/27/2016.
 */
public class Client implements IClient {

    public final static String ACTION_CLIENT_RECEIVE_MESSAGE = "com.client.error";
    public static final String EXTRA_ERROR_MESSAGE = "extra.error.message";
    public static final int UNKNOWN_ERROR = 1;
    public static final int TIMEOUT_ERROR = 2;
    public static final int IO_ERROR = 3;
    public static final int UNKNOWN_HOST_ERROR = 4;


    private final String TAG = "Client";
    private Socket mSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ReadSocketThread socketThread;
    private List<IMessageListener> listenerList;
    private MessageType mCurrentType;

    public Client(String sever, int port) {
        try {
            mSocket = new Socket(sever, port);
            mSocket.setSoTimeout(Config.SOCKET_TIMOUT);

            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
            socketThread = new ReadSocketThread();
            socketThread.start();
        } catch (UnknownHostException e0) {
            sendBroadcastErrorMessage(UNKNOWN_HOST_ERROR);
        } catch (SocketTimeoutException e1) {
            e1.printStackTrace();
            sendBroadcastErrorMessage(TIMEOUT_ERROR);
        } catch (IOException e2) {
            e2.printStackTrace();
            sendBroadcastErrorMessage(IO_ERROR);
        }

        listenerList = new ArrayList<>();
        mCurrentType = MessageType.LOGIN;
    }

    @Override
    public synchronized boolean sendMessage(String message) {
        Log.d("HungHN", "send message: " + message);
        if (mSocket != null) {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(message);
                writer.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                sendBroadcastErrorMessage(IO_ERROR);
            }
        }
        return false;
    }

    @Override
    public synchronized boolean sendMessage(byte[] bytes) {
        Log.d("HungHN", "send message: " + new String(bytes).trim());
        if (mSocket != null) {
            try {
                outputStream.write(bytes);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                sendBroadcastErrorMessage(IO_ERROR);
            }
        }
        return false;
    }

    @Override
    public synchronized void sendLoginMessage(String msg) {
        mCurrentType = MessageType.LOGIN;
        //String msg = "@haicuong@:" + userName + ":" + pass;
        sendMessage(msg);
    }

    @Override
    public synchronized void sendCheckOnlineMessage(String msg) {
        mCurrentType = MessageType.ONLINE;
        //String msg = "@messageClient@checkonline@messageClient@////1600000000000020////1500000000010001////1600000000000025////1600000000000026////";
        sendMessage(msg);
    }

    @Override
    public synchronized void sendLoginReadTimeMessage(String msg) {
        mCurrentType = MessageType.LOGIN_REALTIME;
        //String msg = "@haicuongplayer@:demo:123456";
        sendMessage(msg);
    }

    @Override
    public synchronized void sendGetReadTimeIdMessage(final String msg) {
        mCurrentType = MessageType.REALTIME;
        //String msg = "@messageClient@yeucaulive@messageClient@////1600000000000025////";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendMessage(msg);
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public synchronized void sendLoginGetDataMessage(final String msg) {
        mCurrentType = MessageType.LOGIN_GETDATA;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendMessage(msg);
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public synchronized void sendGetDataMessage(final String msg) {
        mCurrentType = MessageType.GETDATA;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendMessage(msg);
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public synchronized void sendGetVODDataMessage(final byte[] msg) {
        mCurrentType = MessageType.VODDATA;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendMessage(msg);
            }
        };
        new Thread(runnable).start();
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
            sendBroadcastErrorMessage(IO_ERROR);
        }
    }

    @Override
    public void addIMessageListener(IMessageListener messageListener) {
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        }
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

    public void sendBroadcastErrorMessage(int code) {
        Intent intent = new Intent(ACTION_CLIENT_RECEIVE_MESSAGE);
        intent.putExtra(EXTRA_ERROR_MESSAGE, code);
        LocalBroadcastManager.getInstance(MyApplication.get())
                .sendBroadcast(intent);
    }
}
