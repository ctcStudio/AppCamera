package com.hiepkhach9x.appcamera.connection.listener;


/**
 * Created by hungh on 12/27/2016.
 */

public interface IClient {

    void sendLoginMessage(String message);

    void sendCheckOnlineMessage(String message);

    void sendLoginReadTimeMessage(String message);

    void sendGetReadTimeIdMessage(String message);

    void sendLoginGetDataMessage(String message);

    void sendGetDataMessage(String message);

    void sendGetVODDataMessage(byte[] message);

    boolean sendMessage(String message);

    boolean sendMessage(byte[] bytes);

    void dispose();

    void addIMessageListener(IMessageListener messageListener);

    void removeIMessageListener(IMessageListener messageListener);
}
