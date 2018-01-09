package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ActionService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

public class BaseAction implements Action {

    protected boolean   mNeedReset          = false;
    protected boolean   mNeedManualRestart  = false;
    protected int       mResumeIndex        = -1;
    protected int       mRestartDelay       = 0;
    protected String[]  mArgs;

    protected String    mResumePayload;
    protected String    mResumeName;

    private static final Hashtable<String, String> mCommandMap = new Hashtable<String, String>() {
        private static final long serialVersionUID = 1L; // Default
        {
            put(Constants.MODULE_WIFI, Codes.OPERATE_WIFI);
            put(Constants.MODULE_BT, Codes.OPERATE_BLUETOOTH);
            put(Constants.MODULE_AIRPLANE_MODE, Codes.OPERATE_AIRPLANE_MODE);
            put(Constants.MODULE_SYNC, Codes.OPERATE_AUTO_SYNC);
            put(Constants.COMMAND_CONFIG, Codes.WIFI_CONFIGURE_NETWORK);
            put(Constants.COMMAND_WIFI_ASSOCIATE, Codes.WIFI_ASSOCIATE_NETWORK);
            put(Constants.MODULE_HOTSPOT, Codes.OPERATE_HOTSPOT);
            put(Constants.COMMAND_MEDIA_PLAYBACK, Codes.MEDIA_PLAYBACK);
            put(Constants.MODULE_GPS, Codes.OPERATE_GPS);
            put(Constants.MODULE_NOTIFICATION_LIGHT, Codes.OPERATE_NOTIFICATION_LIGHT);
            put(Constants.COMMAND_LAUNCH_APPLICATION, Codes.LAUNCH_APPLICATION);
            put(Constants.COMMAND_TASKER_TASK, Codes.TASKER_TASK);
            put(Constants.COMMAND_RINGER_TYPE, Codes.SOUND_RINGER_TYPE);
            put(Constants.COMMAND_RINGER_SOUND, Codes.SOUND_RINGER_SOUND);
            put(Constants.COMMAND_NOTIFICATION_SOUND, Codes.SOUND_NOTIFICATION_SOUND);
            put(Constants.COMMAND_RINGER_VOLUME, Codes.SOUND_RINGER_VOLUME);
            put(Constants.COMMAND_MEDIA_VOLUME, Codes.SOUND_MEDIA_VOLUME);
            put(Constants.COMMAND_ALARM_VOLUME, Codes.SOUND_ALARM_VOLUME);
            put(Constants.COMMAND_NOTIFICATION_VOLUME, Codes.SOUND_NOTIFICATION_VOLUME);
            put(Constants.COMMAND_SET_BRIGHTNESS, Codes.DISPLAY_BRIGHTNESS);
            put(Constants.COMMAND_SET_ALARM, Codes.ALARM_SET_STATIC);
            put(Constants.COMMAND_SET_ALARM_FORWARD, Codes.ALARM_SET_FORWARD);
            put(Constants.COMMAND_FOURSQUARE, Codes.CHECKIN_FOURSQUARE);
            put(Constants.COMMAND_FOURSQUARE_SEARCH, Codes.CHECKIN_FOURSQUARE_SEARCH);
            put(Constants.COMMAND_FACEBOOK, Codes.CHECKIN_FACEBOOK);
            put(Constants.COMMAND_LAUNCH_URL, Codes.LAUNCH_URL);
            put(Constants.COMMAND_TWITTER_TWEET, Codes.TWITTER_TWEET);
            put(Constants.COMMAND_VIBRATE, Codes.VIBRATE);
            put(Constants.COMMAND_NAVIGATION, Codes.NAVIGATION);
            put(Constants.COMMAND_AUTO_ROTATE, Codes.DISPLAY_AUTO_ROTATE);
            put(Constants.COMMAND_LATITUDE_PLACES, Codes.CHECKIN_LATITUDE_PLACES);
            put(Constants.COMMAND_SET_DISPLAY_TIMEOUT, Codes.DISPLAY_TIMEOUT);
            put(Constants.COMMAND_SET_ALARM_TIMER, Codes.ALARM_SET_TIMER);
            put(Constants.MODULE_MOBILE_DATA, Codes.OPERATE_MOBILE_DATA);
            put(Constants.MODULE_3G_DATA, Codes.OPERATE_3G_DATA);
            put(Constants.MODULE_CAR_DOCK, Codes.OPERATE_CAR_DOCK);
            put(Constants.MODULE_DESK_DOCK, Codes.OPERATE_DESK_DOCK);
            put(Constants.COMMAND_CALL, Codes.PHONE_CALL);
            put(Constants.COMMAND_SEND_SMS, Codes.PHONE_SMS);
            put(Constants.MODULE_KEYGUARD, Codes.LOCK_SCREEN_KEYGUARD);
            put(Constants.COMMAND_KILL_APPLICATION, Codes.KILL_APPLICATION);
            put(Constants.COMMAND_LAUNCH_TASK, Codes.LAUNCH_CUSTOM_TASK);
            put(Constants.COMMAND_EVENT_CALENDAR_STATIC, Codes.EVENT_CALENDAR_STATIC);
            put(Constants.COMMAND_EVENT_CALENDAR_TIMESTAMP, Codes.EVENT_CALENDAR_TIMESTAMP);
            put(Constants.COMMAND_PAUSE, Codes.PAUSE);
            put(Constants.COMMAND_SEND_EMAIL, Codes.EMAIL);
            put(Constants.COMMAND_SAMSUNG_BLOCK_MODE, Codes.SAMSUNG_BLOCKING_MODE);
            put(Constants.COMMAND_SAMSUNG_DRIVING_MODE, Codes.SAMSUNG_DRIVING_MODE);
            put(Constants.COMMAND_SAMSUNG_POWER_SAVER, Codes.SAMSUNG_POWER_SAVER);
            put(Constants.COMMAND_SPEAK_TTS, Codes.TTS);
            put(Constants.COMMAND_SYSTEM_VOLUME, Codes.SOUND_SYSTEM_VOLUME);
            put(Constants.COMMAND_MEDIA_NEXT, Codes.MEDIA_NEXT);
            put(Constants.COMMAND_MEDIA_PREVIOUS, Codes.MEDIA_PREVIOUS);
            put(Constants.COMMAND_SEND_GLYMPSE, Codes.GLYMPSE);
            put(Constants.COMMAND_WIFI_ASSOCIATE, Codes.WIFI_CONFIGURE_NETWORK);
            put(Constants.COMMAND_CONFIG, Codes.WIFI_CONFIGURE_NETWORK);
            put(Constants.COMMAND_MEDIA_START, Codes.MEDIA_START);
            put(Constants.COMMAND_MEDIA_STOP, Codes.MEDIA_STOP);
            put(Constants.MODULE_SIP, Codes.SIP);
            put(Constants.MODULE_DRIVE_AGENT, Codes.DRIVE_AGENT);
            put(Constants.COMMAND_BLUETOOTH_DISCOVERABLE, Codes.BLUETOOTH_DISCOVERABLE);
            put(Constants.COMMAND_CONNECT_A2DP, Codes.BLUETOOTH_CONNECT_A2DP);
            put(Constants.COMMAND_WIFI_DISPLAY_CONNECT, Codes.WIFI_DISPLAY_CONNECT);
            put(Constants.COMMAND_DISCONNECT_BLUETOOTH, Codes.BLUETOOTH_DISCONNECT);
            put(Constants.COMMAND_START_AGENT, Codes.AGENT_START);
            put(Constants.COMMAND_STOP_AGENT, Codes.AGENT_STOP);
            put(Constants.COMMAND_PAUSE_AGENT, Codes.AGENT_PAUSE);
            put(Constants.COMMAND_SHOW_TEXT, Codes.SHOW_TEXT);
            put(Constants.COMMAND_FORGET_WIFI, Codes.FORGET_WIFI_NETWORK);
            put(Constants.COMMAND_SAMSUNG_HANDS_FREE, Codes.SAMSUNG_HANDS_FREE);
            put(Constants.COMMAND_SAMSUNG_SMART_STAY, Codes.SAMSUNG_SMART_STAY);
            put(Constants.COMMAND_HTC_BOOM_SOUND, Codes.HTC_BOOM_SOUND);
            put(Constants.COMMAND_HTC_POWER_SAVER, Codes.HTC_POWER_SAVER);
            put(Constants.COMMAND_HTC_SLEEP_MODE, Codes.HTC_SLEEP_MODE);
            put(Constants.COMMAND_HTC_SMART_DISPLAY, Codes.HTC_SMART_DISPLAY);
            put(Constants.COMMAND_SAMSUNG_MULTI_WINDOW, Codes.SAMSUNG_MULTI_WINDOW);
            put(Constants.COMMAND_SHOW_TOAST, Codes.SHOW_TOAST);
            put(Constants.COMMAND_PLAY_SOUND, Codes.PLAY_SOUND);
            put(Constants.MODULE_NOTIFICATION_MODE, Codes.NOTIFICATION_MODE);
            put(Constants.COMMAND_SAMSUNG_VOICE_INPUT_CONTROL, Codes.SAMSUNG_VOICE_INPUT_CONTROL);
            put(Constants.COMMAND_UNIFIED_REMOTE, Codes.UNIFIED_REMOTE);
        }
    };

