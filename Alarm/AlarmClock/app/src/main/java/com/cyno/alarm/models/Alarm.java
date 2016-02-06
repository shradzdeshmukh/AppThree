package com.cyno.alarm.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.cyno.alarm.alarm_logic.AlarmService;
import com.cyno.alarm.database.AlarmTable;
import com.cyno.alarmclock.R;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by hp on 12-01-2016.
 */
public class Alarm {

    private static final String KEY_SNOOZE_DURATION = "snooze_duration";
    private static final long TIME_DURATION_TEN_MIN = 1000*60*10;
    private int id;
    private long time;
    private boolean isVibrate;
    private String ringtone;
    private boolean isActive;
    private boolean isRepeat;
    private SortedSet<Long> repeatDays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    public void setIsVibrate(boolean isVibrate) {
        this.isVibrate = isVibrate;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public SortedSet<Long> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(SortedSet<Long> repeatDays) {
        this.repeatDays = repeatDays;

    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public static void storeLocally(Alarm alarm , Context context){
        Log.d("alarm","store locally");

        alarm.setRepeatDays(refreshRepeatDays(alarm.getRepeatDays()));

        long time = alarm.getTime();
        if(time < System.currentTimeMillis()){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            alarm.setTime(cal.getTimeInMillis());
        }



        ContentValues values = new ContentValues();
        values.put(AlarmTable.COL_ALARM_TIME , alarm.getTime());
        values.put(AlarmTable.COL_ALARM_RINGTONE, alarm.getRingtone());
        values.put(AlarmTable.COL_ALARM_IS_REPEAT, alarm.isRepeat());
        if(alarm.getRepeatDays() != null)
            values.put(AlarmTable.COL_ALARM_REPEAT , alarm.getRepeatDays().toString().replace("[","").replace("]", ""));
        values.put(AlarmTable.COL_ALARM_VIBRATE, alarm.isVibrate());
        values.put(AlarmTable.COL_ALARM_IS_ACTIVE, alarm.isActive());
        int count = context.getContentResolver().update(AlarmTable.CONTENT_URI, values,
                AlarmTable.COL_ID + " = ?", new String[]{String.valueOf(alarm.getId())});

        if(count == 0)
            context.getContentResolver().insert(AlarmTable.CONTENT_URI , values);

    }

    public static SortedSet<Long> refreshRepeatDays(SortedSet<Long>  repeatList) {
        long time = -1;
        for(int index = 0 ; index < repeatList.size() ; ++index) {
            Long[] array = (Long[]) repeatList.toArray(new Long[repeatList.size()]);
            List<Long> daysList = Arrays.asList(array);
            time = daysList.get(index);
            if ((time / 1000) <= (System.currentTimeMillis() / 1000)) {
                repeatList.remove(time);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time);
                cal.add(Calendar.WEEK_OF_MONTH, 1);
                repeatList.add(cal.getTimeInMillis());
            }
        }
        return repeatList;
    }

    public static  Alarm getAlarm(int id , Context context) {
        Cursor mCursor = context.getContentResolver().query(AlarmTable.CONTENT_URI, null,
                AlarmTable.COL_ID + " = ? ", new String[]{String.valueOf(id)}, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            Alarm mAlarm = new Alarm();
            if (mCursor.moveToNext()) {
                mAlarm = getAlarmFromCursor(mCursor);
            }
            mCursor.close();
            return mAlarm;

        }
        return null;
    }

    public static Alarm getAlarmFromCursor(Cursor mCursor) {
        Alarm mAlarm = new Alarm();
        mAlarm.setId(mCursor.getInt(mCursor.getColumnIndex(AlarmTable.COL_ID)));
        mAlarm.setTime(mCursor.getLong(mCursor.getColumnIndex(AlarmTable.COL_ALARM_TIME)));
        mAlarm.setIsActive(mCursor.getString(mCursor.getColumnIndex(AlarmTable.COL_ALARM_IS_ACTIVE)).equalsIgnoreCase("1"));
        mAlarm.setIsVibrate(mCursor.getString(mCursor.getColumnIndex(AlarmTable.COL_ALARM_VIBRATE)).equalsIgnoreCase("1"));
        mAlarm.setRingtone(mCursor.getString(mCursor.getColumnIndex(AlarmTable.COL_ALARM_RINGTONE)));
        mAlarm.setIsRepeat(mCursor.getString(mCursor.getColumnIndex(AlarmTable.COL_ALARM_IS_REPEAT)).equalsIgnoreCase("1"));
        String repeat = mCursor.getString(mCursor.getColumnIndex(AlarmTable.COL_ALARM_REPEAT));
        if(repeat != null) {
            String[] arr = repeat.split(",");
            SortedSet<Long> list = new TreeSet<>();
            for (String str : arr) {
                if(!TextUtils.isEmpty(str))
                    list.add(Long.valueOf(str.trim()));
            }
            mAlarm.setRepeatDays(list);
        }
        Log.d("repeat" , mCursor.getString(mCursor.getColumnIndex(AlarmTable.COL_ALARM_IS_ACTIVE)));
        return mAlarm;
    }

    public static String getRepeatDaysText(SortedSet<Long> repeatList , Context context , boolean isRepeat){
        if(repeatList.size() == 7){
            return context.getString(R.string.everyday);
        }else if(!isRepeat){
            long date = repeatList.first();
//            return  DateUtils.formatElapsedTime(date).toString();
            return  DateUtils.getRelativeTimeSpanString(date).toString();
        }
        Long[] array = (Long[]) repeatList.toArray(new Long[repeatList.size()]);
        List<Long> daysList = Arrays.asList(array);
        StringBuilder mBuilder = new StringBuilder();
        for(int index = 0 ; index < daysList.size() ; ++index){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(daysList.get(index));
            switch (cal.get(Calendar.DAY_OF_WEEK)){
                case Calendar.SUNDAY:
                    mBuilder.append("Sun");
                    break;
                case Calendar.MONDAY:
                    mBuilder.append("Mon");

                    break;
                case Calendar.TUESDAY:
                    mBuilder.append("Tue");

                    break;
                case Calendar.WEDNESDAY:
                    mBuilder.append("Wed");

                    break;
                case Calendar.THURSDAY:
                    mBuilder.append("Thurs");

                    break;
                case Calendar.FRIDAY:
                    mBuilder.append("Fri");

                    break;
                case Calendar.SATURDAY:
                    mBuilder.append("Sat");
                    break;

            }
            mBuilder.append(" ");
        }

        return mBuilder.toString();
    }

    public static void DeleteAlarm(int id , Context context){
        Log.d("alarm", "delete alarm");

        context.getContentResolver().delete(AlarmTable.CONTENT_URI, AlarmTable.COL_ID + " = ? ",
                new String[]{String.valueOf(id)});

        Intent service = new Intent(context, AlarmService.class);
        service.setAction(AlarmService.ACTION_STOP_ALARM);
        context.startService(service);

    }

    public static long getTimeOfDay(int day , long originalTime){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(originalTime);
        cal.set(Calendar.DAY_OF_WEEK, day);
        if(cal.getTimeInMillis() < System.currentTimeMillis())
            cal.add(Calendar.WEEK_OF_MONTH , 1);
        Log.d("time ", "time = " + cal.getTimeInMillis());
        return cal.getTimeInMillis();
    }

    public static boolean isDayActive(int day , Alarm alarm){

        Calendar cal = Calendar.getInstance();
        for(Long time : alarm.getRepeatDays()){
            cal.setTimeInMillis(time);
            if(cal.get(Calendar.DAY_OF_WEEK) == day)
                return true;
        }
        return false;
    }

    public static Alarm getNextAlarmTime(Context context){
        Alarm alarm = null;
        Cursor cursor = context.getContentResolver().query(AlarmTable.CONTENT_URI , null , null , null  , AlarmTable.COL_ALARM_TIME);
        int nextAlarmId = -1;
        long time = -1;
        if(cursor != null){
            while(cursor.moveToNext()){
                String[] repDays = cursor.getString(cursor.getColumnIndex(AlarmTable.COL_ALARM_REPEAT)).split(",");
                long temp = Long.valueOf(repDays[0]);
                if(time == -1) {
                    time = temp;
                    nextAlarmId = cursor.getInt(cursor.getColumnIndex(AlarmTable.COL_ID));
                } else if(time > temp) {
                    time = temp;
                    nextAlarmId = cursor.getInt(cursor.getColumnIndex(AlarmTable.COL_ID));
                }
            }
            cursor.close();
        }
        alarm =   getAlarm(nextAlarmId , context);
        return alarm;
    }

    public String getDisplayRingtone(Context context) {
        if (ringtone != null){
            Uri uri = Uri.parse(ringtone);
            Ringtone tone = RingtoneManager.getRingtone(context, uri);
            return tone.getTitle(context);

        }
        return context.getString(android.R.string.unknownName);
    }

    public static void startAlarmService(Context context){
        Intent service = new Intent(context, AlarmService.class);
        context.startService(service);
    }

}
