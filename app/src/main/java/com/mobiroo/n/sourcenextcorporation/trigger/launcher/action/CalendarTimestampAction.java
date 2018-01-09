package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.util.Calendar;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

public class CalendarTimestampAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_EVENT_CALENDAR_TIMESTAMP;
    }

    @Override
    public String getCode() {
        return Codes.EVENT_CALENDAR_TIMESTAMP;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option053, null, false);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.eventTitle)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Calendar Timestamp";
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText titleET = (EditText) actionView.findViewById(R.id.eventTitle);
        String eventTitle = titleET.getText().toString();
        eventTitle = Utils.encodeData(eventTitle);
        String message = Constants.COMMAND_EVENT_CALENDAR_TIMESTAMP + ":" + eventTitle;
        return new String[] { message, context.getString(R.string.listDiaplayCalendarTimestamp), ""};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listDiaplayCalendarTimestamp);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {

            String[] args = action.split(":");
            return new CommandArguments(
                    new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, ""))
                    );
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String title = Utils.tryParseEncodedString(args, 1, "");
        title = Utils.removePlaceHolders(title);
        Calendar now = Calendar.getInstance();
        long startTime = now.getTimeInMillis();
        long endTime = now.getTimeInMillis() + 60000;
        
        CalendarUtils.insertCalendarEvent(context, startTime, endTime, title);
        setupManualRestart(currentIndex + 1);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetCalendarTimestamp);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionCalendarTimestamp);
    }

}
