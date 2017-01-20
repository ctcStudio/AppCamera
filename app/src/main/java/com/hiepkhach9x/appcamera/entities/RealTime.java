package com.hiepkhach9x.appcamera.entities;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hungnh on 12/28/16.
 */

public class RealTime implements Parcelable {
    GpsInfo gpsData;
    Bitmap pictureData;
    long idData;

    public RealTime() {
    }

    public RealTime(GpsInfo gpsData, Bitmap pictureData,long idData) {
        this.gpsData = gpsData;
        this.pictureData = pictureData;
        this.idData = idData;
    }

    public GpsInfo getGpsData() {
        return gpsData;
    }

    public void setGpsData(GpsInfo gpsData) {
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

    protected RealTime(Parcel in) {
        gpsData = in.readParcelable(GpsInfo.class.getClassLoader());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(gpsData, i);
        parcel.writeParcelable(pictureData, i);
        parcel.writeLong(idData);
    }
}