    private static final Hashtable<String, Class<?>> mActionMap = new Hashtable<String, Class<?>>() {
        private static final long serialVersionUID = 2L; // Default
        {
            put(Codes.OPERATE_AIRPLANE_MODE, AirplaneModeAction.class);
            put(Codes.OPERATE_AUTO_SYNC, AutoSyncAction.class);
            put(Codes.OPERATE_BLUETOOTH, BluetoothAdapterAction.class);
            put(Codes.OPERATE_GPS, GpsAction.class);
            put(Codes.OPERATE_HOTSPOT, WifiHotspotAction.class);
            put(Codes.OPERATE_MOBILE_DATA, MobileDataAction.class);
            put(Codes.OPERATE_WIFI, WifiAdapterAction.class);
            put(Codes.CONFIGURE_SSID, ConfigureSsidAction.class);
            put(Codes.CONNECT_SSID, ConfigureSsidAction.class); 
            put(Codes.WIFI_CONFIGURE_NETWORK, ConfigureSsidAction.class);
            put(Codes.BLUETOOTH_DISCOVERABLE, BluetoothDiscoverableAction.class);
            put(Codes.OPERATE_3G_DATA, BaseAction.class);
            put(Codes.SOUND_RINGER_TYPE, RingerTypeAction.class);
            put(Codes.SOUND_RINGER_SOUND, RingerSoundAction.class);
            put(Codes.SOUND_RINGER_VOLUME, RingerVolumeAction.class);
            put(Codes.SOUND_NOTIFICATION_SOUND, NotificationSoundAction.class);
            put(Codes.SOUND_NOTIFICATION_VOLUME, NotificationVolumeAction.class);
            put(Codes.SOUND_MEDIA_VOLUME, MediaVolumeAction.class);
            put(Codes.SOUND_ALARM_VOLUME, AlarmVolumeAction.class);
            put(Codes.VIBRATE, VibrationAction.class);
            put(Codes.EMAIL, EmailAction.class);
            put(Codes.SAMSUNG_BLOCKING_MODE, SamsungBlockingModeAction.class);
            put(Codes.SAMSUNG_DRIVING_MODE, SamsungDrivingModeAction.class);
            put(Codes.SAMSUNG_POWER_SAVER, SamsungPowerSaverAction.class);
            put(Codes.GLYMPSE, SendGlympseAction.class);
            put(Codes.TTS, TtsAction.class);
            put(Codes.DISPLAY_AUTO_ROTATE, AutoRotationAction.class);
            put(Codes.DISPLAY_BRIGHTNESS,  DisplayBrightnessAction.class);
            put(Codes.OPERATE_NOTIFICATION_LIGHT, NotificationLightAction.class);
            put(Codes.DISPLAY_TIMEOUT, DisplayTimeoutAction.class);
            put(Codes.PHONE_SMS, SmsAction.class);
            put(Codes.ALARM_SET_FORWARD,  AlarmForwardAction.class);
            put(Codes.ALARM_SET_STATIC, AlarmStaticAction.class);
            put(Codes.ALARM_SET_TIMER, TimerAction.class);
            put(Codes.EVENT_CALENDAR_TIMESTAMP, CalendarTimestampAction.class);
            put(Codes.EVENT_CALENDAR_STATIC, CalendarEventAction.class);
            put(Codes.LOCK_SCREEN_KEYGUARD, ScreenLockAction.class);
            put(Codes.PHONE_CALL, CallAction.class);
            put(Codes.TASKER_TASK, TaskerAction.class);
            put(Codes.TWITTER_TWEET, TwitterTweetAction.class);
            put(Codes.CHECKIN_FOURSQUARE_SEARCH, FoursquareCheckinGenericAction.class);
            put(Codes.CHECKIN_FOURSQUARE, FoursquareCheckinVenueAction.class);
            put(Codes.CHECKIN_FACEBOOK, FacebookCheckinAction.class);
            put(Codes.CHECKIN_LATITUDE_PLACES, GooglePlacesCheckinAction.class);
            put(Codes.OPERATE_CAR_DOCK, CarDockAction.class);
            put(Codes.OPERATE_DESK_DOCK, DeskDockAction.class);
            put(Codes.NAVIGATION, GoogleNavigationAction.class);
            put(Codes.LAUNCH_URL, LaunchURIAction.class);
            put(Codes.MEDIA_PLAYBACK, MediaPlaybackStartStopAction.class);
            put(Codes.PAUSE, PauseAction.class);
            put(Codes.LAUNCH_APPLICATION, OpenApplicationAction.class);
            put(Codes.KILL_APPLICATION, CloseApplicationAction.class);
            put(Codes.LAUNCH_CUSTOM_TASK, OpenActivityAction.class);
            put(Codes.SOUND_SYSTEM_VOLUME, SystemVolumeAction.class);
            put(Codes.MEDIA_NEXT, MediaNextAction.class);
            put(Codes.MEDIA_PREVIOUS, MediaPreviousAction.class);
            put(Codes.MEDIA_STOP, MediaPlaybackStopAction.class);
            put(Codes.MEDIA_START, MediaPlaybackStartAction.class);
            put(Codes.SIP, SipAction.class);
            put(Codes.WIFI_ASSOCIATE_NETWORK, ConfigureSsidAction.class);
            put(Codes.DRIVE_AGENT, DriveAgentAction.class);
            put(Codes.BLUETOOTH_CONNECT_A2DP, BluetoothDeviceConnectAction.class);
            put(Codes.WIFI_DISPLAY_CONNECT, WifiDisplayAction.class);
            put(Codes.BLUETOOTH_DISCONNECT, BluetoothDeviceDisconnectAction.class);
            put(Codes.AGENT_START, AgentStartAction.class);
            put(Codes.AGENT_STOP, AgentStopAction.class);
            put(Codes.AGENT_PAUSE, AgentPauseAction.class);
            put(Codes.FORGET_WIFI_NETWORK, ForgetWifiNetworkAction.class);
            put(Codes.SHOW_TEXT, ShowTextAction.class);
            put(Codes.SAMSUNG_HANDS_FREE, SamsungHandsFreeAction.class);
            put(Codes.SAMSUNG_SMART_STAY, SamsungSmartStayAction.class);
            put(Codes.HTC_BOOM_SOUND, HtcBoomSoundAction.class);
            put(Codes.HTC_POWER_SAVER, HtcPowerSaverAction.class);
            put(Codes.HTC_SLEEP_MODE, HtcSleepModeAction.class);
            put(Codes.HTC_SMART_DISPLAY, HtcSmartDisplayAction.class);
            put(Codes.SAMSUNG_MULTI_WINDOW, SamsungMultiWindowAction.class);
            put(Codes.SHOW_TOAST, ShowToastAction.class);
            put(Codes.PLAY_SOUND, PlaySoundAction.class);
            put(Codes.NOTIFICATION_MODE, NotificationModeAction.class);
            put(Codes.SAMSUNG_VOICE_INPUT_CONTROL, SamsungVoiceInputControlAction.class);
            put(Codes.UNIFIED_REMOTE, UnifiedRemoteAction.class);
        }
    };
    
