package com.hiepkhach9x.appcamera.entities;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hungnh on 12/28/16.
 */

public class VOData implements Parcelable {
    GpsInfo gpsData;
    Bitmap pictureData;
    long idData;
    String fileName;
    short time;

    public VOData() {
    }


    protected VOData(Parcel in) {
        gpsData = in.readParcelable(GpsInfo.class.getClassLoader());
        pictureData = in.readParcelable(Bitmap.class.getClassLoader());
        idData = in.readLong();
        fileName = in.readString();
    }

    public static final Creator<VOData> CREATOR = new Creator<VOData>() {
        @Override
        public VOData createFromParcel(Parcel in) {
            return new VOData(in);
        }

        @Override
        public VOData[] newArray(int size) {
            return new VOData[size];
        }
    };

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public short getTime() {
        return time;
    }

    public void setTime(short time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(gpsData, flags);
        dest.writeParcelable(pictureData, flags);
        dest.writeLong(idData);
        dest.writeString(fileName);
    }
}
