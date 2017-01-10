package com.hiepkhach9x.appcamera.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hungnh on 1/10/17.
 */

public class ListCameraAdapter extends RecyclerView.Adapter<ListCameraAdapter.CameraHolder> {


    @Override
    public CameraHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(CameraHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    class CameraHolder extends RecyclerView.ViewHolder {

        public CameraHolder(View itemView) {
            super(itemView);
        }
    }
}
