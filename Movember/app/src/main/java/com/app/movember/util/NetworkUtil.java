package com.app.movember.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    public static boolean areWeConnectedTonetwork(Context aContext) {
        NetworkInfo networkInfo = ((ConnectivityManager) aContext.getSystemService(Context
                .CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
