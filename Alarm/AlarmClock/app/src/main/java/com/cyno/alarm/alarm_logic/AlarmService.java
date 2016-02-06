package com.cyno.alarm.alarm_logic;


import com.cyno.alarm.models.Alarm;
import com.cyno.alarm.ui.SettingsActivity;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class AlarmService extends IntentService {


    public static final String KEY_SNOOZE_DURATION = "snoozeDuration";
    public static final String ACTION_SNOOZE_ALARM = "action_snooze";
        private static final int TIME_DURATION_TEN_MIN = 10 ;
//    private static final int TIME_DURATION_TEN_MIN = 1;
    public static final String KEY_ALARM_ID = "ID";
    public static final String ACTION_STOP_ALARM = "STOP";
    private static final int REQ_CODE_ALARM = 111;

    public AlarmService() {
        super(AlarmService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_SNOOZE_ALARM))
            snoozeAlarm(intent.getIntExtra(KEY_ALARM_ID , -1));
        else if(intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_STOP_ALARM))
            stopAlarm();
        else
            setAlarm();
    }

    private void setAlarm() {
        Log.d("alarm", "set new alarm");

        Alarm mAlarm = Alarm.getNextAlarmTime(this);
        if (mAlarm != null) {
            Intent mIntent = new Intent(this, AlarmReceiver.class);
            mIntent.putExtra(AlarmReceiver.ALARM_ID, mAlarm.getId());
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, mIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if(Build.VERSION.SDK_INT >= 23)
                mManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mAlarm.getRepeatDays().first(), mPendingIntent);
            else
                mManager.set(AlarmManager.RTC_WAKEUP, mAlarm.getRepeatDays().first(), mPendingIntent);
        }
    }

    private void snoozeAlarm(int alarmId) {
        Alarm mAlarm = Alarm.getAlarm(alarmId , this);
        long lastTime = System.currentTimeMillis();
        lastTime += 1000*60* PreferenceManager.getDefaultSharedPreferences(this).
                getInt(SettingsActivity.PREF_SNOOZE_INTERVAL, TIME_DURATION_TEN_MIN);

        Intent mIntent = new Intent(this,AlarmReceiver.class);
        mIntent.putExtra(AlarmReceiver.ALARM_ID , mAlarm.getId());
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, REQ_CODE_ALARM ,
                mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= 23)
            mManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lastTime, mPendingIntent);
        else
            mManager.set(AlarmManager.RTC_WAKEUP, lastTime, mPendingIntent);
    }

    private void stopAlarm(){
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQ_CODE_ALARM , intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        setAlarm();
    }
}
