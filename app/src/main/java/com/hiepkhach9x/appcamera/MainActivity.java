package com.hiepkhach9x.appcamera;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements NavigateManager {
    public static final String TAG_LOGIN = "LOGIN-FRAGMENT";

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
        transaction.add(getContentLayout(), fragment, tag);
        transaction.commit();
    }

    @Override
    public void swapPage(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getContentLayout(), fragment, tag);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void syncTitle() {

    }

    @Override
    public int getContentLayout() {
        return R.id.main_content;
    }
}
