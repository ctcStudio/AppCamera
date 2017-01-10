package com.hiepkhach9x.appcamera;

import java.util.ArrayList;

/**
 * Created by hungh on 1/10/2017.
 */

public interface LoginClient {

    void initClient();

    void sendLogin(String userName,String pass);

    void sendCheckOnline(ArrayList<String> strings);

    void closeClient();

    boolean isClientAlive();
}
