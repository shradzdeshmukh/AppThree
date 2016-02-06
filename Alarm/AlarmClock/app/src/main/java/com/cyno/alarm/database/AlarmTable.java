package com.cyno.alarm.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class AlarmTable
{
	public static final String ALARM_TABLE = "Tasks";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AlarmContentProvider.AUTHORITY
			+ "/" + ALARM_TABLE);

	public static final String COL_ID = "_id";
	public static final String COL_ALARM_TIME = "time";
	public static final String COL_ALARM_REPEAT = "repeat";
	public static final String COL_ALARM_RINGTONE = "ringtone";
	public static final String COL_ALARM_VIBRATE = "vibrate";
	public static final String COL_ALARM_IS_ACTIVE = "is_active";
	public static final String COL_ALARM_IS_REPEAT = "is_repeat";

	private static final String DATABASE_CREATE_NEW = "create table "
			+ ALARM_TABLE
			+ "("
			+ COL_ID + " integer primary key autoincrement, "
			+ COL_ALARM_TIME + " TEXT , "
			+ COL_ALARM_REPEAT+ " TEXT , "
			+ COL_ALARM_RINGTONE+ " TEXT , "
			+ COL_ALARM_VIBRATE+ " TEXT , "
			+ COL_ALARM_IS_REPEAT+ " TEXT , "
			+ COL_ALARM_IS_ACTIVE + " TEXT  "
			+ ");";


	public static void onCreate(SQLiteDatabase mDatabase)
	{
		mDatabase.execSQL(DATABASE_CREATE_NEW);
	}
	public static void onUpdate(SQLiteDatabase mDatabase , int oldVer, int newVer){
	}


}
