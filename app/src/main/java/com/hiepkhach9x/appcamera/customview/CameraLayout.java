package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.preference.UserPref;

/**
 * Created by hungh on 1/13/2017.
 */

public class CameraLayout extends FrameLayout {

    private CameraView mCameraView;
    private TextView mTxtCameraName, mTxtCameraId;
    private ImageButton mAddFavorite;
    private CamViewListener listener;
    private String mCameraId;

    public CameraLayout(Context context, String cameraId) {
        super(context);
        this.mCameraId = cameraId;
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
        mCameraView.setCameraId(mCameraId);

        mTxtCameraName = (TextView) findViewById(R.id.camera_name);
        mTxtCameraId = (TextView) findViewById(R.id.camera_id);
        mTxtCameraId.setText(mCameraId);
        mAddFavorite = (ImageButton) findViewById(R.id.add_favorite);
        if (UserPref.getInstance().hasCameraFavorite(mCameraId)) {
            mAddFavorite.setBackgroundResource(R.drawable.ic_favorited);
        } else {
            mAddFavorite.setBackgroundResource(R.drawable.ic_favorite);
        }

        mAddFavorite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.clickFavorite(mCameraId);
                }
                UserPref userPref = UserPref.getInstance();
                if(userPref.hasCameraFavorite(mCameraId)) {
                    userPref.removeCameraFavorite(mCameraId);
                    mAddFavorite.setBackgroundResource(R.drawable.ic_favorite);
                } else {
                    userPref.saveCameraFavorite(mCameraId);
                    mAddFavorite.setBackgroundResource(R.drawable.ic_favorited);
                }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTxtCameraId.setText(mCameraId);
    }

    public void setCameraId(String mCameraId) {
        this.mCameraId = mCameraId;
        if (mCameraView != null) {
            mCameraView.initClient();
        }
        mTxtCameraId.setText(mCameraId);
    }

    public void setListener(CamViewListener listener) {
        this.listener = listener;
    }

    public interface CamViewListener {
        void clickFavorite(String cameraId);
    }
}
