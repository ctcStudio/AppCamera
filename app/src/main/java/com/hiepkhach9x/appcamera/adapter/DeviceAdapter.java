package com.hiepkhach9x.appcamera.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.Device;

import java.util.ArrayList;

/**
 * Created by hungh on 1/6/2017.
 */

public class DeviceAdapter extends BaseExpandableListAdapter {
    private ArrayList<Device> mDevices;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    ArrayList<Camera> cameraSelected;

    public DeviceAdapter(Context context, ArrayList<Device> devices) {
        this.mContext = context;
        if (devices != null) this.mDevices = devices;
        else this.mDevices = new ArrayList<>();
        cameraSelected = new ArrayList<>();
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ArrayList<Camera> getCameraSelected() {
        return cameraSelected;
    }

    @Override
    public int getGroupCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Camera> cameras = mDevices.get(groupPosition).getCameras();
        return cameras == null ? 0 : cameras.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDevices.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<Camera> cameras = mDevices.get(groupPosition).getCameras();
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
        ArrayList<Camera> cameras = mDevices.get(groupPosition).getCameras();
        Holder holder;
        final Camera camera = cameras.get(childPosition);
        if (convertView == null) {
            holder = new Holder();
            convertView = mLayoutInflater.inflate(R.layout.item_camera, parent, false);
            holder.status = (ImageView) convertView.findViewById(R.id.status_camera);
            holder.cameraName = (TextView) convertView.findViewById(R.id.camera_name);
            holder.cameraId = (TextView) convertView.findViewById(R.id.camera_id);
            holder.layoutCamera = (LinearLayout) convertView.findViewById(R.id.layout_device_camera);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.cameraName.setText(camera.getCameraName());
        holder.cameraId.setText(camera.getCameraId());
        holder.status.setImageResource(camera.isOnline() ? R.drawable.dot_online : R.drawable.dot_offline);
        int bgColor = ContextCompat.getColor(mContext, cameraSelected.contains(camera) ? R.color.colorPrimary
                : R.color.colorAccent);
        holder.layoutCamera.setBackgroundColor(bgColor);
        holder.layoutCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraSelected.size() >= 4) {
                    showAlertDialogMaxSelected();
                    return;
                }
                if (cameraSelected.contains(camera)) {
                    cameraSelected.remove(camera);
                } else {
                    cameraSelected.add(camera);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private void showAlertDialogMaxSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Alert");
        builder.setMessage("Bạn chỉ chọn được tối đa 4 camera một lúc");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class Holder {
        ImageView status;
        TextView cameraName, cameraId;
        LinearLayout layoutCamera;
    }
}
