package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.entities.Camera;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hungh on 1/13/2017.
 */

public class CameraLayout extends FrameLayout {

    private final String INFO_FORMAT = "%s %s %s";
    private final String SPEED_FORMAT = "%d km/h";

    private CameraView mCameraView;
    private TextView mTxtCameraInfo, mTxtCameraAddress, mTxtCameraSpeed;
    private Button mGps;
    private CamViewListener listener;
    private Camera mCamera;
    private View mapView;

    public CameraLayout(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        initializeViews(context);
    }

    public CameraLayout(Context context) {
        super(context);
        initializeViews(context);
    }

    public CameraLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public CameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_list_camera, this);

        mCameraView = (CameraView) findViewById(R.id.camera);
        mCameraView.setCameraId(mCamera.getCameraId());

        mTxtCameraInfo = (TextView) findViewById(R.id.camera_info);
        setCameraInfo();

        mTxtCameraAddress = (TextView) findViewById(R.id.camera_address);
        mTxtCameraAddress.setText("Ha Noi");

        mTxtCameraSpeed = (TextView) findViewById(R.id.camera_speed);
        setCameraSpeed();

        mapView = findViewById(R.id.map_view);
        mGps = (Button) findViewById(R.id.gps);

        mGps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapView.getVisibility() == VISIBLE) {
                    mapView.setVisibility(GONE);
                } else {
                    mapView.setVisibility(VISIBLE);
                }
            }
        });
    }

    private void setCameraSpeed() {
        String speed = String.format(SPEED_FORMAT,50);
        mTxtCameraSpeed.setText(speed);
    }

    private void setCameraInfo() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String info = String.format(INFO_FORMAT, df.format(date), mCamera.getCameraName(), mCamera.getCameraId());
        mTxtCameraInfo.setText(mCamera.getCameraName());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mCamera != null)
            mTxtCameraAddress.setText(mCamera.getCameraId());
    }

    public void setCameraId(Camera camera) {
        this.mCamera = camera;
        if (mCameraView != null) {
            mCameraView.initClient();
        }
        if (mCamera != null)
            mTxtCameraAddress.setText(camera.getCameraId());
    }

    public void setListener(CamViewListener listener) {
        this.listener = listener;
    }

    public interface CamViewListener {
        void clickFavorite(String cameraId);
    }
}
