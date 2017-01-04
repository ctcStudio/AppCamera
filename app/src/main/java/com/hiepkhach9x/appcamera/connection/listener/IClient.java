package com.hiepkhach9x.appcamera.connection.listener;

import java.util.ArrayList;

/**
 * Created by hungh on 12/27/2016.
 */

public interface IClient {
    boolean sendLoginMessage(String userName, String pass);

    boolean sendCheckOnlineMessage(ArrayList<String> listId);

    boolean sendLoginReadTimeMessage(String userName, String pass);

    boolean sendGetReadTimeIdMessage(String id);

    boolean sendGetDataMessage(String userName, String pass, ArrayList<String> listId);

    void dispose();

    void addIMessageListener(IMessageListener messageListener);
}
