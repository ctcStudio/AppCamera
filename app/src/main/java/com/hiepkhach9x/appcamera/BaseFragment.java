package com.hiepkhach9x.appcamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiepkhach9x.appcamera.entities.MessageClient;

/**
 * Created by hungh on 1/4/2017.
 */

public abstract class BaseFragment extends Fragment {

    protected NavigateManager mNavigateManager;
    protected LoginClient mLoginClient;
    private ProgressDialog mDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigateManager) {
            mNavigateManager = (NavigateManager) context;
        }

        if (context instanceof LoginClient) {
            mLoginClient = (LoginClient) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    public abstract int getLayoutId();

    public abstract void handleMessageClient(MessageClient messageClient);

    public void showDialog() {
        if (!isAdded()) {
            return;
        }
        if (mDialog == null) {
            mDialog = new ProgressDialog(getContext());
            mDialog.setMessage("Loading...");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();
        } else if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void dismissDialog() {
        if (!isAdded()) {
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
