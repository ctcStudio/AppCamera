package com.hiepkhach9x.appcamera.entities;

import java.util.ArrayList;

/**
 * Created by hungh on 12/27/2016.
 */

public class Device {
    private String deviceName;
    private ArrayList<Camera> cameras;

    public Device(String deviceName, ArrayList<Camera> cameras) {
        this.deviceName = deviceName;
        this.cameras = cameras;
    }

    public Device() {
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public ArrayList<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(ArrayList<Camera> cameras) {
        if(cameras == null) {
            this.cameras = new ArrayList<>();
        }
        this.cameras = cameras;
    }

    @Override
    public String toString() {
        return "Device: " + deviceName + ": NumberCamera: " + cameras.size();
    }

    public static class Camera {
        private String cameraName;
        private String cameraId;

        public Camera() {
        }

        public Camera(String cameraName, String cameraId) {
            this.cameraName = cameraName;
            this.cameraId = cameraId;
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

        @Override
        public String toString() {
            return "Camera: " + cameraName + "---ID: " + cameraId;
        }
    }
}
