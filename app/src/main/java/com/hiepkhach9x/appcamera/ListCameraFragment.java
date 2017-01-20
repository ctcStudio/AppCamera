package com.hiepkhach9x.appcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hiepkhach9x.appcamera.adapter.ListCameraAdapter;
import com.hiepkhach9x.appcamera.customview.CameraLayout;
import com.hiepkhach9x.appcamera.customview.CameraView;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.util.ArrayList;

/**
 * Created by hungnh on 1/10/17.
 */

public class ListCameraFragment extends BaseFragment implements CameraLayout.CamViewListener {

    private static final String ARGS_LIST_CAMERA = "args.list.camera";

    public static ListCameraFragment newInstance(ArrayList<Camera> listCamera) {

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_LIST_CAMERA, listCamera);
        ListCameraFragment fragment = new ListCameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<Camera> listCamera;
//    private RecyclerView mRecyclerView;
//    private ListCameraAdapter cameraAdapter;
    private LinearLayout mLayoutCamera;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            listCamera = savedInstanceState.getParcelableArrayList(ARGS_LIST_CAMERA);
        } else if (getArguments() != null) {
            listCamera = getArguments().getParcelableArrayList(ARGS_LIST_CAMERA);
        }
        if(listCamera == null) {
            listCamera = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARGS_LIST_CAMERA, listCamera);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_camera);
//        cameraAdapter = new ListCameraAdapter(getContext(),listCamera);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(layoutManager);
//        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setAdapter(cameraAdapter);
        mLayoutCamera = (LinearLayout) view.findViewById(R.id.layout_camera);
        for (Camera camera: listCamera) {
            mLayoutCamera.addView(createCameraView(camera));
        }
        mLayoutCamera.invalidate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private View createCameraView(Camera camera) {
        CameraLayout cameraLayout = new CameraLayout(getContext(),camera);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cameraLayout.setLayoutParams(layoutParams);
        cameraLayout.initClient();
        return cameraLayout;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_list_camera;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {

    }

    @Override
    public void clickFavorite(String cameraId) {

    }
}
