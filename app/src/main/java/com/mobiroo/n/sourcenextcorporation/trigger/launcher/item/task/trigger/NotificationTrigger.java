package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.EventConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by krohnjw on 1/8/14.
 */
public class NotificationTrigger extends Trigger {

    private static HashMap<String, String> accountMap;

    public NotificationTrigger() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot instantiate through default constructor");
    }

    public static String serializeExtraFromView(View v) throws JSONException {
        JSONObject output = new JSONObject();

        // Store name
        output.put(EventConfiguration.KEY_NAME, ((TextInputLayout) v.findViewById(R.id.name)).getEditText().getText().toString());
        output.put(EventConfiguration.KEY_DESCRIPTION, ((TextInputLayout) v.findViewById(R.id.description)).getEditText().getText().toString());

        // Get match types for name ane description
        int match = (((RadioGroup) v.findViewById(R.id.name_option)).getCheckedRadioButtonId()
                == R.id.option_contains) ? EventConfiguration.MATCH_TYPE_CONTAINS
                : EventConfiguration.MATCH_TYPE_MATCHES;
        output.put(EventConfiguration.KEY_NAME_MATCH, match);

        match = (((RadioGroup) v.findViewById(R.id.description_option)).getCheckedRadioButtonId()
                == R.id.option_contains) ? EventConfiguration.MATCH_TYPE_CONTAINS
                : EventConfiguration.MATCH_TYPE_MATCHES;

        output.put(EventConfiguration.KEY_DESCRIPTION_MATCH, match);

        Logger.d("NOTIFICATION: Storing " + output.toString());
        return output.toString();
    }

    private static String tryGetStringFromCursor(Cursor c, int column) {
        String value = "";
        try {
            value = c.getString(column);
            if (value == null) {
                value = "";
            }
        } catch (CursorIndexOutOfBoundsException ignored) {
            return "";
        }

        return value;
    }

    private static int tryGetIntFromCursor(Cursor c, int column) {
        int value = -999999;
        try {
            value = c.getInt(column);
        } catch (CursorIndexOutOfBoundsException ignored) {
            return -999999;
        }

        return value;
    }

    public static boolean eventMatchesTask(TaskSet task, String name, String description) {
        boolean matches = true;
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(description)) {
            return false;
        }

        if (task == null || task.getTriggers() == null) return false;

        // Check title
        Trigger trigger = task.getTrigger(0);
        EventConfiguration configuration = null;

        try {
            configuration = EventConfiguration.deserializeExtra(trigger.getExtra(1));
        } catch (JSONException e) {
            Logger.e("CALENDAR: Exception deserializing event in match check: " + e, e);
        }

        if (configuration == null) return false;

        if (!configuration.name.isEmpty()) {
            Logger.d(String.format("NOTIFICATION: Checking for name match %1$s, type %2$s", name, configuration.name_match_type));
            if (configuration.name_match_type == EventConfiguration.MATCH_TYPE_CONTAINS) {
                matches = name.toLowerCase().contains(configuration.name.toLowerCase()) || name.toLowerCase().startsWith(configuration.name.toLowerCase());
            } else {
                matches = name.equalsIgnoreCase(configuration.name);
            }
            Logger.d("NOTIFICATION: Matches is " + matches);
        }

        if (!matches) return false;

        if (!configuration.description.isEmpty()) {
            Logger.d(String.format("NOTIFICATION: Checking for description match %1$s, type %2$s", description, configuration.description_match_type));
            if (configuration.description_match_type == EventConfiguration.MATCH_TYPE_CONTAINS) {
                matches = description.toLowerCase().contains(configuration.description.toLowerCase());
            } else {
                matches = description.equalsIgnoreCase(configuration.description);
            }
            Logger.d("NOTIFICATION: Matches is " + matches);
        }
        return matches;
    }
}
