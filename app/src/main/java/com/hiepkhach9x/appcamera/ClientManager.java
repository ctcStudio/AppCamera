package com.hiepkhach9x.appcamera;

import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;

import java.util.ArrayList;

/**
 * Created by hungh on 1/6/2017.
 */

public interface ClientManager {

    void initLoginClient(IMessageListener messageListener);

    void disposeLoginClient();

    void initRealTimeClient(IMessageListener messageListener);

    void disposeRealTimeClient();

    void intStoreClient(IMessageListener messageListener);

    void disposeStoreClient();

    void addLoginListener(IMessageListener messageListener);

    void addRealTimeListener(IMessageListener messageListener);

    void addStoreListener(IMessageListener messageListener);

    void sendLoginMessage(String userName, String pass);

    void sendCheckOnlineMessage(ArrayList<String> listId);

    void sendLoginReadTimeMessage(String userName, String pass);

    void sendGetReadTimeIdMessage(String id);

    void sendGetDataMessage(String userName, String pass, ArrayList<String> listId);

    Client getLoginClient();

    Client getRealTimeClient();

    Client getStoreClient();
}
