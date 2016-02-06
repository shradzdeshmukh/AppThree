package com.cyno.alarm.alarm_logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by hp on 15-01-2016.
 */
public class AlarmSetterOnBoot extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("alarm","alarm setter");

        Intent service = new Intent(context, AlarmService.class);
        context.startService(service);

    }
}
