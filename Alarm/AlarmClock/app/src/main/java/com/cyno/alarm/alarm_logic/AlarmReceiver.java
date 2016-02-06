package com.cyno.alarm.alarm_logic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.cyno.alarm.models.Alarm;
import com.cyno.alarm.ui.MainActivity;
import com.cyno.alarmclock.R;

/**
 * Created by hp on 15-01-2016.
 */
public class AlarmReceiver extends BroadcastReceiver{
    public static final String ALARM_ID = "idd";

    @Override
    public void onReceive(Context context, Intent intent) {

        WakeLocker.acquire(context);

//        String time = intent.getStringExtra("time");
//        Log.d("alarm", "ringinggg " + time);
//
//        Notification noti = new NotificationCompat.Builder(context)
//                .setContentTitle("Alarm")
//                .setContentText("Alarm")
//                .setSmallIcon(R.drawable.ic_ringtone)
//                .setAutoCancel(true)
//                .build();
//
//        noti.defaults |= Notification.DEFAULT_SOUND;
//
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(1, noti);

        int alarmId = intent.getIntExtra(ALARM_ID, -1);
        Alarm mAlarm = Alarm.getAlarm(alarmId, context);
        if(mAlarm == null) {
            WakeLocker.release();
            return;
        }

        if (mAlarm.isActive()) {

            Intent mIntent = new Intent(context, MainActivity.class);
            mIntent.setAction(MainActivity.ACTION_RING_ALARM);
            mIntent.putExtra(MainActivity.KEY_ALARM_ID, mAlarm.getId());
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);

        }else{
            WakeLocker.release();
            mAlarm.setRepeatDays(Alarm.refreshRepeatDays(mAlarm.getRepeatDays()));
            mAlarm.setTime(mAlarm.getRepeatDays().first());
            Alarm.storeLocally(mAlarm , context);
//            Intent service = new Intent(context, AlarmService.class);
//            context.startService(service);
        }
    }
}
