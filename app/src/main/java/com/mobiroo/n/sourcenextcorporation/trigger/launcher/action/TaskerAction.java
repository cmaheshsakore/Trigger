package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TaskerIntent;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class TaskerAction extends BaseAction {

    public static final int REQUEST_TASKER_LIST = 40;
    private View mView;
    private Context mContext;

    public void setTaskerTask(String task) {
        EditText taskerTaskResult = (EditText) mView.findViewById(R.id.TaskerTaskResult);
        taskerTaskResult.setText(task);
    }
    @Override
    public String getCommand() {
        return Constants.COMMAND_TASKER_TASK;
    }

    @Override
    public String getCode() {
        return Codes.TASKER_TASK;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option017, null, false);

        boolean hasTasker = TaskerIntent.taskerInstalled(context);
        if (!hasTasker) {
            ((EditText) dialogView.findViewById(R.id.TaskerTaskResult)).setText(context.getString(R.string.taskerNotInstalled));
        } else {
            ((ImageButton) dialogView.findViewById(R.id.TaskerButton)).setOnClickListener(taskerTaskButtonClicked);
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.TaskerTaskResult)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        mView = dialogView;
        mContext = context;
        return dialogView;
    }

    @Override
    public String getName() {
        return "Tasker Task";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText taskerTaskResult = (EditText) actionView.findViewById(R.id.TaskerTaskResult);
        String taskerTask = taskerTaskResult.getText().toString();
        
        if ((!(taskerTask.equals(""))) && 
                (!taskerTask.equals(context.getString(R.string.taskerNotInstalled))) &&
                (!taskerTask.equals(context.getString(R.string.layoutTaskerSelect))) ) {
            
            taskerTask = Utils.encodeData(taskerTask);
            return new String[] { Constants.COMMAND_TASKER_TASK + ":" + taskerTask, context.getString(R.string.listLaunchText), taskerTask};
        } else {
            return new String[] { "" };
        }
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listLaunchText);
        try {
            display += " " + Utils.decodeData(args[0]);
        } catch (Exception e) {
            /* Ignore any exception here */
        }

        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, ""))
                );
    }

    private OnClickListener taskerTaskButtonClicked = new OnClickListener() {
        public void onClick(View v) {
            try {
                Logger.d("Starting Tasker dialog");
                ((Activity) mContext).startActivityForResult(TaskerIntent.getTaskSelectIntent(), REQUEST_TASKER_LIST);
            } catch (Exception e) {
                Toast.makeText(
                        mContext,
                        "Could not start Tasker task listing.\n\nYou can try uninstalling both apps and reinstalling Tasker and then NFC Task Launcher.  If the issue persists please contact us with the Tasker version.",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String task = Utils.tryParseString(args, 1, "");
        task = Utils.decodeData(task);
        if (!task.isEmpty()) {
            Logger.d("Launching Tasker Task " + task);
            if (TaskerIntent.havePermission(context)) {
                try {
                    TaskerIntent i = new TaskerIntent(task);
                    context.sendBroadcast(i);
                } catch (Exception e) {
                    Toast.makeText(context,"Cound not launch Tasker task " + args[1], Toast.LENGTH_LONG).show();
                    Logger.e("Exception thrown launching tasker Task",e);
                }
            } else {
                Logger.d("Permission not granted to launch tasks");
                Toast.makeText(context, "Permission to launch tasks has not been granted by Tasker.", Toast.LENGTH_LONG).show();
            }
        } else {
            Logger.d("Task name is empty");
        }

    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetTaskerTask) + " " + mArgs[1];
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionTaskerTask) + " " + mArgs[1];
    }


}
