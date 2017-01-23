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
import com.hiepkhach9x.appcamera.entities.StoreData;
import com.hiepkhach9x.appcamera.entities.VOData;
import com.hiepkhach9x.appcamera.preference.UserPref;
import com.hiepkhach9x.appcamera.util.BitConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hungh on 12/27/2016.
 */

public class MessageParser {

    public static final String SPERATER1 = "////";
    public static final String SPERATER2 = "\\\\";
    public static final String BEGIN = "@begin@";
    public static final String BACKSLASH = "\\";
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

    private long lastGetTime = 0;
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
            GpsInfo gpsInfo;
            if (TextUtils.isEmpty(gps)) {
                gpsInfo = new GpsInfo();
            } else {
                gpsInfo = new GpsInfo(gps);
                if(System.currentTimeMillis() - lastGetTime > 10000) {
                    gpsInfo.getAddress();
                    lastGetTime = System.currentTimeMillis();
                }
            }
            byte[] pic = Arrays.copyOfRange(bytes, beginPic, endPic + 1); // picture from 0xFF8 to 0xFF9
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(pic, 0, pic.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 500, 500);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
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

    public ArrayList<StoreData> parseMessagePlayBack(MessageClient messageClient) {
        if (messageClient == null || !messageClient.isStoreData()) {
            return null;
        }
        String data = messageClient.getDataToString();
        data = data.replace("@message@timkiemxemlai@message@", "");
        String[] datas = data.split(BEGIN);
        ArrayList<StoreData> storeDatas = new ArrayList<>();
        for (String str : datas) {
            if (str.contains(BACKSLASH)) {
                StoreData storeData = new StoreData(str);
                storeDatas.add(storeData);
            }
        }
        return storeDatas;
    }


    public VOData parseVODMessage(MessageClient messageClient) {
        //TODO Có hiện tượng chồng chéo bytes dữ liệu
        if (!messageClient.isVOData()) {
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
            GpsInfo gpsInfo = null;
            if (!TextUtils.isEmpty(gps)) {
                gpsInfo = new GpsInfo(gps);
                if(System.currentTimeMillis() - lastGetTime > 10000) {
                    gpsInfo.getAddress();
                    lastGetTime = System.currentTimeMillis();
                }
            }
            byte[] pic = Arrays.copyOfRange(bytes, beginPic, endPic + 1); // picture from 0xFF8 to 0xFF9
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(pic, 0, pic.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 500, 500);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
            Log.d("HungHN", "has bitmap: " + (bitmap != null));
            long cameId = 0;
            if (endPic + 8 <= bytes.length) {
                cameId = BitConverter.toInt64(bytes, endPic + 1); // 8 byte sau picture
                Log.d("HungHN", "ID camera: " + cameId);
            }

            VOData data = new VOData();
            data.setGpsData(gpsInfo);
            data.setPictureData(bitmap);
            data.setIdData(cameId);

            int startFileName = endPic + 9;
            int endFileName = startFileName + 13;
            if (endFileName  < bytes.length) {
                byte[] fileByte = Arrays.copyOfRange(bytes, startFileName, endFileName);
                String fileName = new String(fileByte).trim();
                Log.d("HungHN","FileName: " + fileName);
                data.setFileName(fileName);
            }
            if(endFileName + 2 < bytes.length) {
                short time = BitConverter.toInt16(bytes,endFileName);
                Log.d("Hung","Time: " + time);
                data.setTime(time);
            }

            return data;
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

    public String genMessagePlayBack(String fromDate, String toDate, ArrayList<String> listId) {
        String userName = UserPref.getInstance().getUserName();
        String pass = UserPref.getInstance().getPassword();
        StringBuilder builder = new StringBuilder("@message@timkiemxemlai@message@");
        for (String id : listId) {
            builder.append("@begin@" + userName + SPERATER1 + pass + SPERATER1 + id + SPERATER1 + fromDate + SPERATER1 + toDate);
        }
        return builder.toString();
    }

    public byte[] genMessageVODData(String cameraId, String fileName, short time, char playSpeed) {
        try {
            String msg = "@message@yeucauVOD@message@";
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bytes.write(msg.getBytes());
            long id = Long.parseLong(cameraId);
            bytes.write(BitConverter.getBytes(id));
            bytes.write(fileName.getBytes());
            bytes.write(BitConverter.getBytes(time));
            bytes.write(BitConverter.getBytes(playSpeed));
            return bytes.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