    /**
     * @param command command
     * @return Mapped code
     */
    public static String getCodeFromCommand(String command) {
        String Code = "000";
        if (mCommandMap.containsKey(command)) {
            Code = mCommandMap.get(command);
        }
        return Code;
    }

    /**
     * Checks for presence of a particular command in the map
     * @param command command
     * @return true if present
     */
    public static Boolean doesMapContainCommand(String command) {
        return mCommandMap.contains(command);
    }
    
    public static Action getAction(String code) {
        
        if (mActionMap.containsKey(code)) {
            Class<?> c = mActionMap.get(code);
            try {
                Constructor<?> construct = c.getConstructor((Class<?>[]) null);
                return (Action) construct.newInstance((Object[] )null);
            } catch (Exception e) {
                Logger.e("Exception throw loading class " + c.getCanonicalName(), e);
                return new BaseAction();
            }
            
        } else {
            Logger.d(code + " is not mapped");
            return new BaseAction();
        }
    }
    
    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public View getView(Context context) {
        return getView(context, new CommandArguments());
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        return null;
    }

    @Override
    public void logUsage(Context context, String command, int group) {
        Usage.storeNamedTuple(context, this.getName(), command, group);
    }

    @Override
    public int getMinArgLength() {
        return 0;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return null;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }
    
