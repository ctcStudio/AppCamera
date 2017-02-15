package com.hiepkhach9x.appcamera.entities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.connection.MessageParser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by hungnh on 1/20/17.
 */

public class GpsInfo implements Parcelable {
    private double lat; // Latitude of current position. Output format is ddmm.mmmmmm
    private String northOrSouth; // N or S
    private double log; // Longitude of current position. Output format is dddmm.mmmmmm
    private String eastOrWest; // E or W
    private String date; // Date. Output format is ddmmyy
    private String utcTime; // UTC Time. Output format is hhmmss.s
    private String alt; // MSL Altitude. Unit is meters.
    private double speed; // Speed Over Ground. Unit is knots. Cần chuyển về km/h bằng cách nhân với 1.852
    private String course; // Course. Degrees. Hướng chạy của camera với giá trị 0 độ đến 360 độ
    private String address;

    public GpsInfo() {
    }

    public GpsInfo(String gps) {
        parseGps(gps);
    }

    protected GpsInfo(Parcel in) {
        lat = in.readDouble();
        northOrSouth = in.readString();
        log = in.readDouble();
        eastOrWest = in.readString();
        date = in.readString();
        utcTime = in.readString();
        alt = in.readString();
        speed = in.readDouble();
        course = in.readString();
        address = in.readString();
    }

    public static final Creator<GpsInfo> CREATOR = new Creator<GpsInfo>() {
        @Override
        public GpsInfo createFromParcel(Parcel in) {
            return new GpsInfo(in);
        }

        @Override
        public GpsInfo[] newArray(int size) {
            return new GpsInfo[size];
        }
    };

    private void parseGps(String gps) {
//        int startInfo = gps.indexOf(MessageParser.KEY_GPS_INFO);
//        String[] element = gps.substring(startInfo + MessageParser.KEY_GPS_INFO.length())
//                .split(MessageParser.COMA);
        gps = gps.replace(MessageParser.KEY_GPS_INFO, "");
        String[] element = gps.split(MessageParser.COMA);
        try {
            if (element.length > 0) {
                lat = Double.parseDouble(element[0]);
            }

            if (element.length > 1) {
                northOrSouth = element[1];
            }
            if (element.length > 2) {
                log = Double.parseDouble(element[2]);
            }
            if (element.length > 3) {
                eastOrWest = element[3];
            }
            if (element.length > 4) {
                date = element[4];
            }
            if (element.length > 5) {
                utcTime = element[5];
            }

            if (element.length > 6) {
                alt = element[6];
            }

            if (element.length > 7) {
                speed = Math.abs(Double.parseDouble(element[7]));
            }
            if (element.length > 8) {
                alt = element[8];
            }

            // address = getAddressFromGps();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getLat() {
        return convertGpsDecimal(lat);
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getNorthOrSouth() {
        return northOrSouth;
    }

    public void setNorthOrSouth(String northOrSouth) {
        this.northOrSouth = northOrSouth;
    }

    public double getLog() {
        return convertGpsDecimal(log);
    }

    public void setLog(double log) {
        this.log = log;
    }

    public String getEastOrWest() {
        return eastOrWest;
    }

    public void setEastOrWest(String eastOrWest) {
        this.eastOrWest = eastOrWest;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeedKm() {
        return speed * 1.852;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    /**
     * @param pos DDMM.MMMM format
     * @return to double
     */
    private double convertGpsDecimal(double pos) {
        int degrees = (int) (pos / 100);
        double minutes = pos - (degrees * 100);
        return degrees + minutes / 60;
    }

    public String getAddressFromGps() {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(MyApplication.get(), Locale.getDefault());
            addresses = geocoder.getFromLocation(getLat(), getLog(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            return address;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {

        }
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeString(northOrSouth);
        parcel.writeDouble(log);
        parcel.writeString(eastOrWest);
        parcel.writeString(date);
        parcel.writeString(utcTime);
        parcel.writeString(alt);
        parcel.writeDouble(speed);
        parcel.writeString(course);
        parcel.writeString(address);
    }
}
