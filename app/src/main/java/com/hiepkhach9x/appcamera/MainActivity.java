package com.hiepkhach9x.appcamera;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hiepkhach9x.appcamera.connection.Client;
import com.hiepkhach9x.appcamera.connection.MessageParser;
import com.hiepkhach9x.appcamera.connection.listener.IMessageListener;
import com.hiepkhach9x.appcamera.entities.MessageClient;
import com.hiepkhach9x.appcamera.preference.UserPref;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigateManager, LoginClient {
    public static final String TAG_LOGIN = "LOGIN-FRAGMENT";
    public static final String TAG_HOME = "HOME-FRAGMENT";
    public static final String TAG_CAMERA = "CAMERA-FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment fragment = getActivePage();
        if (fragment == null) {
            LoginFragment loginFragment = new LoginFragment();
            swapPage(loginFragment, TAG_LOGIN);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getContentLayout(), fragment, tag);
        transaction.commit();
    }

    @Override
    public void syncTitle() {

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
        mClient = new Client(UserPref.getInstance().getServerAddress());
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
}
