package com.hiepkhach9x.appcamera.connection.listener;


/**
 * Created by hungh on 12/27/2016.
 */

public interface IClient {

    boolean sendLoginMessage(String message);

    boolean sendCheckOnlineMessage(String message);

    boolean sendLoginReadTimeMessage(String message);

    boolean sendGetReadTimeIdMessage(String message);

    boolean sendLoginGetDataMessage(String message);

    boolean sendGetDataMessage(String message);

    boolean sendGetVODDataMessage(byte[] message);

    boolean sendMessage(String message);

    boolean sendMessage(byte[] bytes);

    void dispose();

    void addIMessageListener(IMessageListener messageListener);

    void removeIMessageListener(IMessageListener messageListener);
}
