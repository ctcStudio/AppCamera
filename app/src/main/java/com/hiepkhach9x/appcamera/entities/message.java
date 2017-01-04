package com.hiepkhach9x.appcamera.entities;

import com.hiepkhach9x.appcamera.connection.MessageType;

/**
 * Created by hungh on 1/4/2017.
 */

public class Message {
    private MessageType messageType;
    private String data;

    public Message() {
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isLoginType() {
        return (messageType == MessageType.LOGIN);
    }
}
