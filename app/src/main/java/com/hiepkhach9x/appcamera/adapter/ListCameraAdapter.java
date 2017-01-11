package com.hiepkhach9x.appcamera.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.customview.CameraView;

import java.util.ArrayList;

/**
 * Created by hungnh on 1/10/17.
 */

public class ListCameraAdapter extends RecyclerView.Adapter<ListCameraAdapter.CameraHolder> {
    private Context ctx;
    private ArrayList<String> mCameras;
    private LayoutInflater mInflater;

    public ListCameraAdapter(Context context, ArrayList<String> listCamera) {
        this.ctx = context;
        this.mCameras = listCamera;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public CameraHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_list_camera, parent, false);
        CameraHolder cameraHolder = new CameraHolder(view);
        return cameraHolder;
    }

    @Override
    public void onBindViewHolder(CameraHolder holder, int position) {
        String id = mCameras.get(position);
        holder.updateCameraId(id);
    }

    @Override
    public int getItemCount() {
        return mCameras != null ? mCameras.size() : 0;
    }


    class CameraHolder extends RecyclerView.ViewHolder {
        CameraView cameraView;
        ImageButton addFavorite;

        public CameraHolder(View itemView) {
            super(itemView);
            cameraView = (CameraView) itemView.findViewById(R.id.camera);
            addFavorite = (ImageButton) itemView.findViewById(R.id.add_favorite);
        }


        public void updateCameraId(String id) {
            if (cameraView != null) {
                cameraView.setCameraId(id);
            }
        }
    }
}
