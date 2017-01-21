package com.hiepkhach9x.appcamera.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.customview.CameraLayout;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.util.ArrayList;

/**
 * Created by hungnh on 1/10/17.
 */

public class ListCameraFragment extends BaseFragment {

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
        if (listCamera == null) {
            listCamera = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARGS_LIST_CAMERA, listCamera);
    }

    private ArrayList<CameraLayout> listCameraLayout;

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
        if (listCameraLayout == null) {
            listCameraLayout = new ArrayList<>();
        } else {
            listCameraLayout.clear();
        }
        for (Camera camera : listCamera) {
            CameraLayout cameraLayout = createCameraView(camera, savedInstanceState);
            listCameraLayout.add(cameraLayout);
            mLayoutCamera.addView(cameraLayout);
        }
        mLayoutCamera.invalidate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private CameraLayout createCameraView(Camera camera, Bundle bundle) {
        CameraLayout cameraLayout = new CameraLayout(getContext(), camera,bundle);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cameraLayout.setLayoutParams(layoutParams);
        cameraLayout.initClient();
        return cameraLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewOnResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewOnDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewOnLowMemory();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_list_camera;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {

    }
}
