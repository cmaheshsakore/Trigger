package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by krohnjw on 12/19/13.
 */
public class DayConstraint extends Constraint {

    public DayConstraint() { }

    public DayConstraint(String id, String trigger_id, String type, String key1, String key2) {
        super(id, trigger_id, type, key1, key2);
    }

    public DayConstraint(int type, String key1, String key2) {
        super(type, key1, key2);
    }

    @Override
    public String getType() {
        return String.valueOf(TYPE_DATE);
    }

    @Override
    public boolean isConstraintSatisfied(Context context) {
        logd("Checking constraint");
        Calendar now = Calendar.getInstance();
        logd("Current day is " + now.get(Calendar.DAY_OF_WEEK));
        String days[] = getExtra(1).split(",");
        for (int i=0; i<days.length; i++) {
            try {
                int day = Integer.parseInt(days[i]);
                if (now.get(Calendar.DAY_OF_WEEK) == day) {
                    logd("Day constraint is OK");
                    return true;
                }
            } catch (Exception e) {
                // Legacy entry, day is a string
                if (now.get(Calendar.DAY_OF_WEEK) == getCalendarDayFromDay(context, days[i])) {
                    logd("Day constraint is OK");
                    return true;
                }
            }
        }
        logd("Day constraint is false");
        return false;
    }

    public int getCalendarDayFromDay(Context context, String day) {

        if (getString(context, R.string.day_sunday).equals(day)) {
            return Calendar.SUNDAY;
        } else if (getString(context, R.string.day_monday).equals(day)) {
            return Calendar.MONDAY;
        } else if (getString(context, R.string.day_tuesday).equals(day)) {
            return Calendar.TUESDAY;
        } else if (getString(context, R.string.day_wednesday).equals(day)) {
            return Calendar.WEDNESDAY;
        } else if (getString(context, R.string.day_thursday).equals(day)) {
            return Calendar.THURSDAY;
        } else if (getString(context, R.string.day_friday).equals(day)) {
            return Calendar.FRIDAY;
        } else if (getString(context, R.string.day_saturday).equals(day)) {
            return Calendar.SATURDAY;
        }

        return -1;
    }

    private SparseIntArray  mDaysSelected;
    private SparseIntArray  mDefaultDaysSelected;
    private View            mView;
    private Context         mContext;

    private View getView() {
        return mView;
    }

    @Override
    public View getView(final Context context) {
        mContext = context;
        View base = super.getView(context);
        View child = View.inflate(context, R.layout.constraint_day, null);
        mView = child;

        if (mDaysSelected == null) {
            mDaysSelected = new SparseIntArray();
            mDefaultDaysSelected = new SparseIntArray(){
                private static final long serialVersionUID = -2369698391849377520L;
                {
                    put(Calendar.MONDAY,Calendar.MONDAY);
                    put(Calendar.TUESDAY,Calendar.TUESDAY);
                    put(Calendar.WEDNESDAY,Calendar.WEDNESDAY);
                    put(Calendar.THURSDAY,Calendar.THURSDAY);
                    put(Calendar.FRIDAY,Calendar.FRIDAY);
                }
            };
            mDaysSelected = mDefaultDaysSelected;
        }

        child.findViewById(R.id.sunday).setOnClickListener(dayClicked);
        child.findViewById(R.id.monday).setOnClickListener(dayClicked);
        child.findViewById(R.id.tuesday).setOnClickListener(dayClicked);
        child.findViewById(R.id.wednesday).setOnClickListener(dayClicked);
        child.findViewById(R.id.thursday).setOnClickListener(dayClicked);
        child.findViewById(R.id.friday).setOnClickListener(dayClicked);
        child.findViewById(R.id.saturday).setOnClickListener(dayClicked);

        updateSelectedDaysUi();

        addChildToContainer(base, child);

        return base;
    }

    @Override
    public int getText() {
        return R.string.constraint_days;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_action_calendar_month;
    }

    @Override
    public DayConstraint buildConstraint(Context context) {
        StringBuilder b = new StringBuilder();
        for (int i=0; i<mDaysSelected.size(); i++) {
            int day = mDaysSelected.valueAt(i);
            if (i > 0) {
                b.append(",");
            }
            b.append(day);
        }

        return new DayConstraint(TYPE_DATE, b.toString(), "");
    }

