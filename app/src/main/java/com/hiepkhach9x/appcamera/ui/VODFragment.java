package com.hiepkhach9x.appcamera.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.customview.PlayBackLayout;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.StoreData;

/**
 * Created by hungh on 1/6/2017.
 */

public class VODFragment extends BaseFragment {

    private static final String ARGS_STORE_DATA = "args.store.data";
    private static final String ARGS_CAMERA = "args.camera";
    private StoreData mStoreData;
    private Camera mCamera;

    public static VODFragment newInstance(Camera camera, StoreData storeData) {

        Bundle args = new Bundle();
        args.putParcelable(ARGS_STORE_DATA, storeData);
        args.putParcelable(ARGS_CAMERA,camera);
        VODFragment fragment = new VODFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private LinearLayout layoutVod;
    private PlayBackLayout playBackLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStoreData = savedInstanceState.getParcelable(ARGS_STORE_DATA);
            mCamera = savedInstanceState.getParcelable(ARGS_CAMERA);
        } else if (getArguments() != null) {
            mStoreData = getArguments().getParcelable(ARGS_STORE_DATA);
            mCamera = getArguments().getParcelable(ARGS_CAMERA);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARGS_STORE_DATA, mStoreData);
        outState.putParcelable(ARGS_CAMERA,mCamera);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutVod = (LinearLayout) view.findViewById(R.id.layout_vod);
        playBackLayout = new PlayBackLayout(getContext(), mCamera, mStoreData.getFileName(), savedInstanceState);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playBackLayout.setLayoutParams(layoutParams);
        playBackLayout.initClient();
        layoutVod.addView(playBackLayout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_vod;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {

    }

    public boolean goBack() {
        return true;
    }
}
