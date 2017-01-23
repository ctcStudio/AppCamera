package com.hiepkhach9x.appcamera.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.NetworkChangeReceiver;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.Camera;
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

    private Button btnLeft, btnRight;
    private TextView txtTitle;
    private ArrayList<Camera> cameras;

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
    }

    private void initActionBar() {
        // Inflate your custom layout
        final View actionBarLayout = getLayoutInflater().inflate(R.layout.layout_actionbar, null);
        btnLeft = (Button) actionBarLayout.findViewById(R.id.button_left);
        btnRight = (Button) actionBarLayout.findViewById(R.id.button_right);
        txtTitle = (TextView) actionBarLayout.findViewById(R.id.text_center);
        btnLeft.setOnClickListener(actionLeftClickListener);
        btnRight.setOnClickListener(actionRightClickListener);

        // Set up your ActionBar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout);
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
                HomeFragment homeFragment = HomeFragment.newInstance(getListCamera());
                swapPage(homeFragment,TAG_HOME);
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
        } else if (activePage instanceof HomeFragment) {
            visibility = View.VISIBLE;
            text = "PlayBack";
        } else if (activePage instanceof ListCameraFragment) {
            visibility = View.VISIBLE;
        } else if (activePage instanceof PlayBackFragment) {
            visibility = View.VISIBLE;
            text = "RealTime";
        } else if (activePage instanceof VODFragment) {
            visibility = View.VISIBLE;
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
                HomeFragment homeFragment = HomeFragment.newInstance(getListCamera());
                swapPage(homeFragment,TAG_HOME);
            } else if (activePage instanceof ListCameraFragment) {
                PlayBackFragment playBackFragment = PlayBackFragment.newInstance(getListCamera());
                swapPage(playBackFragment,TAG_PLAY_BACK);
            }
        }
    };

    @Override
    public void syncRightButton() {
        Fragment activePage = getActivePage();
        int visibility = View.VISIBLE;
        String text = "RealTime";
        if (activePage instanceof LoginFragment) {
            visibility = View.GONE;
        } else if (activePage instanceof HomeFragment) {
            visibility = View.VISIBLE;
        } else if (activePage instanceof ListCameraFragment) {
            visibility = View.VISIBLE;
            text = "PlayBack";
        } else if (activePage instanceof PlayBackFragment) {
            visibility = View.GONE;
        } else if (activePage instanceof VODFragment) {
            visibility = View.VISIBLE;
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
    public void sendCheckOnline(ArrayList<String> strings) {
        if (isClientAlive()) {
            MessageParser parser = new MessageParser();
            mClient.sendCheckOnlineMessage(parser.genMessageCheckOnline(strings));
        }
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
                    if (currentPage instanceof LoginClient) {
                        LoginFragment fragment = (LoginFragment) currentPage;
                        fragment.switchToLogin();
                    } else {
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
}
