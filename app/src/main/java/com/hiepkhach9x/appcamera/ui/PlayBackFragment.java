package com.hiepkhach9x.appcamera.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.MyApplication;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.entities.StoreData;
import com.hiepkhach9x.appcamera.preference.UserPref;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hungnh on 1/20/17.
 */
enum DateType {
    FromDate,
    ToDate
}

public class PlayBackFragment extends BaseFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private static final String KEY_ARG_CAMERA = "key.arg.camera";
    private static final String TAG = "TAG_PLAYBACK";
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
    private Spinner spinnerCamera, spinnerSpeed;
    private TextView txtFromDate, txtToDate;
    private ListView listData;
    private Date fromDate, toDate;
    private ArrayList<StoreData> storeDataList;
    private ArrayAdapter storeAdapter;
    private Camera currentCamera;
    private String[] arrSpeeds = new String[]{"x1","x2","x3","x4","x5","x6","x7","x8","x9"};

    private boolean hasLoginSuccess;
    private Client playBackClient;
    private MessageParser messageParser;

    private Handler handler;
    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ARGS_WHAT_SEND_PLAY_BACK:
                    currentCamera = mCameras.get(spinnerCamera.getSelectedItemPosition());
                    ArrayList<String> listId = new ArrayList<>();
                    listId.add(currentCamera.getCameraId());
                    String msg = messageParser.genMessagePlayBack(convertDateToString(fromDate),
                            convertDateToString(toDate), listId);
                    if (playBackClient != null)
                        playBackClient.sendGetDataMessage(msg);
                    return true;
                case ARGS_WHAT_PLAY_BACK:
                    if (message.obj instanceof MessageClient) {
                        MessageClient messageClient = (MessageClient) message.obj;
                        dismissDialog();
                        ArrayList<StoreData> datas = messageParser.parseMessagePlayBack(messageClient, currentCamera.getCameraName());
                        storeDataList.clear();
                        storeDataList.addAll(datas);
                        storeAdapter.notifyDataSetChanged();
                    }
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

        if (storeDataList == null) {
            storeDataList = new ArrayList<>();
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
        spinnerSpeed = (Spinner) view.findViewById(R.id.spinner_speed);
        listData = (ListView) view.findViewById(R.id.list_data);

        Calendar calendar = Calendar.getInstance();
        fromDate = toDate = calendar.getTime();
        txtFromDate.setText(convertDateToString(fromDate));
        txtToDate.setText(convertDateToString(toDate));

        view.findViewById(R.id.from_date).setOnClickListener(this);
        view.findViewById(R.id.to_date).setOnClickListener(this);
        view.findViewById(R.id.get_data).setOnClickListener(this);

        ArrayAdapter spinnerSpeedAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, arrSpeeds);
        spinnerSpeed.setAdapter(spinnerSpeedAdapter);


        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mCameras);
        spinnerCamera.setAdapter(spinnerArrayAdapter);

        storeAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, storeDataList);
        listData.setAdapter(storeAdapter);

        listData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StoreData storeData = storeDataList.get(position);
                int playSpeed = spinnerSpeed.getSelectedItemPosition();
                VODFragment vodFragment = VODFragment.newInstance(currentCamera, storeData, playSpeed);
                if (mNavigateManager != null) {
                    mNavigateManager.addPage(vodFragment, MainActivity.TAG_VOD);
                }
            }
        });
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
                playBackClient = new Client(server, Config.SERVER_STORE_PORT);
                playBackClient.addIMessageListener(playBackListener);

                String msg = messageParser.genLoginCallBack(MyApplication.get().getUserName(),
                        MyApplication.get().getPassword());
                playBackClient.sendLoginGetDataMessage(msg);

            }
        };
        showDialog();
        storeDataList.clear();
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
                if (hasLoginSuccess) {
                    if (handler != null) {
                        handler.sendEmptyMessageDelayed(ARGS_WHAT_SEND_PLAY_BACK, 200);
                        showDialog();
                    }
                } else {
                    Toast.makeText(getContext(), "Chưa kết nối đến server thành công !", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String convertDateToString(Date date) {
        try {
            @SuppressLint("SimpleDateFormat")
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private DateType selectedType = DateType.FromDate;

    private void showDateTimeDialog(DateType type) {
        selectedType = type;
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setVersion(DatePickerDialog.Version.VERSION_1);
        datePickerDialog.show(getChildFragmentManager(), TAG);
    }

    private IMessageListener playBackListener = new IMessageListener() {
        @Override
        public String getLsTag() {
            return TAG;
        }

        @Override
        public void handleMessage(MessageClient messageClient) {
            Log.d("HungHN", messageClient.getDataToString());
            if (messageClient.isLoginGetData()) {
                if (!hasLoginSuccess) {
                    hasLoginSuccess = (messageClient.getDataToString().contains("yeucaulai"));
                }
                dismissDialog();
            } else if (messageClient.isStoreData()) {
                if (handler != null) {
                    Message message = new Message();
                    message.what = ARGS_WHAT_PLAY_BACK;
                    message.obj = messageClient;
                    handler.sendMessage(message);
                }
            }
        }
    };

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (selectedType == DateType.FromDate) {
            fromDate = calendar.getTime();
            txtFromDate.setText(String.format(Locale.ENGLISH, "%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
        } else if (selectedType == DateType.ToDate) {
            txtFromDate.setText(String.format(Locale.ENGLISH, "%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
            toDate = calendar.getTime();
        }
    }
}
