package com.hiepkhach9x.appcamera.util;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by hungnh on 2/24/17.
 */

public class Utils {

    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
