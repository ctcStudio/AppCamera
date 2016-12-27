package com.hiepkhach9x.appcamera.connection;

import android.util.Log;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.connection.listener.ICheckOnlineListener;
import com.hiepkhach9x.appcamera.connection.listener.IClient;
import com.hiepkhach9x.appcamera.connection.listener.IGetDataListener;
import com.hiepkhach9x.appcamera.connection.listener.ILoginListener;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.connection.listener.IRealTimeListener;
import com.hiepkhach9x.appcamera.entities.Device;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by hungh on 12/27/2016.
 */

enum MessageType {
    LOGIN,
    ONLINE,
    LOGIN_REALTIME,
    REALTIME,
    GETDATA
}

public class Client implements IClient {
    private Socket mSocket;
    private InputStreamReader streamReader;
    private OutputStreamWriter streamWriter;
    private StringBuilder stringBuilder;
    private ReadSocketThread socketThread;
    private List<IMessageListener> listenerList;
    private MessageType mCurrentType;

    public Client() throws IOException {
        InetAddress serverAddress = InetAddress.getByName(Config.SERVER_ADDRESS);
        mSocket = new Socket(serverAddress, Config.SERVER_PORT);
        mSocket.setSoTimeout(Config.SOCKET_TIMOUT);

        streamReader = new InputStreamReader(mSocket.getInputStream(), "UTF-8");
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
        return false;
    }

    @Override
    public boolean sendGetReadTimeIdMessage(String id) {
        mCurrentType = MessageType.REALTIME;
        return false;
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
    public void addILoginListener(ILoginListener loginListener) {
        if (loginListener != null) {
            listenerList.add(loginListener);
        }
    }

    @Override
    public void addCheckOnlineListener(ICheckOnlineListener checkOnlineListener) {
        if (checkOnlineListener != null) {
            listenerList.add(checkOnlineListener);
        }
    }

    @Override
    public void addReadTimeListener(IRealTimeListener realTimeListener) {
        if (realTimeListener != null) {
            listenerList.add(realTimeListener);
        }
    }

    @Override
    public void addGetDataListener(IGetDataListener getDataListener) {
        if (getDataListener != null) {
            listenerList.add(getDataListener);
        }
    }


    private class ReadSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            MessageParser messageParser = new MessageParser();
            while (true) {
                if (mSocket == null || mSocket.isClosed()) {
                    dispose();
                    return;
                }
                String message = null;
                try {
                    char[] buf = new char[50];
                    int read = streamReader.read(buf);
                    if (read != -1) {
                        String str = new String(buf).trim();
                        stringBuilder.append(str);
                    } else {
                        this.sleep(100);
                    }
                } catch (IOException ex) {
                    message = stringBuilder.toString();
                    Log.d("HungHN", message);
                    stringBuilder = new StringBuilder();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                switch (mCurrentType) {
                    case LOGIN:
                        ArrayList<Device> devices = messageParser.parseDevice(message);
                        if (devices != null) {
                            for (Device device : devices) {
                                Log.d("HungHN", device.toString());
                            }
                        }
                        break;
                    case ONLINE:
                        ArrayList<String> listIdOnline = messageParser.parseIdOnline(message);
                        if (listIdOnline != null) {
                            Log.d("HungHN", "Number online: " + listIdOnline.size());
                        }
                        break;
                    case LOGIN_REALTIME:
                        break;
                    case REALTIME:
                        break;
                    case GETDATA:
                        break;
                }
            }
        }
    }

    private void deliverMsg(Object object) {
    }
}
