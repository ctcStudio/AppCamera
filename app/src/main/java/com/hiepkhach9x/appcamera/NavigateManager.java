package com.hiepkhach9x.appcamera;

import android.support.v4.app.Fragment;

/**
 * Created by hungh on 1/4/2017.
 */

public interface NavigateManager {
    int getContentLayout();
    Fragment getActivePage();
    void addPage(Fragment fragment,String tag);
    void swapPage(Fragment fragment,String tag);
    void syncTitle();
}
