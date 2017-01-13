package com.hiepkhach9x.appcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hiepkhach9x.appcamera.util.NetworkUtils;

/**
 * Created by hungh on 1/13/2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static ConnectivityReceiverListener connectivityReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (connectivityReceiverListener != null) {
            if (NetworkUtils.isConnected(context))
                connectivityReceiverListener.onNetworkConnection();
            else
                connectivityReceiverListener.onNetworkDisConnection();
        }
    }

    public static void setConnectivityReceiverListener(ConnectivityReceiverListener connectivityReceiverListener) {
        NetworkChangeReceiver.connectivityReceiverListener = connectivityReceiverListener;
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnection();

        void onNetworkDisConnection();
    }
}