    @Override
    public void loadData(Context context, Constraint c) {
        super.loadData(context, c);
        mContext = context;

        String[] days = c.getExtra(1).split(",");
        Logger.d("Found " + days.length + " days");
        mDaysSelected = new SparseIntArray();
        for (int i=0; i < days.length; i++) {
            Logger.d("Adding day " + days[i]);
            // Try parsing day to int
            try {
                int d = Integer.parseInt(days[i]);
                mDaysSelected.put(d,d);
            } catch (Exception e) {
                // This is a legacy entry using a string value, get int from string
                // TODO: Get day from string here (int) and add
                int d = getDayFromString(days[i]);
                mDaysSelected.put(d, d);
            }

        }
    }

    @Override
    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        holder.constraint_date_text.setVisibility(View.VISIBLE);

        String[] days = getExtra(1).split(",");
        StringBuilder b = new StringBuilder();
        Calendar c = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat f = new SimpleDateFormat("EEE");
        for (int i=0; i< days.length; i++) {
            String day = days[i];
            if (i > 0) {
                b.append(",");
            }
            try {
                int d = Integer.parseInt(day);
                c.set(Calendar.DAY_OF_WEEK, d);
                b.append(f.format(c.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
                b.append(day);
            }

        }
        holder.constraint_date_text.setText(String.format(context.getString(R.string.display_on_days), b.toString().toUpperCase(Locale.getDefault())));
    }

    private View.OnClickListener dayClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int day = getDayFromId(v.getId());
            TextView t = (TextView) v;

            if (mDaysSelected.get(day, -1) != -1) {
                mDaysSelected.delete(day);
                setDisplay(t, false);
            } else {
                mDaysSelected.put(day, day);
                setDisplay(t, true);
            }
        }
    };

    private void updateSelectedDaysUi() {
        updateDay(R.id.sunday);
        updateDay(R.id.monday);
        updateDay(R.id.tuesday);
        updateDay(R.id.wednesday);
        updateDay(R.id.thursday);
        updateDay(R.id.friday);
        updateDay(R.id.saturday);
    }

    private void updateDay(int id) {
        setDisplay((TextView) getView().findViewById(id), (mDaysSelected.get(getDayFromId(id), -1) != -1));
    }

    private int getDayFromId(int id) {
        switch(id) {
            case R.id.sunday:
                return Calendar.SUNDAY;
            case R.id.monday:
                return Calendar.MONDAY;
            case R.id.tuesday:
                return Calendar.TUESDAY;
            case R.id.wednesday:
                return Calendar.WEDNESDAY;
            case R.id.thursday:
                return Calendar.THURSDAY;
            case R.id.friday:
                return Calendar.FRIDAY;
            case R.id.saturday:
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }

    private int getDayFromString(String s) {

        if (mContext == null) { logd("Context is null"); }

        if (s.equals("MON") || (s.equals(getString(mContext, R.string.day_monday)))) {
            return Calendar.MONDAY;
        } else if (s.equals("TUE") || (s.equals(getString(mContext, R.string.day_tuesday)))) {
            return Calendar.TUESDAY;
        } else if (s.equals("WED") || (s.equals(getString(mContext, R.string.day_wednesday)))) {
            return Calendar.WEDNESDAY;
        } else if (s.equals("THU") || (s.equals(getString(mContext, R.string.day_thursday)))) {
            return Calendar.THURSDAY;
        } else if (s.equals("FRI") || (s.equals(getString(mContext, R.string.day_friday)))) {
            return Calendar.FRIDAY;
        } else if (s.equals("SAT") || (s.equals(getString(mContext, R.string.day_saturday)))) {
            return Calendar.SATURDAY;
        } else if (s.equals("SUN") || (s.equals(getString(mContext, R.string.day_sunday)))) {
            return Calendar.SUNDAY;
        }
        return -1;
    }
    private void setDisplay(TextView t, boolean checked) {
        if (checked) {
            t.setTextColor(mContext.getResources().getColor(R.color.time_selected));
            t.setTypeface(null, Typeface.BOLD);
        } else {
            t.setTextColor(mContext.getResources().getColor(R.color.time_unselected));
            t.setTypeface(null, Typeface.NORMAL);
        }
    }

}
