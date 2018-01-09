package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;

/**
 * Created by krohnjw on 5/15/2014.
 */
public class AirplaneModeConstraint extends Constraint {

    private boolean mIsOn = true;

    public AirplaneModeConstraint() { }

    public AirplaneModeConstraint(String id, String trigger_id, String type, String key1, String key2) {
        super(id, trigger_id, type, key1, key2);
    }

    public AirplaneModeConstraint(int type, String key1, String key2) {
        super(type, key1, key2);
    }

    @Override
    public String getType() {
        return String.valueOf(Constraint.TYPE_AIRPLANE_MODE);
    }

    @Override
    public boolean isConstraintSatisfied(Context context) {
        logd("Checking Constraint");
        String condition = getExtra(2);

        boolean isAirplaneModeOn = false;
        if (Build.VERSION.SDK_INT < 17) {
            isAirplaneModeOn = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            isAirplaneModeOn = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }

        boolean satisfied = (IS_OFF.equals(condition)) ? !isAirplaneModeOn : isAirplaneModeOn;
        logd("Constraint satisfied: " + satisfied);

        return satisfied;
    }

    @Override
    public View getView(Context context) {

        View base = super.getView(context);
        View child = View.inflate(context, R.layout.constraint_airplane_mode, null);
        ((RadioButton) child.findViewById(mIsOn ? R.id.is_on : R.id.is_off)).setChecked(true);
        addChildToContainer(base, child);

        return base;
    }

    @Override
    public int getText() {
        return R.string.wifiOptionsAirplaneMode;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_action_plane;
    }

    @Override
    public Constraint buildConstraint(Context context) {
        int checked = ((RadioGroup) getBaseView().findViewById(R.id.connection_type)).getCheckedRadioButtonId();
        return new AirplaneModeConstraint(Constraint.TYPE_AIRPLANE_MODE, "", (checked == R.id.is_on) ? IS_ON : IS_OFF);
    }

    @Override
    public void loadData(Context context, Constraint c) {
        super.loadData(context, c);
        mIsOn = (!c.getExtra(2).isEmpty() && IS_ON.equals(c.getExtra(2)));
    }

    @Override
    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        holder.constraint_airplane_mode_text.setVisibility(View.VISIBLE);
        String condition = getExtra(2);
        holder.constraint_airplane_mode_text.setText(String.format("%s : %s",
                context.getString(R.string.wifiOptionsAirplaneMode),
                (IS_ON.equals(condition)) ? context.getString(R.string.is_on)
                        : context.getString(R.string.is_off)
        ));
    }
}