    protected LayoutInflater getLayoutInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean needReset() {
        return mNeedReset;
    }

    @Override
    public boolean needManualRestart() {
        return mNeedManualRestart;
    }

    @Override
    public int getResumeIndex() {
        return mResumeIndex;
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return null;
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return null;
    }

    @Override
    public int getRestartDelay() {
        return mRestartDelay;
    }

    @Override
    public void setResumeData(String payload, int position, String name) {
        mResumePayload = payload;
        mResumeIndex = position;
        mResumeName = name;
    }

    protected Intent buildReturnIntent(Intent intent) {
        Logger.d("Setting return intent to %s, %s, %s", mResumeName, mResumeIndex, mResumePayload);
        intent.putExtra(ActionService.EXTRA_TAG_NAME, mResumeName);
        intent.putExtra(ActionService.EXTRA_PAYLOAD, mResumePayload);
        intent.putExtra(ActionService.EXTRA_START_POSITION, mResumeIndex);
        return intent;
    }

    protected void setupManualRestart(int next) {
        setupManualRestart(next, 3);
    }
    
    protected void setupManualRestart(int next, int seconds) {
        mNeedReset = true;
        mNeedManualRestart = true; 
        mResumeIndex = next;
        mRestartDelay = seconds;
    }
    
    protected void setAutoRestart(int index) {
        mResumeIndex = index;
        mNeedReset = true;
    }
    
