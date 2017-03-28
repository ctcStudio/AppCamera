package com.hiepkhach9x.appcamera.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.hiepkhach9x.appcamera.connection.MessageParser;

/**
 * Created by hungh on 1/21/2017.
 */

public class StoreData implements Parcelable {
    private String cameraName;
    private String cameraId;
    private String fileName; // yyyy-MM-dd-hh

    public StoreData() {
    }

    public StoreData(String data) {
        String[] element = data.split(MessageParser.SPERATER2);
        if (element.length > 0) {
            cameraId = element[0];
        }

        if (element.length > 1) {
            fileName = element[1];
        }
    }

    @Override
    public String toString() {
        if(TextUtils.isEmpty(cameraName)) {
            return cameraId + " - " + fileName;
        }
        return cameraName + " - " + fileName;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    protected StoreData(Parcel in) {
        cameraName = in.readString();
        cameraId = in.readString();
        fileName = in.readString();
    }

    public static final Creator<StoreData> CREATOR = new Creator<StoreData>() {
        @Override
        public StoreData createFromParcel(Parcel in) {
            return new StoreData(in);
        }

        @Override
        public StoreData[] newArray(int size) {
            return new StoreData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cameraName);
        dest.writeString(cameraId);
        dest.writeString(fileName);
    }
}
