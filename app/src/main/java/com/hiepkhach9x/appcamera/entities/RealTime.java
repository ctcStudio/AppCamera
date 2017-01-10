package com.hiepkhach9x.appcamera.entities;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hungnh on 12/28/16.
 */

public class RealTime implements Parcelable {
    String gpsData;
    Bitmap pictureData;
    long idData;

    public RealTime() {
    }

    public RealTime(String gpsData, Bitmap pictureData,long idData) {
        this.gpsData = gpsData;
        this.pictureData = pictureData;
        this.idData = idData;
    }

    protected RealTime(Parcel in) {
        gpsData = in.readString();
        pictureData = in.readParcelable(Bitmap.class.getClassLoader());
        idData = in.readLong();
    }

    public static final Creator<RealTime> CREATOR = new Creator<RealTime>() {
        @Override
        public RealTime createFromParcel(Parcel in) {
            return new RealTime(in);
        }

        @Override
        public RealTime[] newArray(int size) {
            return new RealTime[size];
        }
    };

    public String getGpsData() {
        return gpsData;
    }

    public void setGpsData(String gpsData) {
        this.gpsData = gpsData;
    }

    public Bitmap getPictureData() {
        return pictureData;
    }

    public void setPictureData(Bitmap pictureData) {
        this.pictureData = pictureData;
    }

    public long getIdData() {
        return idData;
    }

    public void setIdData(long idData) {
        this.idData = idData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(gpsData);
        parcel.writeParcelable(pictureData, i);
        parcel.writeLong(idData);
    }
}
