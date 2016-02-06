package com.cyno.alarm.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cyno.alarm.adapters.AlarmListAdapter;
import com.cyno.alarm.alarm_logic.AlarmReceiver;
import com.cyno.alarm.database.AlarmTable;
import com.cyno.alarm.models.CustomRingtone;
import com.cyno.alarmclock.R;
import com.cyno.alarm.models.Alarm;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;

public class AddAlarmActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, TimePickerDialog.OnTimeSetListener {

    private static final int LOADER_ID = 100;
    private static final int ACTION_CHOOSE_RINGTONE = 111;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 222;


    private RecyclerView mRecyclerView;
    private AlarmListAdapter adapter;
    private FloatingActionButton fabAddAlarm;
    private Alarm updateAlarm;
    private static MediaPlayer mediaPlayer ;
    private View mEmptyView;
    private boolean alreadSet;
    private boolean backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

//        testNextAlarmTime();

        setContentView(R.layout.activity_add_alarm);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_alarms);
        mEmptyView = findViewById(R.id.add_alarm_empty_view);
        fabAddAlarm = (FloatingActionButton) findViewById(R.id.fab_add_alarm);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new AlarmListAdapter(this, null, this);
        mRecyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

//        Alarm alarm = new Alarm();
//        alarm.setTime(System.currentTimeMillis());
//        alarm.setRingtone("Ringtone");
//        alarm.setIsActive(true);
//        alarm.setIsVibrate(false);
//        Boolean arr[] = new Boolean[]{true,false,false,false,true,false,true};
//        alarm.setRepeatDays(Arrays.<Boolean>asList(arr));
//        Alarm.storeLocally(alarm, this);

        fabAddAlarm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_alarm:
                updateAlarm = null;
                addNewAlarm();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 20);
                cal.set(Calendar.HOUR_OF_DAY, 5);
                Alarm.getTimeOfDay(Calendar.SATURDAY, cal.getTimeInMillis());
                break;
            case R.id.tv_alarms_list_item_time:
                this.updateAlarm = (Alarm) v.getTag();
                addNewAlarm();
                break;

            case R.id.tv_alarms_list_item_ringtone:
                showRingtoneChooser();

                this.updateAlarm = (Alarm) v.getTag();

                break;
        }
    }


    private void addNewAlarm() {
        alreadSet = false;
        Calendar currCal = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, currCal.get(Calendar.HOUR_OF_DAY),
                currCal.get(Calendar.MINUTE), PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.PREF_IS_24HOUR , true));
        timePickerDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, AlarmTable.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adapter.setDatasetObserver(data);
        if(adapter.getCursor().getCount() == 0){
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }else{
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        boolean isRepeat = false;
        if(alreadSet || backPressed) {
            backPressed = false;
            return;
        }
        alreadSet = true;
        Alarm alarm = null;
        if (updateAlarm != null) {
            alarm = updateAlarm;
            isRepeat = true;
        } else
            alarm = new Alarm();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_MONTH, 1);
        alarm.setTime(cal.getTimeInMillis());
        SortedSet<Long> list = new TreeSet<>();
        list.add(cal.getTimeInMillis());
        alarm.setRepeatDays(list);
        alarm.setIsActive(true);
        if(!isRepeat) {
            if (alarm.getRingtone() != null)
                alarm.setRingtone(alarm.getRingtone());
            else {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
                if (uri != null)
                    alarm.setRingtone(uri.toString());
            }
        }
        Alarm.storeLocally(alarm, this);
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        Alarm.startAlarmService(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == ACTION_CHOOSE_RINGTONE) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone tone = RingtoneManager.getRingtone(this, uri);
            if (uri != null) {
                Log.d("ringtone", "ringtone = " + uri.toString());
                Log.d("ringtone", "ringtone = " + tone.getTitle(this));
                updateAlarm.setRingtone(uri.toString());
                Alarm.storeLocally(updateAlarm, this);
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            }
        }
    }

    private void showRingtoneChooser() {
        if (checkMediaPermision()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Ringtone from");
            String[] arrChooser = new String[]{"System Tones", "From SD Card"};
            builder.setSingleChoiceItems(arrChooser, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                            startActivityForResult(intent, ACTION_CHOOSE_RINGTONE);
                            break;
                        case 1:
                            showUserRingtones();
                            break;
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private void showUserRingtones() {

        Cursor cur = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cur != null) {
            final CustomRingtone[] arrCustomRingtone = new CustomRingtone[cur.getCount()];
            String[] arrCustomRingtoneStrings = new String[cur.getCount()];
            while (cur.moveToNext()) {
                arrCustomRingtone[cur.getPosition()] = new CustomRingtone(cur.getString
                        (cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID)));
                arrCustomRingtoneStrings[cur.getPosition()] = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Tone");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Alarm.storeLocally(updateAlarm, AddAlarmActivity.this);
                    getSupportLoaderManager().restartLoader(LOADER_ID, null, AddAlarmActivity.this);

                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setSingleChoiceItems(arrCustomRingtoneStrings, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item != 0) {
                        Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + arrCustomRingtone[item].getId());
                        playMediaSound(uri);
                        updateAlarm.setRingtone(uri.toString());
                    } else {
                        updateAlarm.setRingtone(null);
                    }
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                }
            });
            builder.show();
        }
    }

    private void playMediaSound(Uri uri) {
        Ringtone tone = RingtoneManager.getRingtone(this, uri);
        Log.d("tone", "tone = " + tone.getTitle(this));
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "error ", Toast.LENGTH_LONG).show();
        }
        mediaPlayer.start();
    }


    public boolean checkMediaPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);

                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showRingtoneChooser();
                } else {
                    Toast.makeText(this, getString(R.string.no_read_permission), Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onBackPressed() {
        backPressed = true;
        super.onBackPressed();
    }

    private void testNextAlarmTime(){
        try {
            Alarm alarm = Alarm.getNextAlarmTime(this);
            long time = alarm.getRepeatDays().first();
            SimpleDateFormat format  = new SimpleDateFormat("dd/MM/yy , hh:mm a");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            Toast.makeText(this , format.format(cal.getTime()) , Toast.LENGTH_LONG).show();
        }catch (Exception ex){
        }
    }
}


