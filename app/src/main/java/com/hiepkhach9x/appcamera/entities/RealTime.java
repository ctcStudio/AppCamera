package com.hiepkhach9x.appcamera.entities;

/**
 * Created by hungnh on 12/28/16.
 */

public class RealTime {
    String gpsData;
    String pictureData;

    public RealTime() {
    }

    public RealTime(String gpsData, String pictureData) {
        this.gpsData = gpsData;
        this.pictureData = pictureData;
    }

    public String getGpsData() {
        return gpsData;
    }

    public void setGpsData(String gpsData) {
        this.gpsData = gpsData;
    }

    public String getPictureData() {
        return pictureData;
    }

    public void setPictureData(String pictureData) {
        this.pictureData = pictureData;
    }
}
