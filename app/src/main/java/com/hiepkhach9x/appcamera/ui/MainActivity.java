package com.hiepkhach9x.appcamera.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.NetworkChangeReceiver;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
import com.hiepkhach9x.appcamera.entities.Device;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.preference.UserPref;
import com.hiepkhach9x.appcamera.util.NetworkUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigateManager, LoginClient, NetworkChangeReceiver.ConnectivityReceiverListener {
    public static final String TAG_LOGIN = "LOGIN-FRAGMENT";
    public static final String TAG_HOME = "HOME-FRAGMENT";
    public static final String TAG_CAMERA = "CAMERA-FRAGMENT";
    public static final String TAG_PLAY_BACK = "TAG-PLAYBACK-FRAGMENT";
    public static final String TAG_VOD = "VOD-FRAGMENT";

    private TextView btnLeft, btnRight;
    private ImageView ivBack, ivHome;
    private ArrayList<Camera> cameras;
    private ArrayList<Device> devices;
    private BroadcastReceiver mClientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Client.ACTION_CLIENT_RECEIVE_MESSAGE.equals(action)) {
                int code = intent.getIntExtra(Client.EXTRA_ERROR_MESSAGE, -1);
                showDialogErrorCode(code);
                Fragment activePage = getActivePage();
                if (activePage instanceof BaseFragment) {
                    ((BaseFragment) activePage).dismissDialog();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initActionBar();

        Fragment fragment = getActivePage();
        if (fragment == null) {
            LoginFragment loginFragment = new LoginFragment();
            swapPage(loginFragment, TAG_LOGIN);
        }

        NetworkChangeReceiver.setConnectivityReceiverListener(this);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Client.ACTION_CLIENT_RECEIVE_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mClientReceiver, intentFilter);
    }

    private void initActionBar() {
        // Inflate your custom layout
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        final View actionBarLayout = getLayoutInflater().inflate(R.layout.layout_actionbar, null);
        btnLeft = (TextView) actionBarLayout.findViewById(R.id.button_left);
        ivBack = (ImageView) actionBarLayout.findViewById(R.id.imageBack);
        btnRight = (TextView) actionBarLayout.findViewById(R.id.button_right);
        ivHome = (ImageView) actionBarLayout.findViewById(R.id.icon_home);
        btnLeft.setOnClickListener(actionLeftClickListener);
        btnRight.setOnClickListener(actionRightClickListener);
        ivBack.setOnClickListener(actionLeftClickListener);
        ivHome.setOnClickListener(actionHomeClickListener);

        // Set up your ActionBar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout,lp);

            Toolbar parent = (Toolbar) actionBarLayout.getParent();
            if (parent != null)
                parent.setContentInsetsAbsolute(0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeClient();
        if (mClientReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mClientReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment activePage = getActivePage();
        if (activePage instanceof ListCameraFragment) {
            if (((ListCameraFragment) activePage).goBack()) {
                super.onBackPressed();
            }
        } else if (activePage instanceof VODFragment) {
            if (((VODFragment) activePage).goBack()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Fragment getActivePage() {
        FragmentManager manager = getSupportFragmentManager();
        return manager.findFragmentById(getContentLayout());
    }

    @Override
    public void addPage(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getContentLayout(), fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void swapPage(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getContentLayout(), fragment, tag);
        transaction.commit();
    }

    @Override
    public void syncTitle() {

    }

    private View.OnClickListener actionHomeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment activePage = getActivePage();
            if (activePage instanceof ListCameraFragment
                    || activePage instanceof VODFragment
                    || activePage instanceof PlayBackFragment) {
                HomeFragment homeFragment = HomeFragment.newInstance(getListDevice());
                swapPage(homeFragment, TAG_HOME);
            }
        }
    };


    View.OnClickListener actionLeftClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Fragment activePage = getActivePage();
            if (activePage instanceof HomeFragment) {
                ((HomeFragment) activePage).gotoPlayBack();
            } else if (activePage instanceof ListCameraFragment
                    || activePage instanceof VODFragment) {
                onBackPressed();
            } else if (activePage instanceof PlayBackFragment) {
                HomeFragment homeFragment = HomeFragment.newInstance(getListDevice());
                swapPage(homeFragment, TAG_HOME);
            }
        }
    };

    @Override
    public void syncLeftButton() {
        Fragment activePage = getActivePage();
        int visibility = View.VISIBLE;
        String text = "Back";
        if (activePage instanceof LoginFragment) {
            visibility = View.GONE;
            ivBack.setVisibility(View.GONE);
        } else if (activePage instanceof HomeFragment) {
            visibility = View.VISIBLE;
            text = "Play back";
            ivBack.setVisibility(View.GONE);
        } else if (activePage instanceof ListCameraFragment) {
            visibility = View.GONE;
            ivBack.setVisibility(View.VISIBLE);
        } else if (activePage instanceof PlayBackFragment) {
            visibility = View.GONE;
            ivBack.setVisibility(View.VISIBLE);
        } else if (activePage instanceof VODFragment) {
            visibility = View.GONE;
            ivBack.setVisibility(View.VISIBLE);
        }
        btnLeft.setText(text);
        btnLeft.setVisibility(visibility);
    }


    View.OnClickListener actionRightClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Fragment activePage = getActivePage();
            if (activePage instanceof HomeFragment) {
                ((HomeFragment) activePage).gotoRealTime();
            } else if (activePage instanceof PlayBackFragment
                    || activePage instanceof VODFragment) {
                HomeFragment homeFragment = HomeFragment.newInstance(getListDevice());
                swapPage(homeFragment, TAG_HOME);
            } else if (activePage instanceof ListCameraFragment) {
                PlayBackFragment playBackFragment = PlayBackFragment.newInstance(getListCamera());
                swapPage(playBackFragment, TAG_PLAY_BACK);
            }
        }
    };

    @Override
    public void syncRightButton() {
        Fragment activePage = getActivePage();
        int visibility = View.VISIBLE;
        String text = "Online";
        if (activePage instanceof LoginFragment) {
            visibility = View.GONE;
        } else if (activePage instanceof HomeFragment) {
            visibility = View.VISIBLE;
        } else if (activePage instanceof ListCameraFragment) {
            visibility = View.VISIBLE;
            text = "Play back";
        } else if (activePage instanceof PlayBackFragment) {
            visibility = View.GONE;
        } else if (activePage instanceof VODFragment) {
            visibility = View.GONE;
        }
        btnRight.setText(text);
        btnRight.setVisibility(visibility);
    }

    @Override
    public boolean checkNetworkConnected() {
        if (!NetworkUtils.isConnected(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert");
            builder.setMessage("Bạn Chưa kết nói internet, Bấm OK để đến setting");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return false;
        }
        return true;
    }

    @Override
    public int getContentLayout() {
        return R.id.main_content;
    }

    private IMessageListener iMessageListener = new IMessageListener() {
        @Override
        public String getLsTag() {
            return "MainActivity";
        }

        @Override
        public void handleMessage(MessageClient messageClient) {
            Fragment fragment = getActivePage();
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).handleMessageClient(messageClient);
            }
        }
    };
    private Client mClient;

    @Override
    public void initClient() {
        closeClient();
        mClient = new Client(UserPref.getInstance().getServerAddress(), Config.SERVER_PORT);
        mClient.addIMessageListener(iMessageListener);
    }

    @Override
    public void closeClient() {
        if (isClientAlive()) {
            mClient.dispose();
            mClient = null;
        }
    }

    @Override
    public void sendLogin(String userName, String pass) {
        if (isClientAlive()) {
            MessageParser parser = new MessageParser();
            mClient.sendLoginMessage(parser.genMessageLogin(userName, pass));
        }
    }

    @Override
    public void sendCheckOnline(final ArrayList<String> strings) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isClientAlive()) {
                    MessageParser parser = new MessageParser();
                    mClient.sendCheckOnlineMessage(parser.genMessageCheckOnline(strings));
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public boolean isClientAlive() {
        return mClient != null;
    }

    @Override
    public void onNetworkConnection() {
        Log.d("HungHN", "Network connected");
    }

    @Override
    public void onNetworkDisConnection() {
        Log.d("HungHN", "Network dis connect");
        closeClient();
        if (!NetworkUtils.isConnected(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert");
            builder.setMessage("Bạn bị mất kết nối đến server, kiểm tra lại mang internet và bấm OK đăng nhập lại!");
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Fragment currentPage = getActivePage();
                    if (!(currentPage instanceof LoginClient)) {
                        LoginFragment loginFragment = new LoginFragment();
                        swapPage(loginFragment, TAG_LOGIN);
                    }
                }
            });
            builder.show();
        }
    }

    @Override
    public void setListCamera(ArrayList<Camera> cameras) {
        this.cameras = cameras;
    }

    @Override
    public ArrayList<Camera> getListCamera() {
        return this.cameras;
    }

    @Override
    public void setListDevice(ArrayList<Device> devices) {
        this.devices = devices;
    }

    @Override
    public ArrayList<Device> getListDevice() {
        return this.devices;
    }

    private void showDialogErrorCode(final int code) {
        if (code == -1) {
            return;
        }
        String title = "Error";
        String message = "";
        switch (code) {
            case Client.TIMEOUT_ERROR:
                message = "Không th kết nối được đến server";
                break;
            case Client.IO_ERROR:
                message = "Lỗi gửi nhận dữ liệu";
                break;
            case Client.UNKNOWN_HOST_ERROR:
                message = "Không có địa chỉ nào kết nối đến server bạn vừa nhập";
                break;
            case Client.UNKNOWN_ERROR:
            default:
                message = "unknowns error";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
