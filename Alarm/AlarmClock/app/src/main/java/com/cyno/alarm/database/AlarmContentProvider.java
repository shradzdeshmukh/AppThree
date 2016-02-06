package com.cyno.alarm.database;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class AlarmContentProvider extends ContentProvider{

	public static final String AUTHORITY = "com.cyno.alarm";
	private static final String DATABASE_NAME = "alarms_db";
	private static final int DATABASE_VERSION = 2;


	private TasksDbHelper mDatabase;

	private static final int ALL_ALARMS = 1;
	private static final int SINGLE_ALARM = 2;

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/alarms";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/alarm";

	private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static
	{
		mURIMatcher.addURI(AUTHORITY, AlarmTable.ALARM_TABLE, ALL_ALARMS);
		mURIMatcher.addURI(AUTHORITY, AlarmTable.ALARM_TABLE +"/#", SINGLE_ALARM);
	}



	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {


		String sTable = "";
		String sColumns = null;
		switch ( mURIMatcher.match(uri)) {
			case ALL_ALARMS:
				sTable = AlarmTable.ALARM_TABLE;
				break;
			case SINGLE_ALARM:
				sTable = AlarmTable.ALARM_TABLE;
				sColumns = AlarmTable.COL_ID;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}if (sColumns != null) {
			selection = sColumns + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		}

		try {
			SQLiteDatabase db = mDatabase.getWritableDatabase();
			int count = db.delete(sTable, selection, selectionArgs);
			if(getContext() != null)
				getContext().getContentResolver().notifyChange(uri, null);
			return count;
		} catch (SQLException e) {
		}
		return -1 ;
	}


	@Override
	public String getType(Uri uri) {
		switch(mURIMatcher.match(uri)){
			case ALL_ALARMS:
				return CONTENT_TYPE;
			case SINGLE_ALARM:
				return CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown Uri "+uri);
		}
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {

		String sTable = "";
		String sColumn = null;
		Uri mContentUri = null;

		switch(mURIMatcher.match(uri)){
			case ALL_ALARMS:
				sTable = AlarmTable.ALARM_TABLE;
				mContentUri = AlarmTable.CONTENT_URI;
				break;
			case SINGLE_ALARM:
				sTable =  AlarmTable.ALARM_TABLE;
				sColumn = AlarmTable.COL_ID;
				mContentUri = AlarmTable.CONTENT_URI;
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri "+uri);
		}


		long rowid;
		try {

			SQLiteDatabase db = mDatabase.getWritableDatabase();
			if (values == null) {
				values = new ContentValues();
			}

			rowid = db.insert(sTable, sColumn, values);
			if (rowid > 0){
				Uri oUri = ContentUris.withAppendedId(mContentUri, rowid);
				if(getContext() != null)
					getContext().getContentResolver().notifyChange(oUri, null);
				return oUri;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		throw new SQLException("Failed to insert row into " + uri);

	}

	@Override
	public boolean onCreate() {
		mDatabase = new TasksDbHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		String sTable = "";
		String sColumn = null;
		String sSort = "";
		switch(mURIMatcher.match(uri)){
			case ALL_ALARMS:
				sTable = AlarmTable.ALARM_TABLE;
				sSort = AlarmTable.COL_ID;
				break;
			case SINGLE_ALARM:
				sTable = AlarmTable.ALARM_TABLE;
				sColumn = AlarmTable.COL_ID;
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri "+uri);
		}

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(sTable);
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = sSort;
		} else {
			orderBy = sortOrder;
		}
		if (sColumn != null) {
			selection = sColumn + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		}

		SQLiteDatabase db;
		try {
			db = mDatabase.getReadableDatabase();
			Cursor cursor = builder.query(db, projection, selection,
					selectionArgs, null, null, orderBy);

			if (cursor != null) {
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
			}

			return cursor;
		} catch (SQLException e) {
		}
		return null ;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		String sTable = "";
		String sColumn = null;
		Uri mContentUri = null;
		switch(mURIMatcher.match(uri)){
			case ALL_ALARMS:
				sTable = AlarmTable.ALARM_TABLE;
				mContentUri  = AlarmTable.CONTENT_URI;
				break;
			case SINGLE_ALARM:
				sTable =  AlarmTable.ALARM_TABLE;
				sColumn = AlarmTable.COL_ID;
				mContentUri = AlarmTable.CONTENT_URI;
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri "+uri);
		}
		try {
			if (sColumn != null) {
				selection = sColumn	+ "=" + uri.getPathSegments().get(1)
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection	+ ')' : "");
			}


			SQLiteDatabase db = mDatabase.getWritableDatabase();
			int count = db.update(sTable, values, selection, selectionArgs);

			if(getContext() != null)
				getContext().getContentResolver().notifyChange(uri, null);
			return count;
		} catch (SQLException e) {
		}
		return 0 ;
	}


	private static class TasksDbHelper extends SQLiteOpenHelper{

		public TasksDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			AlarmTable.onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			AlarmTable.onUpdate(db, oldVersion, newVersion);
		}
	}
}
