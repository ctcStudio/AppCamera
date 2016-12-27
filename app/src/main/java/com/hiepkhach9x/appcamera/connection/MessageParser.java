package com.hiepkhach9x.appcamera.connection;

import android.text.TextUtils;

import com.hiepkhach9x.appcamera.entities.Device;

import java.util.ArrayList;

/**
 * Created by hungh on 12/27/2016.
 */

public class MessageParser {

    private final String SPERATER1 = "////";
    private final String NEW_LINE = "\n";

    public MessageParser() {
    }

    public ArrayList<Device> parseDevice(String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        if (!message.contains("cameralistbegin")) {
            return null;
        }
        int startList = message.indexOf(NEW_LINE) + 1;
        int endList = message.indexOf("cameralistend");
        String deviceData = message.substring(startList, endList);
        String[] dataDevice = deviceData.split(NEW_LINE);
        if (dataDevice.length < 1) {
            return null;
        }
        ArrayList<Device> devices = new ArrayList<>();
        for (String strDevice : dataDevice) {
            String[] data = strDevice.split(SPERATER1);
            if (data.length > 1) {
                Device device = new Device();
                String deviceName = data[0];
                device.setDeviceName(deviceName);
                ArrayList<Device.Camera> cameras = new ArrayList<>();
                try {
                    for (int i = 1; i < data.length; i = i + 2) {
                        Device.Camera camera = new Device.Camera();
                        camera.setCameraName(data[i]);
                        camera.setCameraId(data[i + 1]);
                        cameras.add(camera);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                device.setCameras(cameras);
                devices.add(device);
            }
        }

        return devices;
    }


    public ArrayList<String> parseIdOnline(String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        if (!message.contains("checkonline")) {
            return null;
        }
        String[] data = message.split(SPERATER1);
        if (data.length > 1) {
            ArrayList<String> listId = new ArrayList<>();
            for (int i = 1; i < data.length; i++) {
                listId.add(data[i]);
            }
            return listId;

        }
        return null;
    }
}
