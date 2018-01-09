package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import junit.framework.Assert;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by krohnjw on 1/15/14.
 */
public class AgentAction extends BaseAction {

    protected class Agent {
        String name;
        String guid;

        public Agent(String name, String guid) {
            this.name = name;
            this.guid = guid;
        }
    }

    protected String COMMAND = "";

    protected static final String GUID_BATTERY     = "tryagent.battery";
    protected static final String GUID_DRIVE       = "tryagent.drive";
    protected static final String GUID_MEETING     = "tryagent.meeting";
    protected static final String GUID_PARKING     = "tryagent.parking";
    protected static final String GUID_SLEEP       = "tryagent.sleep";

    public static class AgentListItem {
        private String name;
        private String guid;

        public AgentListItem(String guid, String name) {
            this.name = name;
            this.guid = guid;
        }

        public String getGuid() {
            return guid;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }


    public static ArrayList<AgentListItem> generateAgentList(final Context context) {
        return new ArrayList<AgentListItem>() {
            {
                add(0, new AgentListItem(GUID_BATTERY, context.getString(R.string.battery_agent)));
                add(1, new AgentListItem(GUID_DRIVE, context.getString(R.string.drive_agent)));
                add(2, new AgentListItem(GUID_MEETING, context.getString(R.string.meeting_agent)));
                add(3, new AgentListItem(GUID_PARKING, context.getString(R.string.parking_agent)));
                add(4, new AgentListItem(GUID_SLEEP, context.getString(R.string.sleep_agent)));
            }
        };
    }

    public static HashMap<Integer, String> agent_guids = new HashMap<Integer, String>() {
        {
            put(0, GUID_BATTERY);
            put(1, GUID_DRIVE);
            put(2, GUID_MEETING);
            put(3, GUID_PARKING);
            put(4, GUID_SLEEP);

        }
    };

    public static String getNameFromGuid(Context context, String name) {
        if (GUID_BATTERY.equals(name)) {
            return context.getString(R.string.battery_agent);
        } else if (GUID_DRIVE.equals(name)) {
            return context.getString(R.string.drive_agent);
        } else if (GUID_MEETING.equals(name)) {
            return context.getString(R.string.meeting_agent);
        } else if (GUID_PARKING.equals(name)) {
            return context.getString(R.string.parking_agent);
        } else if (GUID_SLEEP.equals(name)) {
            return context.getString(R.string.sleep_agent);
        }

        return "";
    }

    @Override
    public String getCommand() {
        Assert.fail();
        return "";
    }

    @Override
    public String getCode() {
        Assert.fail();
        return "";
    }

    protected String getFormattedAgentString(Context context) {
        Assert.fail();
        return "";
    }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option071, null, false);

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner);
            spinner.setSelection(Integer.parseInt(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE)));
        }

        return dialogView;
    }

    @Override
    public String getName() {
        Assert.fail();
        return "";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner spinner = (Spinner) actionView.findViewById(R.id.spinner);
        int selected = spinner.getSelectedItemPosition();
        String message = getCommand() + ":" + selected + ":" + agent_guids.get(selected);
        String action = getFormattedAgentString(context);

        return new String[]{message, String.format(context.getString(R.string.agent_action), action, getAgentNameFromGuid(context, agent_guids.get(selected))), ""};
    }

    private String getAgentNameFromGuid(Context context, String guid) {
        String name = "";
        if (GUID_BATTERY.equals(guid)) { return context.getString(R.string.battery_agent); }
        else if (GUID_DRIVE.equals(guid)) { return context.getString(R.string.drive_agent); }
        else if (GUID_MEETING.equals(guid)) { return context.getString(R.string.meeting_agent); }
        else if (GUID_PARKING.equals(guid)) { return context.getString(R.string.parking_agent); }
        else if (GUID_SLEEP.equals(guid)) { return context.getString(R.string.sleep_agent); }

        return name;
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String guid = Utils.tryParseString(args, 2, "");
        if (!guid.isEmpty()) {
            return String.format(context.getString(R.string.agent_action), getFormattedAgentString(context), getAgentNameFromGuid(context, guid));
        } else {
            return String.format(context.getString(R.string.agent_title_string), getFormattedAgentString(context));
        }
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, ""))
        );
    }

    protected String getTarget() {
        Assert.fail();
        return "";
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        String guid = Utils.tryParseString(args, 2, "");
        if (!guid.isEmpty()) {
            Intent intent = new Intent();
            intent.setClassName("com.tryagent", getTarget());
            intent.putExtra("agentId", guid);
            context.startService(intent);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return "Operation is " + operation;
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return "Operation is " + operation;
    }
}
