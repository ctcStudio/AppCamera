package com.hiepkhach9x.appcamera.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hungnh on 1/20/17.
 */
enum DateType {
    FromDate,
    ToDate;
}

public class PlayBackFragment extends BaseFragment implements View.OnClickListener {

    private static final String KEY_ARG_CAMERA = "key.arg.camera";
    private static final int ARGS_WHAT_SEND_LOGIN_PLAY_BACK = 111;
    private final int ARGS_WHAT_PLAY_BACK = 112;
    private static final int ARGS_WHAT_SEND_PLAY_BACK = 113;

    public static PlayBackFragment newInstance(ArrayList<Camera> cameras) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_ARG_CAMERA, cameras);
        PlayBackFragment fragment = new PlayBackFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<Camera> mCameras;
    private Spinner spinnerCamera;
    private TextView txtFromDate, txtToDate;
    private Date fromDate, toDate;

    private Client playBackClient;
    private MessageParser messageParser;

    private Handler handler;
    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ARGS_WHAT_SEND_LOGIN_PLAY_BACK:
                    if (playBackClient != null) {
                        showDialog();
                        String msg = messageParser.genLoginCallBack(UserPref.getInstance().getUserName(),
                                UserPref.getInstance().getPassword());
                        playBackClient.sendLoginGetDataMessage(msg);
                    }
                    return true;
                case ARGS_WHAT_SEND_PLAY_BACK:
                    dismissDialog();
                    Camera camera = mCameras.get(spinnerCamera.getSelectedItemPosition());
                    String msg = messageParser.genMessagePlayBack(convertDateToString(fromDate),
                            convertDateToString(toDate), camera.getCameraId());
                    if (playBackClient != null)
                        playBackClient.sendGetDataMessage(msg);
                    return true;
                case ARGS_WHAT_PLAY_BACK:
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCameras = savedInstanceState.getParcelableArrayList(KEY_ARG_CAMERA);
        } else if (getArguments() != null) {
            mCameras = getArguments().getParcelableArrayList(KEY_ARG_CAMERA);
        }
        if (mCameras == null) {
            mCameras = new ArrayList<>();
        }
        messageParser = new MessageParser();

        handler = new Handler(callback);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtFromDate = (TextView) view.findViewById(R.id.text_from);
        txtToDate = (TextView) view.findViewById(R.id.text_to);
        spinnerCamera = (Spinner) view.findViewById(R.id.spinner_camera);

        view.findViewById(R.id.from_date).setOnClickListener(this);
        view.findViewById(R.id.to_date).setOnClickListener(this);
        view.findViewById(R.id.get_data).setOnClickListener(this);

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mCameras);
        spinnerCamera.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_play_back;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initClient();
    }

    private void initClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                disposeClient();
                String server = UserPref.getInstance().getServerAddress();
                playBackClient = new Client(server, Config.SERVER_PORT + 1);
            }
        };
        new Thread(runnable).start();
    }

    private void disposeClient() {
        if (playBackClient != null) {
            playBackClient.dispose();
            playBackClient = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposeClient();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void handleMessageClient(MessageClient messageClient) {
        Log.d("HungHN",messageClient.getDataToString());
        if (messageClient.isLoginGetData()) {
            if (handler != null) {
                handler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_PLAY_BACK, 2000);
            }
        } else if (messageClient.isStoreData()) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.from_date:
                showDateTimeDialog(DateType.FromDate);
                break;
            case R.id.to_date:
                showDateTimeDialog(DateType.ToDate);
                break;
            case R.id.get_data:
                if (handler != null) {
                    handler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_LOGIN_PLAY_BACK, 200);
                    showDialog();
                }
                break;
        }
    }

    private String convertDateToString(Date date) {
        try {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return df.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private void showDateTimeDialog(final DateType type) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        if (DateType.FromDate == type) {
                            txtToDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            fromDate = calendar.getTime();
                        } else if (DateType.ToDate == type) {
                            txtToDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            toDate = calendar.getTime();
                        }

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
