package com.hiepkhach9x.appcamera.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.entities.Device;

import java.util.ArrayList;

/**
 * Created by hungh on 1/6/2017.
 */

public class CameraAdapter extends ArrayAdapter<Device.Camera> {
    private ArrayList<Device.Camera> cameras;
    private LayoutInflater mLayoutInflater;

    public CameraAdapter(Context context, ArrayList<Device.Camera> cameras) {
        super(context, R.layout.item_camera, cameras);
        this.cameras = cameras;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setCameras(ArrayList<Device.Camera> cameras) {
        if (cameras == null) {
            cameras = new ArrayList<>();
        } else {
            cameras.clear();
        }
        this.cameras.addAll(cameras);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cameras.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Device.Camera camera = cameras.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_camera, parent, false);
            holder.dot = (ImageView) convertView.findViewById(R.id.dot);
            holder.cameraName = (TextView) convertView.findViewById(R.id.camera_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.dot.setImageResource(camera.isOnline() ? R.drawable.dot_online : R.drawable.dot_offline);
        holder.cameraName.setText(camera.getCameraName());
        return convertView;
    }

    class ViewHolder {
        ImageView dot;
        TextView cameraName;
    }
}
