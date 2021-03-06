package com.hiepkhach9x.appcamera.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hungh on 1/18/2017.
 */

public class Camera implements Parcelable {
    private String cameraGroup;
    private String cameraName;
    private String cameraId;
    private boolean isOnline;

    public Camera() {
    }

    public Camera(String cameraGroup,String cameraName, String cameraId) {
        this.cameraGroup = cameraGroup;
        this.cameraName = cameraName;
        this.cameraId = cameraId;
    }

    protected Camera(Parcel in) {
        cameraGroup = in.readString();
        cameraName = in.readString();
        cameraId = in.readString();
        isOnline = in.readByte() != 0;
    }

    public static final Creator<Camera> CREATOR = new Creator<Camera>() {
        @Override
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        @Override
        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    public String getCameraGroup() {
        return cameraGroup;
    }

    public void setCameraGroup(String cameraGroup) {
        this.cameraGroup = cameraGroup;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public String getCameraId() {
        return cameraId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public String toString() {
        return cameraGroup + "-" + cameraName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cameraGroup);
        dest.writeString(cameraName);
        dest.writeString(cameraId);
        dest.writeByte((byte) (isOnline ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Camera) {
            return cameraId.equals(((Camera) o).getCameraId());
        }
        return super.equals(o);
    }
}