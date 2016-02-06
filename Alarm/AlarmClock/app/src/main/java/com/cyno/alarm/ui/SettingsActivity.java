package com.cyno.alarm.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cyno.alarm.color_picker.ColorPickerClickListener;
import com.cyno.alarm.color_picker.ColorPickerDialogBuilder;
import com.cyno.alarm.color_picker.ColorPickerView;
import com.cyno.alarm.color_picker.OnColorSelectedListener;
import com.cyno.alarmclock.R;

/**
 * Created by hp on 24-01-2016.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PREF_CLOCK_BACKGROUND_COLOR = "clock_background";
    public static final String PREF_CLOCK_DIGIT_COLOR = "clock_digits";
    public static final String PREF_IS_24HOUR = "am_pm";
    public static final String PREF_KEEP_SCREEN_ON = "screenon";
    public static final String PREF_SNOOZE_INTERVAL = "snooze_int";
    private static final String FEEDBACK_EMAIL_ID = "developer.shraddha@gmail.com";
    private static final String FEEDBACK_SUBJECT = "Feedback on your app Alarm";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        View vThemeBackground = findViewById(R.id.settings_theme_background);
        View vThemeDigits = findViewById(R.id.settings_theme_digits);
        vThemeBackground.setOnClickListener(this);
        vThemeDigits.setOnClickListener(this);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        setDigitsColor(pref.getInt(PREF_CLOCK_DIGIT_COLOR , Color.WHITE));
        setBackroundColor(pref.getInt(PREF_CLOCK_BACKGROUND_COLOR, Color.BLUE));
        setAmPm(pref.getBoolean(PREF_IS_24HOUR, true));
        setScreenOn(pref.getBoolean(PREF_KEEP_SCREEN_ON, true));
        setSnoozeInterval(pref.getInt(PREF_SNOOZE_INTERVAL, 10));
        setFeedback();


    }

    private void setFeedback() {
        TextView tvFeedback = (TextView) findViewById(R.id.settings_tv_feedback);
        tvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{FEEDBACK_EMAIL_ID});
                i.putExtra(Intent.EXTRA_SUBJECT, FEEDBACK_SUBJECT);
                String deviceName = android.os.Build.MODEL;
                String manufacturer = android.os.Build.MANUFACTURER + " ver " +Build.VERSION.SDK_INT ;
                String display = getResources().getDisplayMetrics().densityDpi +"dpi " +
                        getResources().getDisplayMetrics().heightPixels+" x "+
                        getResources().getDisplayMetrics().widthPixels;
                i.putExtra(Intent.EXTRA_TEXT   , deviceName + "\n" + manufacturer+ "\n" + display +"\n\n");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.no_email_client), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settings_theme_background:
                prepareColorPickerDialog(true);
                break;
            case R.id.settings_theme_digits:
                prepareColorPickerDialog(false);
                break;
        }
    }

    private void prepareColorPickerDialog(final boolean isBackground) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int defCOlor = -1;
        if(isBackground)
            defCOlor = pref.getInt(PREF_CLOCK_BACKGROUND_COLOR , Color.BLUE);
        else
            defCOlor = pref.getInt(PREF_CLOCK_DIGIT_COLOR, Color.WHITE);

        ColorPickerDialogBuilder
                .with(this)
                .lightnessSliderOnly()
                .setTitle(getString(R.string.choose_color))
                .initialColor(defCOlor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                    }
                })
                .setPositiveButton(getString(android.R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit();
                        if (isBackground) {
                            editor.putInt(PREF_CLOCK_BACKGROUND_COLOR, selectedColor);
                            setBackroundColor(selectedColor);

                        } else {
                            editor.putInt(PREF_CLOCK_DIGIT_COLOR, selectedColor);
                            setDigitsColor(selectedColor);
                        }
                        editor.commit();

                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    private void setDigitsColor(int color){
        View view = findViewById(R.id.settings_digit_color);
        view.setBackgroundColor(color);
    }
    private void setBackroundColor(int color){
        View view = findViewById(R.id.settings_background_color);
        view.setBackgroundColor(color);
    }

    private void setAmPm(boolean aBoolean) {
        final CheckBox cb = (CheckBox) findViewById(R.id.settings_am_pm);
        cb.setChecked(aBoolean);

        View parent = (View) cb.getParent();
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.setChecked(!cb.isChecked());
            }
        });
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit();
                editor.putBoolean(PREF_IS_24HOUR, isChecked);
                editor.commit();
            }
        });

    }

    private void setScreenOn(boolean aBoolean){
        final CheckBox cb = (CheckBox) findViewById(R.id.settings_screen_on);
        cb.setChecked(aBoolean);

        View parent = (View) cb.getParent();
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.setChecked(!cb.isChecked());
            }
        });
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit();
                editor.putBoolean(PREF_KEEP_SCREEN_ON, isChecked);
                editor.commit();
            }
        });
    }

    private void setSnoozeInterval(int interval){
        final TextView tvSnooze = (TextView) findViewById(R.id.settings_tv_snooze_interval);
        tvSnooze.setText(interval +" "+ getString(R.string.minutes));
        ((View)tvSnooze.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String []arr = new String[]{"5 "+ getString(R.string.minutes) ,
                        "10 " + getString(R.string.minutes),
                        "15 " + getString(R.string.minutes),
                        "20 "+ getString(R.string.minutes)};
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle(R.string.snooze_title);
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit();
                        int interval = -1;
                        switch (which){
                            case 0:
                                interval = 5;
                                break;
                            case 1:
                                interval = 10;
                                break;
                            case 2:
                                interval = 15;
                                break;
                            case 3:
                                interval = 20;
                                break;
                        }
                        edit.putInt(PREF_SNOOZE_INTERVAL , interval);
                        edit.commit();
                        tvSnooze.setText(interval + " " +getString(R.string.minutes) );
                    }
                });
                builder.show();
            }
        });
    }

}
