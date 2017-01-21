package com.hiepkhach9x.appcamera.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.hiepkhach9x.appcamera.Config;
import com.hiepkhach9x.appcamera.NetworkChangeReceiver;
import com.hiepkhach9x.appcamera.R;
import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment fragment = getActivePage();
        if (fragment == null) {
            LoginFragment loginFragment = new LoginFragment();
            swapPage(loginFragment, TAG_LOGIN);
        }

        NetworkChangeReceiver.setConnectivityReceiverListener(this);
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
}
