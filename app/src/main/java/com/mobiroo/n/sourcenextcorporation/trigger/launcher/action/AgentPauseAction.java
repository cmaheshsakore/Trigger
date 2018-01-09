package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

/**
 * Created by krohnjw on 1/15/14.
 */
public class AgentPauseAction extends AgentAction {

    @Override
    public String getCommand() { return Constants.COMMAND_PAUSE_AGENT; }

    @Override
    public String getCode() { return Codes.AGENT_PAUSE; }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        View dialogView = super.getView(context, arguments);

        TextView title = (TextView) dialogView.findViewById(R.id.title);
        title.setText(getFormattedAgentString(context));

        return dialogView;
    }

    @Override
    protected String getFormattedAgentString(Context context) {
        return context.getString(R.string.pause);
    }

    @Override
    public String getName() {
        return "Pause an Agent";
    }

    @Override
    public String getTarget() {
        return "com.tryagent.service.PauseIntentService";
    }
}
