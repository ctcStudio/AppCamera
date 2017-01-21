package com.hiepkhach9x.appcamera.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.customview.CameraLayout;
import com.hiepkhach9x.appcamera.customview.ClickCameraInterface;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.GpsInfo;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;

import java.util.ArrayList;

/**
 * Created by hungnh on 1/10/17.
 */

public class ListCameraFragment extends BaseFragment implements OnMapReadyCallback, ClickCameraInterface, CameraLayout.UpdateCameraInfo {

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
    private FrameLayout layoutImage;
    private ImageView imageFull;
    private MapView mapFull;
    private GoogleMap gMap;

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

        layoutImage = (FrameLayout) view.findViewById(R.id.layout_image);
        imageFull = (ImageView) view.findViewById(R.id.image_full);
        mapFull = (MapView) view.findViewById(R.id.map_full);

        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        mapFull.onCreate(savedInstanceState);
        mapFull.getMapAsync(this);

        imageFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutImage.setVisibility(View.GONE);
            }
        });

        mapFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFull.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private CameraLayout createCameraView(Camera camera, Bundle bundle) {
        CameraLayout cameraLayout = new CameraLayout(getContext(), camera, bundle);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cameraLayout.setLayoutParams(layoutParams);
        cameraLayout.initClient();
        cameraLayout.setCameraListener(this);
        return cameraLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewOnResume();
        }
        if (mapFull != null)
            mapFull.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewPause();
        }
        if (mapFull != null)
            mapFull.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewOnDestroy();
        }
        if (mapFull != null)
            mapFull.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        for (CameraLayout layout : listCameraLayout) {
            layout.mapViewOnLowMemory();
        }
        if (mapFull != null)
            mapFull.onLowMemory();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_list_camera;
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
    }

    private void showGpsLocation(double lat, double log) {
        LatLng cam = new LatLng(lat, log);
        MarkerOptions markerOptions = new MarkerOptions().position(cam)
                .title("Camera");

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_oto));
        gMap.addMarker(markerOptions);
        gMap.setMinZoomPreference(15.0f);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(cam));
    }


    @Override
    public void onUpdateInfo(RealTime realTime) {
        if (realTime.getPictureData() != null) {
           imageFull.setImageBitmap(realTime.getPictureData());
        }
        if (realTime.getGpsData() != null) {
            GpsInfo gpsInfo = realTime.getGpsData();
            showGpsLocation(gpsInfo.getLat(), gpsInfo.getLog());
        }
    }

    @Override
    public void onClickCamera(String cameraId) {
        layoutImage.setVisibility(View.VISIBLE);
        mapFull.setVisibility(View.GONE);
        for (CameraLayout cameraLayout : listCameraLayout) {
            if (cameraLayout.hasCamera(cameraId)) {
                cameraLayout.setUpdateCameraInfo(this);
            } else {
                cameraLayout.removeUpdateCameraInfo();
            }
        }
    }

    @Override
    public void onClickMap(String cameraId) {
        layoutImage.setVisibility(View.GONE);
        mapFull.setVisibility(View.VISIBLE);

        for (CameraLayout cameraLayout : listCameraLayout) {
            if (cameraLayout.hasCamera(cameraId)) {
                cameraLayout.setUpdateCameraInfo(this);
            } else {
                cameraLayout.removeUpdateCameraInfo();
            }
        }
    }

    public boolean goBack() {
        if(layoutImage.getVisibility() != View.VISIBLE
                || mapFull.getVisibility() != View.VISIBLE) {
            layoutImage.setVisibility(View.GONE);
            mapFull.setVisibility(View.GONE);
            return false;
        }
        return true;
    }
}
