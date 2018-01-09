package com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.mobiroo.n.sourcenextcorporation.trigger.FlavorInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.Action;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActionCategory;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Options;

import java.util.ArrayList;
import java.util.Locale;


public class BuildTools {

    public static boolean shouldShowFreeTags() {
        return false; // Never showing free tags now.
    }

    public static boolean shouldShowShop() {
        boolean show = true;
        if (BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_VODAFONE)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_AVEA)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_TMOBILE)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_NXP)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_KOREA)) {
            show = false;
        }
        return show;
    }


    public static boolean shouldShowRootOperation() {
        if ((BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_TMOBILE))
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_VODAFONE)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_AVEA)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_NXP)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_VERIZON)) {
            return false;
        }

        if (isTclDevice()) {
            return false;
        }

        return true;
    }

    public static boolean isTclDevice() {
        String product = Build.DEVICE.toLowerCase(Locale.ENGLISH);
        Logger.d("Device is " + product);

        Logger.d("Model=%s, Device=%s, Product=%s", Build.MODEL, Build.DEVICE, Build.PRODUCT);
        Logger.d("Device Property is " + System.getProperty("ro.product.device", ""));
        ArrayList<String> TCL_DEVICES = new ArrayList<String>() {
            {
                add("eclipse");
                add("eos_lte");
                add("rio5");
                add("rio_4g");
                add("rio6_lte");
                add("miata_lte");
                add("hero2");
            }
        };

        Logger.d("isTclDevice? " + TCL_DEVICES.contains(product));

        if (TCL_DEVICES.contains(product)) {
            return true;
        }
        return false;
    }

    /**
     * @param context
     * @param checkApiLevel Whether or not to show below a specific API level 
     * @param apiLevel api level at which this became a secure setting
     * @return
     */
    public static boolean shouldShowSecureSettingOperation(Context context, boolean checkApiLevel, int apiLevel) {

        /* If WRITE_SECURE_SETTINGS has been granted return true */
        PackageManager pm = context.getPackageManager();
        if ((pm.checkPermission(permission.WRITE_SECURE_SETTINGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }

        /* Otherwise deal with this on a per build basis */
        if (!checkApiLevel) {
            return FlavorInfo.SHOW_SECURE_SETTINGS;
        } else {
            if (!FlavorInfo.SHOW_SECURE_SETTINGS) {
                if (Build.VERSION.SDK_INT >= apiLevel) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }
    
    /**
     * Generates a build specific list of categories and actions for display in ActionPickerActivity
     * @return ActionCategory[] of Categories and build specific actions for that category
     */
    public static ActionCategory[] buildActionCategoryList(boolean hideSecureSettings) {
        ArrayList<ActionCategory> list = new ArrayList<ActionCategory>();
        list.add(new ActionCategory(Options.CATEGORY_WIFI, R.string.menuWireless, R.drawable.ic_category_networks, BuildTools.buildActionList(Options.CATEGORY_WIFI, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_BLUETOOTH, R.string.menuBluetooth, R.drawable.ic_action_bluetooth, BuildTools.buildActionList(Options.CATEGORY_BLUETOOTH, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_SOUND, R.string.menuSound, R.drawable.ic_category_sound, BuildTools.buildActionList(Options.CATEGORY_SOUND, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_DISPLAY, R.string.menuDisplay, R.drawable.ic_category_display, BuildTools.buildActionList(Options.CATEGORY_DISPLAY, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_SOCIAL, R.string.menuCheckin, R.drawable.ic_category_social, BuildTools.buildActionList(Options.CATEGORY_SOCIAL, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_MESSAGES, R.string.menuMessages, R.drawable.ic_category_messages, BuildTools.buildActionList(Options.CATEGORY_MESSAGES, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_APPLICATIONS, R.string.menuApplications, R.drawable.ic_category_apps, BuildTools.buildActionList(Options.CATEGORY_APPLICATIONS, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_MEDIA, R.string.menuMedia, R.drawable.ic_category_media, BuildTools.buildActionList(Options.CATEGORY_MEDIA, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_AGENT, R.string.menu_agent, R.drawable.ic_category_agent, BuildTools.buildActionList(Options.CATEGORY_AGENT, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_TRAVEL, R.string.menuTravel, R.drawable.ic_action_car, BuildTools.buildActionList(Options.CATEGORY_TRAVEL, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_ALARM, R.string.menuAlarm, R.drawable.ic_category_alarm, BuildTools.buildActionList(Options.CATEGORY_ALARM, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_EVENTS, R.string.menuEvents, R.drawable.ic_category_events, BuildTools.buildActionList(Options.CATEGORY_EVENTS, hideSecureSettings)));
        list.add(new ActionCategory(Options.CATEGORY_PHONE, R.string.menuPhone, R.drawable.ic_category_phone, BuildTools.buildActionList(Options.CATEGORY_PHONE, hideSecureSettings)));
        if ((!isTclDevice())) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                list.add(new ActionCategory(Options.CATEGORY_SAMSUNG, R.string.menuSamsung, R.drawable.ic_category_settings, BuildTools.buildActionList(Options.CATEGORY_SAMSUNG, hideSecureSettings)));
            }
            list.add(new ActionCategory(Options.CATEGORY_HTC, R.string.menuHtc, R.drawable.ic_category_settings, BuildTools.buildActionList(Options.CATEGORY_HTC, hideSecureSettings)));
        }

        list.add(new ActionCategory(Options.CATEGORY_TASKER, R.string.menuTasker, R.drawable.ic_category_tasker, BuildTools.buildActionList(Options.CATEGORY_TASKER, hideSecureSettings)));

        if (!isTclDevice()) {
            list.add(new ActionCategory(Options.CATEGORY_EXPERIMENTAL, R.string.menu_experimental, R.drawable.ic_category_security, BuildTools.buildActionList(Options.CATEGORY_EXPERIMENTAL, hideSecureSettings)));
        }

        return list.toArray(new ActionCategory[list.size()]);
    }

    /**
     * Generates a build specific list of actions for a specific category.  Uses Options.BUILD_PROFILE and API level to build lists for alternate builds
     * @param category
     * @return
     */
    public static Action[] buildActionList(String category, boolean hideSecureSettings) {
        Action[] actions = null;
        if (category.equals(Options.CATEGORY_WIFI)) {
            return buildWifiActionList(hideSecureSettings);
        } else if (category.equals(Options.CATEGORY_BLUETOOTH)) {
            return buildBluetoothActionList(hideSecureSettings);
        } else if (category.equals(Options.CATEGORY_SOUND)) {
            return buildSoundActionList();
        } else if (category.equals(Options.CATEGORY_DISPLAY)) {
            return buildDisplayActionList();
        } else if (category.equals(Options.CATEGORY_SECURITY)) {
            return buildSecurityActionList();
        } else if (category.equals(Options.CATEGORY_SOCIAL)) {
            return buildSocialActionList();
        } else if (category.equals(Options.CATEGORY_PHONE)) {
            return buildPhoneActionList();
        } else if (category.equals(Options.CATEGORY_EVENTS)) {
            return buildEventsActionList();
        } else if (category.equals(Options.CATEGORY_APPLICATIONS)) {
            return buildApplicationsActionList();
        } else if (category.equals(Options.CATEGORY_ALARM)) {
            return buildAlarmActionList();
        } else if (category.equals(Options.CATEGORY_TASKER)) {
            return buildTaskerActionList();
        } else if (category.equals(Options.CATEGORY_MESSAGES)) {
            return buildMessagesActionList();
        } else if (category.equals(Options.CATEGORY_SAMSUNG)){
            return buildSamsungActionList();
        } else if (category.equals(Options.CATEGORY_MEDIA)) {
            return buildMediaActionList();
        } else if (category.equals(Options.CATEGORY_TRAVEL)) {
            return buildTravelList(hideSecureSettings);
        }else if (category.equals(Options.CATEGORY_EXPERIMENTAL)) {
            return buildExperimentalActionList();
        } else if (category.equals(Options.CATEGORY_AGENT)) {
            return buildAgentActionList();
        } else if (category.equals(Options.CATEGORY_HTC)){
            return buildHtcActionList();
        }
        return actions;
    }

    public static boolean hideForCarriers() {
        return ((BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_TMOBILE)) || (BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_VODAFONE))|| (BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_AVEA)) || (BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_VERIZON)));
    }
    public static Action[] buildAgentActionList() {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action(Codes.AGENT_START, R.string.agent_title_start));
        actions.add(new Action(Codes.AGENT_STOP, R.string.agent_title_stop));
        actions.add(new Action(Codes.AGENT_PAUSE, R.string.agent_title_pause));

        return actions.toArray(new Action[actions.size()]);
    }

    public static Action[] buildTravelList(boolean hideSecureSettings) {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action(Codes.NAVIGATION,         R.string.layoutAppLaunchNavigation));
        if (!hideForCarriers()) {
            actions.add(new Action(Codes.OPERATE_CAR_DOCK,   R.string.layoutAppCarDock));
        }

        return actions.toArray(new Action[actions.size()]);
    }
    public static Action[] buildMediaActionList() {
        return new Action[] {
                new Action(Codes.MEDIA_PLAYBACK,    R.string.listSoundPlayback),
                new Action(Codes.MEDIA_START,       R.string.start_media_playback),
                new Action(Codes.MEDIA_STOP,        R.string.stop_media_playback),
                new Action(Codes.MEDIA_NEXT,        R.string.media_next),
                new Action(Codes.MEDIA_PREVIOUS,    R.string.media_previous)
        };
    }

    public static Action[] buildSamsungActionList() {
        return new Action[] {
                new Action(Codes.SAMSUNG_BLOCKING_MODE, R.string.block_mode),
                new Action(Codes.SAMSUNG_DRIVING_MODE,  R.string.driving_mode),
                new Action(Codes.SAMSUNG_POWER_SAVER,   R.string.power_saver),
                new Action(Codes.SAMSUNG_HANDS_FREE,    R.string.samsung_hands_free),
                new Action(Codes.SAMSUNG_SMART_STAY,    R.string.samsung_smart_stay),
                new Action(Codes.SAMSUNG_MULTI_WINDOW,    R.string.samsung_multi_window),
                new Action(Codes.SAMSUNG_VOICE_INPUT_CONTROL,    R.string.voice_input_control)
        };
    }

    public static Action[] buildHtcActionList() {
        return new Action[] {
                new Action(Codes.HTC_SMART_DISPLAY, R.string.htc_smart_display),
                new Action(Codes.HTC_BOOM_SOUND,    R.string.htc_boom_sound),
                new Action(Codes.HTC_POWER_SAVER,   R.string.htc_power_saver)
                //new Action(Codes.HTC_SLEEP_MODE,    R.string.htc_sleep_mode) // Not working, may need an additional broadcast on change or internal flagging
        };
    }

    public static Action[] buildMessagesActionList() {
        return new Action[] {
                new Action(Codes.OPERATE_AUTO_SYNC, R.string.wifiOptionsAutoSync),
                new Action(Codes.EMAIL,             R.string.layoutEmail),
                new Action(Codes.PHONE_SMS,         R.string.layoutPhoneSMS),
                new Action(Codes.GLYMPSE,           R.string.heading_glympse),
                new Action(Codes.SHOW_TEXT,         R.string.show_user_text),
                new Action(Codes.SHOW_TOAST,        R.string.show_toast)
        };
    }

    public static Action[] buildTaskerActionList() {
        return new Action[] {
                new Action(Codes.TASKER_TASK, R.string.layoutTaskerLaunchText)
        };
    }

    public static Action[] buildAlarmActionList() {
        return new Action[] {
                new Action(Codes.ALARM_SET_STATIC,  R.string.displayAlarmSet),
                new Action(Codes.ALARM_SET_TIMER,   R.string.displayListAlarmSet),
                new Action(Codes.ALARM_SET_FORWARD, R.string.displayAlarmSetTimer)
        };
    }

    public static Action[] buildApplicationsActionList() {

        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action(Codes.LAUNCH_APPLICATION, R.string.layoutAppLaunchAppText));
        actions.add(new Action(Codes.LAUNCH_CUSTOM_TASK, R.string.layoutAppLaunchActivityText));
        actions.add(new Action(Codes.PAUSE,              R.string.layoutAppPause));
        actions.add(new Action(Codes.LAUNCH_URL,         R.string.layoutAppLaunchURL));
        actions.add(new Action(Codes.TTS,                R.string.heading_tts));
        actions.add(new Action(Codes.UNIFIED_REMOTE,     R.string.unified_remote));
        if (!hideForCarriers()) {
            actions.add(new Action(Codes.OPERATE_DESK_DOCK,  R.string.layoutAppDeskDock));
        }
        
        return actions.toArray(new Action[actions.size()]);
    }

    public static Action[] buildEventsActionList() {
        return new Action[] {
                new Action(Codes.EVENT_CALENDAR_STATIC,    R.string.menuEventsCalendarStatic),
                new Action(Codes.EVENT_CALENDAR_TIMESTAMP, R.string.menuEventsCalendarTimestamp)
        };
    }

    public static Action[] buildPhoneActionList() {
        return new Action[] {
                new Action(Codes.PHONE_CALL, R.string.layoutPhoneCall),
                new Action(Codes.SIP, R.string.receive_sip_calls)
        };
    }

    public static Action[] buildSocialActionList() {
        return new Action[] {
                new Action(Codes.TWITTER_TWEET,             R.string.optionsSocialTwitter),
                new Action(Codes.CHECKIN_FOURSQUARE_SEARCH, R.string.optionsSocialFoursquare),
                new Action(Codes.CHECKIN_FOURSQUARE,        R.string.optionsSocialFoursquareVenue),
                new Action(Codes.CHECKIN_FACEBOOK,          R.string.optionsSocialFacebook)
        };
    }

    public static Action[] buildSecurityActionList() {
        return new Action[] {
                new Action(Codes.LOCK_SCREEN_KEYGUARD, R.string.layoutOptionsKeyguard)
        };
    }

    public static Action[] buildExperimentalActionList() {
        ArrayList<Action> actions = new ArrayList<Action>();

        if ((Build.VERSION.SDK_INT >= 18)) {
            actions.add(new Action(Codes.OPERATE_AIRPLANE_MODE,  R.string.wifiOptionsAirplaneMode));
        }
        
        actions.add(new Action(Codes.LOCK_SCREEN_KEYGUARD, R.string.layoutOptionsKeyguard));

        if (!hideForCarriers()) {
            actions.add(new Action(Codes.KILL_APPLICATION,   R.string.layoutAppKillAppText));
        }

        actions.add(new Action(Codes.FORGET_WIFI_NETWORK,     R.string.forget_network));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actions.add(new Action(Codes.OPERATE_MOBILE_DATA, R.string.wifiOptionsMobileData));
        }

        return actions.toArray(new Action[actions.size()]);
    }

    public static Action[] buildDisplayActionList() {
        return new Action[] {
                new Action(Codes.DISPLAY_BRIGHTNESS,         R.string.layoutDisplayBrightnessLevel),
                new Action(Codes.OPERATE_NOTIFICATION_LIGHT, R.string.layoutDisplayNotificationLight),
                new Action(Codes.DISPLAY_AUTO_ROTATE,        R.string.layoutDisplayAutoRotation),
                new Action(Codes.DISPLAY_TIMEOUT,            R.string.layoutDisplayTimeout)
        };
    }

    public static Action[] buildSoundActionList() {

        boolean combine_streams = ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) && (Build.FINGERPRINT.startsWith("google/")));

        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action(Codes.SOUND_RINGER_TYPE,         R.string.soundOptionsRinger));
        actions.add(new Action(Codes.SOUND_RINGER_SOUND,        R.string.soundOptionsRingtone));

        if (combine_streams) {
            /* Combine ringer and notification volume for Google builds ICS+ */
            actions.add(new Action(Codes.SOUND_RINGER_VOLUME,       R.string.soundOptionsRingVolumeCombined));
        } else {
            actions.add(new Action(Codes.SOUND_RINGER_VOLUME,       R.string.soundOptionsRingVolume));
        }
        actions.add(new Action(Codes.SOUND_NOTIFICATION_SOUND,  R.string.soundOptionsNotificationtone));
        if (!combine_streams) {
            actions.add(new Action(Codes.SOUND_NOTIFICATION_VOLUME,  R.string.soundOptionsNotificationVolume));
        }
        actions.add(new Action(Codes.SOUND_MEDIA_VOLUME,        R.string.soundOptionsMediaVolume));
        actions.add(new Action(Codes.SOUND_SYSTEM_VOLUME,       R.string.system_volume));
        actions.add(new Action(Codes.SOUND_ALARM_VOLUME,        R.string.soundOptionsAlarmVolume));
        
        if (!Build.MODEL.contains("SAMSUNG")) {
            actions.add(new Action(Codes.VIBRATE,                   R.string.soundOptionsVibrate));
        }

        actions.add(new Action(Codes.PLAY_SOUND,    R.string.play_sound));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actions.add(new Action(Codes.NOTIFICATION_MODE,     R.string.notification_mode_title));
        }

        return actions.toArray(new Action[actions.size()]);
    }

    public static Action[] buildBluetoothActionList(boolean hideSecureSettings) {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action(Codes.OPERATE_BLUETOOTH,      R.string.wifiOptionsOperateBluetooth));
        actions.add(new Action(Codes.BLUETOOTH_DISCOVERABLE, R.string.wifiOptionsBluetoothDiscoverable));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actions.add(new Action(Codes.BLUETOOTH_CONNECT_A2DP, R.string.bluetooth_connect_a2dp_heading));
            actions.add(new Action(Codes.BLUETOOTH_DISCONNECT, R.string.bluetooth_disconnect));
        }

        return actions.toArray(new Action[actions.size()]);
    }

    public static Action[] buildWifiActionList(boolean hideSecureSettings) {

        /* This is a Jelly Bean build, exclude both GPS and Airplane Mode as WRITE_SECURE_SETTINGS is not granted*/
        boolean hide_global = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1);

        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action(Codes.OPERATE_WIFI,           R.string.wifiOptionsOperateWifi));
        actions.add(new Action(Codes.CONFIGURE_SSID,         R.string.wifiOptionsConfigureSSID));

        if (!hideSecureSettings) {
            actions.add(new Action(Codes.OPERATE_HOTSPOT, R.string.hotspotOptions));
        }

        if ((!hideSecureSettings || !hide_global) && (Build.VERSION.SDK_INT < 18)) {
            actions.add(new Action(Codes.OPERATE_AIRPLANE_MODE,  R.string.wifiOptionsAirplaneMode));
        }
        if (!hideSecureSettings) {
            actions.add(new Action(Codes.OPERATE_GPS,            R.string.wifiOptionsGPS));
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            actions.add(new Action(Codes.OPERATE_MOBILE_DATA, R.string.wifiOptionsMobileData));
        }
        //actions.add(new Action(Codes.WIFI_DISPLAY_CONNECT, R.string.miracast_device));

        return actions.toArray(new Action[actions.size()]);
    }

    public static boolean showContactPickers() {
        if (Build.MODEL.startsWith("HTC") || Build.MODEL.startsWith("Xperia") || Build.MANUFACTURER.startsWith("HTC") || Build.MANUFACTURER.startsWith("Sony")) {
            return false;
        } else {
            return true;
        }
    }
}
