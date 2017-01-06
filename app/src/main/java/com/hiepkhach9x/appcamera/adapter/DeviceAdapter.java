package com.hiepkhach9x.appcamera.adapter;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.customview.CustomListView;
import com.hiepkhach9x.appcamera.entities.Device;

import java.util.ArrayList;

/**
 * Created by hungh on 1/6/2017.
 */

public class DeviceAdapter extends BaseExpandableListAdapter {
    private ArrayList<Device> mDevices;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public DeviceAdapter(Context context, ArrayList<Device> devices) {
        this.mContext = context;
        if (devices != null)
            this.mDevices = devices;
        else this.mDevices = new ArrayList<>();
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Device.Camera> cameras = mDevices.get(groupPosition).getCameras();
        return cameras == null ? 0 : cameras.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDevices.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<Device.Camera> cameras = mDevices.get(groupPosition).getCameras();
        return cameras != null ? cameras.get(childPosition) : null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Device device = mDevices.get(groupPosition);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_device, parent, false);
        }
        TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
        deviceName.setText(device.getDeviceName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ArrayList<Device.Camera> cameras = mDevices.get(groupPosition).getCameras();
        Device.Camera camera = cameras.get(childPosition);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_camera, parent, false);
        }
        if(camera !=null) {
            ImageView dot = (ImageView) convertView.findViewById(R.id.dot);
            TextView cameraName = (TextView) convertView.findViewById(R.id.camera_name);

            dot.setImageResource(camera.isOnline() ? R.drawable.dot_online : R.drawable.dot_offline);
            cameraName.setText(camera.getCameraName());
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
