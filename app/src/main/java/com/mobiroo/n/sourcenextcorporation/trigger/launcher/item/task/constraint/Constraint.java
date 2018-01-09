package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.AirplaneModeConstraint;

import junit.framework.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Constraint implements Parcelable, CompoundButton.OnCheckedChangeListener {

    public static final int TYPE_TIME = 1;
    public static final int TYPE_WIFI = 2;
    public static final int TYPE_DATE = 4;
    public static final int TYPE_CHARGING = 8;
    public static final int TYPE_BLUETOOTH = 16;
    public static final int TYPE_AIRPLANE_MODE = 32;

    protected static final String NOT_CONNECTED = "0";
    protected static final String CONNECTED = "1";
    protected static final String IS_ON = "2";
    protected static final String IS_OFF = "3";

    private String  mId;
    private String  mType;
    private String  mTriggerId;
    private String  mKey1;
    private String  mKey2;

    protected View mBaseView;
    protected Context mContext;

    protected boolean mIsEnabled;

    public Constraint() {
    }

    public Constraint(String id, String trigger, String type, String key1, String key2) {
        mId = id;
        mTriggerId = id;
        mType = type;
        mKey1 = key1;
        mKey2 = key2;
    }

    public Constraint(String type, String key1, String key2) {
        mType = type;
        mKey1 = key1;
        mKey2 = key2;
        mId = null;
        mTriggerId = null;
    }

    public Constraint(int type, String key1, String key2) {
        mType = String.valueOf(type);
        mKey1 = key1;
        mKey2 = key2;
        mId = null;
        mTriggerId = null;
    }

    public String toString() {
        return String.format("%s, %s, %s, %s, %s", mType, mKey1, mKey2, mId, mTriggerId);
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setTriggerId(String id) {
        mTriggerId = id;
    }

    public String getTriggerId() {
        return mTriggerId;
    }

    public String getExtra(int which) {
        switch (which) {
            case 1:
                return(mKey1 != null) ? mKey1 : "";
            case 2:
                return (mKey2 != null) ? mKey2 : "";
            default:
                return "";
        }
    }

    public void setExtra(int which, String value) {
        switch (which) {
            case 1:
                mKey1 = value;
                break;
            case 2:
                mKey2 = value;
                break;
        }
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTriggerId);
        dest.writeString(mType);
        dest.writeString(mKey1);
        dest.writeString(mKey2);

    }

    public static final Creator<Constraint> CREATOR = new Creator<Constraint>() {
        @Override
        public Constraint createFromParcel(Parcel source) {
            return createConstraintFromParcel(source);
        }

        @Override
        public Constraint[] newArray(int size) {
            return new Constraint[size];
        }
    };

    public static Constraint createConstraintFromParcel(Parcel source) {
        String id = source.readString();
        String trigger = source.readString();
        String type = source.readString();
        String key1 = source.readString();
        String key2 = source.readString();

        if (type.equals(String.valueOf(Constraint.TYPE_CHARGING))) {
            return new ChargingConstraint(id, trigger, type, key1, key2);
        } else if (type.equals(String.valueOf(Constraint.TYPE_DATE))) {
            return new DayConstraint(id, trigger, type, key1, key2);
        } else if (type.equals(String.valueOf(Constraint.TYPE_TIME))) {
            return new TimeConstraint(id, trigger, type, key1, key2);
        } else if (type.equals(String.valueOf(Constraint.TYPE_BLUETOOTH))) {
            return new BluetoothConstraint(id, trigger, type, key1, key2);
        } else if (type.equals(String.valueOf(Constraint.TYPE_WIFI))) {
            return new WifiConstraint(id, trigger, type, key1, key2);
        } else if (type.equals(String.valueOf(Constraint.TYPE_AIRPLANE_MODE))) {
            return new AirplaneModeConstraint(id, trigger, type, key1, key2);
        }else {
            return new Constraint(id, trigger, type, key1, key2);
        }
    }

    protected static SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm");

    public static Constraint loadFromCursor(Cursor c) {
        // Assume queried with FIELDS_CONSTRAINT

        String id = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_ID));
        String trigger_id = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_TRIGGER_ID));
        String type = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_TYPE));
        String key1 = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_KEY_1));
        String key2 = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_KEY_2));

        switch (Integer.parseInt(type)) {
            case Constraint.TYPE_CHARGING:
                return new ChargingConstraint(id, trigger_id, type, key1, key2);
            case Constraint.TYPE_DATE:
                return new DayConstraint(id, trigger_id, type, key1, key2);
            case Constraint.TYPE_TIME:
                return new TimeConstraint(id, trigger_id, type, key1, key2);
            case Constraint.TYPE_WIFI:
                return new WifiConstraint(id, trigger_id, type, key1, key2);
            case Constraint.TYPE_BLUETOOTH:
                return new BluetoothConstraint(id, trigger_id, type, key1, key2);
            case Constraint.TYPE_AIRPLANE_MODE:
                return new AirplaneModeConstraint(id, trigger_id, type, key1, key2);
            default:
                return new Constraint(id, trigger_id, type, key1, key2);
        }
    }

    public boolean isConstraintSatisfied(Context context) {
        return false;
    }
    

    protected String getString(Context context, int resId) {
        return context.getString(resId);
    }

    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        Logger.d("Using base method");
    }

    public static Holders.Trigger populateTriggerHolderViews(Context context, Holders.Trigger holder, ArrayList<Constraint> constraints) {
        for (int i=0; i< constraints.size(); i++) {
           constraints.get(i).getTriggerViewText(context, holder);
        }
        return holder;
    }

    protected void logd(String message) {
        Logger.d(getClass().getSimpleName() + ": " + message);
    }



    public View getView(Context context) {
        mContext = context;
        mBaseView = inflateBaseView(context);
        ((TextView) mBaseView.findViewById(R.id.title)).setText(getText());
        ((ImageView) mBaseView.findViewById(R.id.icon)).setImageResource(getIcon());
        ((SwitchCompat) mBaseView.findViewById(R.id.enabled)).setChecked(mIsEnabled);
        ((SwitchCompat)mBaseView.findViewById(R.id.enabled)).setOnCheckedChangeListener(this);
        return mBaseView;
    }

    protected View inflateBaseView(Context context) {
        return View.inflate(context, R.layout.constraint_base_container, null);
    }

    protected View getBaseView() {
        if (mBaseView == null) { Logger.d("ERROR: BASE VIEW IS NULL"); }
        return mBaseView;
    }

    protected int getIcon() {
        Assert.fail();
        return -1;
    }

    protected int getText() {
        Assert.fail();
        return -1;
    }

    protected void addChildToContainer(View base, View child) {
        ((LinearLayout) base.findViewById(R.id.content)).addView(child);
    }

    public static ArrayList<Constraint> getAllConstraints(int type) {
        ArrayList<Constraint> c = new ArrayList<Constraint>();
        if (type != TaskTypeItem.TASK_TYPE_TIME) {
            c.add(new TimeConstraint());
            c.add(new DayConstraint());
        }
        c.add(new WifiConstraint());
        c.add(new BluetoothConstraint());
        if (type != TaskTypeItem.TASK_TYPE_CHARGER) {
            c.add(new ChargingConstraint());
        }
        c.add(new AirplaneModeConstraint());

        return c;
    }

    public Constraint getConstraint(Context context) {
        return !mIsEnabled ? null : buildConstraint(context);
    }

    public Constraint buildConstraint(Context context) {
        return null;
    }

    public void loadData(Context context, Constraint c) {
        mIsEnabled = (c != null);
        mContext = context;
        Logger.d("E1 %s, E2 %s", c.getExtra(1), c.getExtra(2));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        mIsEnabled = checked;
    }
}
