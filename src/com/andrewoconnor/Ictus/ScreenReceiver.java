package com.andrewoconnor.Ictus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 3/3/13
 * Time: 4:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            wasScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            wasScreenOn = true;
        }
    }

}