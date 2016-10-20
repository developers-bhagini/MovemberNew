package com.app.movember.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by tnahakax on 10/7/2016.
 */

public class Util {

    public static boolean packageInstalled(Context aContext, String aPackageName) {
        try {
            PackageInfo info = aContext.getPackageManager().getPackageInfo(aPackageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
