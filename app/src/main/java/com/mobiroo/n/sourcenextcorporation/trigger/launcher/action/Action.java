package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

public interface Action {
    public static final int INDEX_ACTION = 0;
    public static final int INDEX_MESSAGE_PREFIX = 1;
    public static final int INDEX_MESSAGE_SUFFIX = 2;
    
    
    /**
     * @return String command
     */
    public String getCommand();
    /**
     * @return String code
     */
    public String getCode();
    
    /**
     * Returns an inflated view with default arguments
     * @param context context
     * @return View
     */
    public View getView(Context context);
    /**
     * Returns an inflated view based on arguments
     * @param context context
     * @param arguments arguments to populate view
     * @return View
     */
    public View getView(Context context, CommandArguments arguments);
    /**
     * Returns a suitable name for analytics
     * @return View
     */
    public String getName();
    
    /**
     * Builds all data for adding an action to a display
     * @param actionView view to pull data from
     * @param context context
     * @return string array to writing
     */
    public String[] buildAction(View actionView, Context context);
    
    public void logUsage(Context context, String command, int group);
    
    public int getMinArgLength();
    
    public String getDisplayFromMessage(String command, String[] args, Context context);
    
    public CommandArguments getArgumentsFromAction(String action);
        
    public boolean needReset();
    
    public boolean needManualRestart();
    
    public int getResumeIndex();
    
    public void performAction(Context context, int operation, String[] args, int currentIndex);
    
    public String getWidgetText(Context context, int operation);
    
    public String getNotificationText(Context context, int operation);
    
    public int getRestartDelay();
    
    public void setArgs(String[] args);
    
    public boolean scheduleWatchdog();
    
    public boolean resumeIsCurrentAction();

    public void setResumeData(String payload, int position, String name);
}
