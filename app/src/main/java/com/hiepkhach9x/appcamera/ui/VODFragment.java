package com.hiepkhach9x.appcamera.ui;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.hiepkhach9x.appcamera.customview.PlayBackLayout;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.GpsInfo;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.RealTime;
import com.hiepkhach9x.appcamera.entities.StoreData;
import com.hiepkhach9x.appcamera.entities.VOData;

/**
 * Created by hungh on 1/6/2017.
 */

public class VODFragment extends BaseFragment implements OnMapReadyCallback, ClickCameraInterface, PlayBackLayout.UpdateVodInfo {

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
    private FrameLayout layoutImage;
    private ImageView imageFull;
    private MapView mapFull;
    private GoogleMap gMap;

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
        playBackLayout.setCameraListener(this);
        layoutVod.addView(playBackLayout);

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
                mapFull.setVisibility(View.GONE);
                playBackLayout.removeUpdateVodInfo();
            }
        });
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
        if (mapFull != null)
            mapFull.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnPause();
        }
        if (mapFull != null)
            mapFull.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnDestroy();
        }
        if (mapFull != null)
            mapFull.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(playBackLayout !=null) {
            playBackLayout.mapViewOnLowMemory();
        }

        if (mapFull != null)
            mapFull.onLowMemory();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                layoutImage.setVisibility(View.GONE);
                mapFull.setVisibility(View.GONE);
                playBackLayout.removeUpdateVodInfo();
            }
        });
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
    public void onUpdateInfo(VOData voData) {
        if (voData.getPictureData() != null) {
            imageFull.setImageBitmap(voData.getPictureData());
        }

        if (voData.getGpsData() != null) {
            GpsInfo gpsInfo = voData.getGpsData();
            showGpsLocation(gpsInfo.getLat(), gpsInfo.getLog());
        }
    }

    @Override
    public void onClickCamera(String cameraId) {
        layoutImage.setVisibility(View.VISIBLE);
        mapFull.setVisibility(View.GONE);
        playBackLayout.setUpdateVodInfo(this);
    }

    @Override
    public void onClickMap(String cameraId) {
        layoutImage.setVisibility(View.GONE);
        mapFull.setVisibility(View.VISIBLE);
        playBackLayout.setUpdateVodInfo(this);
    }

    public boolean goBack() {
        if(layoutImage.getVisibility() == View.VISIBLE
                || mapFull.getVisibility() == View.VISIBLE) {
            layoutImage.setVisibility(View.GONE);
            mapFull.setVisibility(View.GONE);
            playBackLayout.removeUpdateVodInfo();
            return false;
        }
        return true;
    }
}
