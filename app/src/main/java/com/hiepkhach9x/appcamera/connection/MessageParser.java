package com.hiepkhach9x.appcamera.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.GpsInfo;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;
import com.hiepkhach9x.appcamera.util.BitConverter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hungh on 12/27/2016.
 */

public class MessageParser {

    public static final String SPERATER1 = "////";
    public static final String COMA = ",";
    public static final String KEY_GPS = "<GPS>";
    public static final String KEY_GPS_INFO = "+CGPSINFO:";
    public static final String NEW_LINE = "\n";

    public MessageParser() {
    }

    public ArrayList<Device> parseDevice(String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        if (!message.contains("cameralistbegin")) {
            return null;
        }
        int startList = message.indexOf("cameralistbegin") + 1;
        int endList = message.indexOf("cameralistend");
        String deviceData = message.substring(startList, endList);
        String[] dataDevice = deviceData.split(NEW_LINE);
        if (dataDevice.length < 1) {
            return null;
        }
        ArrayList<Device> devices = new ArrayList<>();
        for (String strDevice : dataDevice) {
            String[] data = strDevice.trim().replace(NEW_LINE, "").split(SPERATER1);
            if (data.length > 1) {
                Device device = new Device();
                String deviceName = data[0];
                device.setDeviceName(deviceName);
                ArrayList<Camera> cameras = new ArrayList<>();
                try {
                    for (int i = 1; i < data.length; i = i + 2) {
                        Camera camera = new Camera();
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

    public RealTime parseRealTimeMessage(MessageClient messageClient) {
        if (!messageClient.isRealTime()) {
            return null;
        }

        byte[] bytes = messageClient.getData();
        int beginPic = 0;
        int endPic = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            if (((bytes[i] == (byte) 0xFF)
                    && (bytes[i + 1] == (byte) 0xD8))) {
                beginPic = i;
            }

            if (((bytes[i] == (byte) 0xFF)
                    && (bytes[i + 1] == (byte) 0xD9))) {
                endPic = i + 1;
                break;
            }
        }
        if (endPic - beginPic > 100) {
            byte[] gpsBytes = Arrays.copyOf(bytes, beginPic);
            String gpsAll = new String(gpsBytes).trim();
            Log.d("HungHN", "gps: " + gpsAll);
            int startGps = gpsAll.indexOf(KEY_GPS) + KEY_GPS.length();
            int endGps = gpsAll.lastIndexOf(KEY_GPS);
            String gps = "";
            if (startGps < endGps) {
                gps = gpsAll.substring(startGps, endGps);
            }
            GpsInfo gpsInfo = new GpsInfo(gps);
            byte[] pic = Arrays.copyOfRange(bytes, beginPic, endPic + 1); // picture from 0xFF8 to 0xFF9
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
            Log.d("HungHN", "has bitmap: " + (bitmap != null));
            long cameId = 0;
            if (endPic + 8 <= bytes.length) {
                cameId = BitConverter.toInt64(bytes, endPic + 1); // 8 byte sau picture
                Log.d("HungHN", "ID camera: " + cameId);
            }
            return new RealTime(gpsInfo, bitmap, cameId);
        }

        return null;
    }

    public String genMessageCheckOnline(ArrayList<String> listId) {
        StringBuilder builder = new StringBuilder("@message@checkonline@message@////");
        for (String id : listId) {
            builder.append(id + SPERATER1);
        }
        return builder.toString();
    }

    public String genMessageLogin(String userName, String pass) {
        return "@haicuong@:" + userName + ":" + pass;
    }

    public String genLoginRealTime(String userName, String password) {
        return "@haicuongplayer@:" + userName + ":" + password;
    }

    public String genMessageRealTime(String cameraId) {
        return "@message@yeucaulive@message@" + SPERATER1 + cameraId + SPERATER1;
    }

    public String genLoginCallBack(String userName, String password) {
        return "@haicuong@:" + userName + ":" + password;
    }

    public String genMessagePlayBack(String fromDate, String toDate, String cameraId) {
        return null;
    }
}
