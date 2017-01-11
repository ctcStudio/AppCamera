package com.hiepkhach9x.appcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiepkhach9x.appcamera.adapter.ListCameraAdapter;
import com.hiepkhach9x.appcamera.entities.MessageClient;

import java.util.ArrayList;

/**
 * Created by hungnh on 1/10/17.
 */

public class ListCameraFragment extends BaseFragment {

    private static final String ARGS_LIST_CAMERA = "args.list.camera";

    public static ListCameraFragment newInstance(ArrayList<String> listCamera) {

        Bundle args = new Bundle();
        args.putStringArrayList(ARGS_LIST_CAMERA, listCamera);
        ListCameraFragment fragment = new ListCameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<String> listCamera;
    private RecyclerView mRecyclerView;
    private ListCameraAdapter cameraAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            listCamera = savedInstanceState.getStringArrayList(ARGS_LIST_CAMERA);
        } else if (getArguments() != null) {
            listCamera = getArguments().getStringArrayList(ARGS_LIST_CAMERA);
        }
        if(listCamera == null) {
            listCamera = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(ARGS_LIST_CAMERA, listCamera);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_camera);
        cameraAdapter = new ListCameraAdapter(getContext(),listCamera);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(cameraAdapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_list_camera;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {

    }
}
