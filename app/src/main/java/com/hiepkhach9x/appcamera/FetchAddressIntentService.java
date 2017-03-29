package com.hiepkhach9x.appcamera;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by hungh on 3/28/2017.
 */
public class FetchAddressIntentService extends IntentService {
    private static final String TAG = "FetchAddressService";

    protected ResultReceiver mReceiver;
    private String cameraId;

    public static void startIntentService(Context context,
                                          String cameraId,
                                          double lat,
                                          double log,
                                          ResultReceiver  mResultReceiver) {
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.RESULT_CAMERA_KEY, cameraId);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA_LAT, lat);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA_LOG, log);
        context.startService(intent);
    }

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";

        // Get the location passed to this service through an extra.
        double lat = intent.getDoubleExtra(
                Constants.LOCATION_DATA_EXTRA_LAT,0);
        double log = intent.getDoubleExtra(
                Constants.LOCATION_DATA_EXTRA_LOG,0);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        cameraId = intent.getStringExtra(Constants.RESULT_CAMERA_KEY);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    lat,
                    log,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service_not_available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid_lat_long_used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + lat +
                    ", Longitude = " +
                    log, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no_address_found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.d(TAG, "address_found: " + addressFragments);
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(", ",
                            addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        bundle.putString(Constants.RESULT_CAMERA_KEY, cameraId);
        mReceiver.send(resultCode, bundle);
    }
}