    @Override
    public void setArgs(String[] args) {
        mArgs = args;
    }

    protected String getBaseWidgetSettingText(Context context, int operation, String module) {
        String text = "";
        if (operation == Constants.OPERATION_ENABLE) {
            text = String.format(context.getString(R.string.widget_enable), module);
        } else if (operation == Constants.OPERATION_DISABLE) {
            text = String.format(context.getString(R.string.widget_disable), module);
        } else if (operation == Constants.OPERATION_TOGGLE) {
            text = String.format(context.getString(R.string.widget_toggle), module);
        }
        return text;
    }
    
    protected String getBaseActionSettingText(Context context, int operation, String module) {
        String text = "";
        if (operation == Constants.OPERATION_ENABLE) {
            text = String.format(context.getString(R.string.action_enable), module);
        } else if (operation == Constants.OPERATION_DISABLE) {
            text = String.format(context.getString(R.string.action_disable), module);
        } else if (operation == Constants.OPERATION_TOGGLE) {
            text = String.format(context.getString(R.string.action_toggle), module);
        }
        return text;
    }
    
    protected String getBaseWidgetSetText(Context context, int operation, String module) {
        return String.format(context.getString(R.string.widget_set_generic), module);
    }
    
    protected String getBaseActionSetText(Context context, int operation, String module) {
        return String.format(context.getString(R.string.action_set_generic), module);
    }
    
    protected String getBaseWidgetSetToText(Context context, int operation, String module, String value) {
        return String.format(context.getString(R.string.widget_set_to), module, value);
    }
    
    protected String getBaseActionSetToText(Context context, int operation, String module, String value) {
        return String.format(context.getString(R.string.action_set_to), module, value);
    }
    
    protected String getBaseWidgetCheckinText(Context context, int operation, String module) {
        return String.format(context.getString(R.string.widget_checking_in), module);
    }
    
    protected String getBaseActionCheckinText(Context context, int operation, String module) {
        return String.format(context.getString(R.string.action_checking_in), module);
    }
    
    public boolean scheduleWatchdog() {
        return false;
    }
    
    public boolean resumeIsCurrentAction() {
        return false;
    }
    
    protected boolean hasArgument(CommandArguments arguments, String flag) {
        return ((arguments != null) && (arguments.hasArgument(flag)));
    }
}
