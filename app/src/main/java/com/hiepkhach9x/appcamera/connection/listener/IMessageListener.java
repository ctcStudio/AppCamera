package com.hiepkhach9x.appcamera.connection.listener;

import com.hiepkhach9x.appcamera.entities.Message;

/**
 * Created by hungh on 12/27/2016.
 */

public interface IMessageListener {
    String getTag();
    void handleMessage(Message message);
}
