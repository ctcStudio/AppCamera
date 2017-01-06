package com.hiepkhach9x.appcamera.entities;

import android.graphics.Bitmap;

/**
 * Created by hungnh on 12/28/16.
 */

public class RealTime {
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
}
