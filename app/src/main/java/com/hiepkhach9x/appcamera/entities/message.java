package com.hiepkhach9x.appcamera.entities;

import com.hiepkhach9x.appcamera.connection.MessageType;

/**
 * Created by hungh on 1/4/2017.
 */

public class Message {
    private MessageType messageType;
    private byte[] data;

    public Message() {
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataToString() {
        return new String(data).trim();
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isLoginType() {
        return (messageType == MessageType.LOGIN);
    }

    public boolean isLoginRealTime() {
        return messageType == MessageType.LOGIN_REALTIME;
    }

    public boolean isRealTime() {
        return messageType == MessageType.REALTIME;
    }

    public boolean isCheckOnline() {
        return messageType == MessageType.ONLINE;
    }

    public boolean isStoreData() {
        return messageType == MessageType.GETDATA;
    }
}
