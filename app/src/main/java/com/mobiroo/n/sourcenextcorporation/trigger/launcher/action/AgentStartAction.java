package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

/**
 * Created by krohnjw on 1/15/14.
 */
public class AgentStartAction extends AgentAction {

    @Override
    public String getCommand() { return Constants.COMMAND_START_AGENT; }

    @Override
    public String getCode() { return Codes.AGENT_START; }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        View dialogView = super.getView(context, arguments);

        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText(getFormattedAgentString(context));

        return dialogView;
    }

    @Override
    protected String getFormattedAgentString(Context context) {
        return context.getString(R.string.start);
    }

    @Override
    public String getName() {
        return "Start an Agent";
    }

    @Override
    public String getTarget() {
        return "com.tryagent.service.StartIntentService";
    }
}
