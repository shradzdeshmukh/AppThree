package com.cyno.alarm.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cyno.alarm.database.AlarmTable;
import com.cyno.alarm.ui.MainActivity;
import com.cyno.alarm.ui.SettingsActivity;
import com.cyno.alarmclock.R;
import com.cyno.alarm.models.Alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;


public class AlarmListAdapter extends CursorRecyclerViewAdapter<AlarmListAdapter.ViewHolder>{

    private final Context context;
    private final OnClickListener onClickListner;
    private final SimpleDateFormat dateFormat24Hour = new SimpleDateFormat("HH:mm" , Locale.getDefault());
    private final SimpleDateFormat dateFormatAmPm = new SimpleDateFormat("hh:mm a" , Locale.getDefault());
    private SimpleDateFormat SmallDateFormat = new SimpleDateFormat("EEEEE" , Locale.getDefault());

    private static Typeface tf;
    private Set<Integer> expandedItemIdsList = new HashSet<>();

    public AlarmListAdapter(Context context, Cursor cursor, OnClickListener onclick){
        super(context,cursor);
        this.context = context;
        this.onClickListner = onclick;
        tf = Typeface.createFromAsset(context.getAssets(), MainActivity.FONT);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
        private final View bottomView;
        private final View repeatLayout;
        private final CheckBox cbRepeat;
        private final CheckBox cbVibrate;
        private final TextView tvRepeatDays;
        private final SwitchCompat swIsActive;
        private final TextView tvRingtone;
        private final TextView ivDelete;
        private TextView tvTime;
        //        private TextView sunday,monday,tuesday,wednesday,thursday,friday,saturday;
        private TextView sunday,monday,tuesday,wednesday,thursday,friday,saturday;
        private View dayLayout;
        private View root;
        private ArrayList<View> daysViewLIst;


        public ViewHolder(View view , OnClickListener onclick) {
            super(view);
            repeatLayout = view.findViewById(R.id.alarms_list_item_time_repeat_layout);
            cbRepeat = (CheckBox) view.findViewById(R.id.cb_alarms_list_item_repeat);
            cbVibrate = (CheckBox) view.findViewById(R.id.cb_alarms_list_item_vibrate);
            tvTime = (TextView) view.findViewById(R.id.tv_alarms_list_item_time);
            bottomView = view.findViewById(R.id.ll_alarms_list_item_bottom);
            dayLayout = view.findViewById(R.id.ll_alarms_list_item_days_layout);
            tvRepeatDays = (TextView) view.findViewById(R.id.tv_alarms_list_item_repeat_days);
            swIsActive = (SwitchCompat) view.findViewById(R.id.sw_alarms_list_item_on_off);
            tvRingtone = (TextView) view.findViewById(R.id.tv_alarms_list_item_ringtone);
            ivDelete = (TextView) view.findViewById(R.id.tv_alarms_list_item_delete);
            root = view.findViewById(R.id.alarms_list_item_time_root);


            sunday = (TextView) view.findViewById(R.id.tv_alarms_list_item_sunday);
            monday = (TextView) view.findViewById(R.id.tv_alarms_list_item_monday);
            tuesday = (TextView) view.findViewById(R.id.tv_alarms_list_item_tuesday);
            wednesday = (TextView) view.findViewById(R.id.tv_alarms_list_item_wednesday);
            thursday = (TextView) view.findViewById(R.id.tv_alarms_list_item_thursday);
            friday = (TextView) view.findViewById(R.id.tv_alarms_list_item_friday);
            saturday = (TextView) view.findViewById(R.id.tv_alarms_list_item_saturday);

            daysViewLIst = new ArrayList<>();
            daysViewLIst.add(sunday);
            daysViewLIst.add(monday);
            daysViewLIst.add(tuesday);
            daysViewLIst.add(wednesday);
            daysViewLIst.add(thursday);
            daysViewLIst.add(friday);
            daysViewLIst.add(saturday);

            tvTime.setTypeface(tf);
            tvRepeatDays.setTypeface(tf);


        }

        @Override
        public void onClick(View v) {

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(this.context, R.layout.alarms_list_item, null);
        ViewHolder vh = new ViewHolder(itemView , onClickListner );
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {
        final Alarm mAlarm = Alarm.getAlarmFromCursor(cursor);
        Calendar mCalander = Calendar.getInstance();
        mCalander.setTimeInMillis(mAlarm.getTime());
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.PREF_IS_24HOUR , true))
            viewHolder.tvTime.setText(dateFormat24Hour.format(mCalander.getTime()));
        else
            viewHolder.tvTime.setText(dateFormatAmPm.format(mCalander.getTime()));

        viewHolder.swIsActive.setChecked(mAlarm.isActive());
        viewHolder.tvRingtone.setText(mAlarm.getDisplayRingtone(context));
        viewHolder.tvRepeatDays.setText(Alarm.getRepeatDaysText(mAlarm.getRepeatDays(), context, mAlarm.isRepeat()));


        viewHolder.cbRepeat.setTag(mAlarm);
        viewHolder.cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    viewHolder.repeatLayout.setVisibility(View.VISIBLE);
                else
                    viewHolder.repeatLayout.setVisibility(View.GONE);

                Alarm mAlarm = (Alarm) buttonView.getTag();
                mAlarm.setIsRepeat(isChecked);
                Alarm.storeLocally(mAlarm, context);
            }
        });
        viewHolder.cbRepeat.setChecked(mAlarm.isRepeat());


        viewHolder.swIsActive.setTag(mAlarm);
        viewHolder.swIsActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Alarm mAlarm = (Alarm) buttonView.getTag();
                mAlarm.setIsActive(isChecked);
                Alarm.storeLocally(mAlarm, context);
            }
        });
        viewHolder.swIsActive.setChecked(mAlarm.isActive());


        viewHolder.cbVibrate.setTag(mAlarm);
        viewHolder.cbVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Alarm mAlarm = (Alarm) buttonView.getTag();
                mAlarm.setIsVibrate(isChecked);
                Alarm.storeLocally(mAlarm, context);
            }
        });

        viewHolder.cbVibrate.setChecked(mAlarm.isVibrate());


        if(expandedItemIdsList.contains(cursor.getInt(cursor.getColumnIndex(AlarmTable.COL_ID)))){
            viewHolder.bottomView.setVisibility(View.VISIBLE);
            viewHolder.dayLayout.setVisibility(View.GONE);
            if(Build.VERSION.SDK_INT >= 23)
                viewHolder.root.setBackgroundColor(context.getColor(R.color.very_dark_gray));
            else
                viewHolder.root.setBackgroundColor(context.getResources().getColor(R.color.very_dark_gray));
        }else {
            viewHolder.bottomView.setVisibility(View.GONE);
            viewHolder.dayLayout.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT >= 23)
                viewHolder.root.setBackgroundColor(context.getColor(R.color.black));
            else
                viewHolder.root.setBackgroundColor(context.getResources().getColor(R.color.black));
        }


        viewHolder.root.setTag(R.string.day_layout, viewHolder.dayLayout);
        viewHolder.repeatLayout.setTag(R.string.tag_alarm, mAlarm);
        viewHolder.sunday.setOnClickListener(dayClickListnerlistner);
        viewHolder.monday.setOnClickListener(dayClickListnerlistner);
        viewHolder.tuesday.setOnClickListener(dayClickListnerlistner);
        viewHolder.wednesday.setOnClickListener(dayClickListnerlistner);
        viewHolder.thursday.setOnClickListener(dayClickListnerlistner);
        viewHolder.friday.setOnClickListener(dayClickListnerlistner);
        viewHolder.saturday.setOnClickListener(dayClickListnerlistner);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            viewHolder.sunday.setActivated(Alarm.isDayActive(Integer.valueOf((String) viewHolder.sunday.getTag()), mAlarm));
        }

        viewHolder.root.setTag(R.string.bottom_visible, false);
        viewHolder.root.setTag(R.string.day_layout, viewHolder.dayLayout);
        viewHolder.root.setTag(R.string.view_id, cursor.getInt(cursor.getColumnIndex(AlarmTable.COL_ID)));
        viewHolder.root.setTag(R.string.down, viewHolder.bottomView);
        viewHolder.root.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                View bottomView = (View) v.getTag(R.string.down);
                View daysView = (View) v.getTag(R.string.day_layout);

                if ((boolean) v.getTag(R.string.bottom_visible)) {
                    if (Build.VERSION.SDK_INT >= 23)
                        v.setBackgroundColor(context.getColor(R.color.black));
                    else
                        v.setBackgroundColor(context.getResources().getColor(R.color.black));

                    v.setTag(R.string.bottom_visible, false);
                    bottomView.setVisibility(View.GONE);
                    daysView.setVisibility(View.VISIBLE);
                    expandedItemIdsList.remove((Integer) v.getTag(R.string.view_id));
                } else {
                    if (Build.VERSION.SDK_INT >= 23)
                        v.setBackgroundColor(context.getColor(R.color.very_dark_gray));
                    else
                        v.setBackgroundColor(context.getResources().getColor(R.color.very_dark_gray));
                    v.setTag(R.string.bottom_visible, true);
                    bottomView.setVisibility(View.VISIBLE);
                    daysView.setVisibility(View.GONE);
                    expandedItemIdsList.add((Integer) v.getTag(R.string.view_id));
                }
            }
        });

        viewHolder.ivDelete.setTag(mAlarm.getId());
        viewHolder.ivDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Alarm.DeleteAlarm((Integer) v.getTag(), context);
            }
        });

        for(View mView : viewHolder.daysViewLIst){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mView.setActivated(Alarm.isDayActive(Integer.valueOf((String) mView.getTag()) , mAlarm));
            }
        }

        viewHolder.tvTime.setTag(mAlarm);
        viewHolder.tvRingtone.setTag(mAlarm);
        viewHolder.tvTime.setOnClickListener(onClickListner);
        viewHolder.tvRingtone.setOnClickListener(onClickListner);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK , cal.getFirstDayOfWeek());
        for(int index = 0 ; index < 7 ; ++index){
            ((TextView)viewHolder.daysViewLIst.get(index)).setText(SmallDateFormat.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
    }

    private OnClickListener dayClickListnerlistner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                v.setActivated(!v.isActivated());

                View p = (View) v.getParent();
                View parent = (View) p.getParent();
                Alarm alarm = (Alarm) parent.getTag(R.string.tag_alarm);
                SortedSet<Long> repDays = alarm.getRepeatDays();

                if(v.isActivated())
                    repDays.add(Alarm.getTimeOfDay(Integer.valueOf((String) v.getTag()), alarm.getTime()));
                else
                    repDays.remove((Long)Alarm.getTimeOfDay(Integer.valueOf((String) v.getTag()), alarm.getTime()));

                if(repDays.size() == 0) {
                    alarm.setIsRepeat(false);
                    repDays.add(alarm.getTime());
                }
                alarm.setRepeatDays(repDays);
                Alarm.storeLocally(alarm, context);
                Alarm.startAlarmService(context);
            }
        }

    };
}