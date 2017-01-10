package com.hiepkhach9x.appcamera.connection.listener;

import com.hiepkhach9x.appcamera.entities.MessageClient;

/**
 * Created by hungh on 12/27/2016.
 */

public interface IMessageListener {
    String getLsTag();
    void handleMessage(MessageClient messageClient);
}
