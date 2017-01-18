package com.hiepkhach9x.appcamera.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.entities.Camera;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hungh on 1/18/2017.
 */

public class CameraAdapter extends ArrayAdapter<Camera> {

    List<Camera> cameras;
    LayoutInflater mInflater;
    ArrayList<Camera> cameraSelected;

    public CameraAdapter(Context context, List<Camera> objects) {
        super(context, R.layout.item_camera, objects);
        this.cameras = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cameraSelected = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return cameras != null ? cameras.size() : 0;
    }

    public ArrayList<Camera> getCameraSelected() {
        return cameraSelected;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        final Camera camera = cameras.get(position);
        if (convertView == null) {
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.item_camera, parent, false);
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
        int bgColor = ContextCompat.getColor(getContext(), cameraSelected.contains(camera) ? R.color.colorPrimary
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    class Holder {
        ImageView status;
        TextView cameraName, cameraId;
        LinearLayout layoutCamera;
    }
}
