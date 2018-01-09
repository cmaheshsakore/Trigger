package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class WidgetIcons {
    
    private static final WidgetIcon[] mIconList = new WidgetIcon[] {
            new WidgetIcon(R.drawable.ic_shortcut_bluetooth, "Bluetooth"),
            new WidgetIcon(R.drawable.ic_shortcut_alarm, "Clock"),
            new WidgetIcon(R.drawable.ic_shortcut_armchair, "Playback"),
            new WidgetIcon(R.drawable.ic_shortcut_bike, "Alarm"),
            new WidgetIcon(R.drawable.ic_shortcut_book, "Apps"),
            new WidgetIcon(R.drawable.ic_shortcut_bulb, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_business, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_calendar_day, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_car, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_coffee, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_dialog, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_gear, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_heart, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_home, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_joypad, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_key, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_laptop, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_music_1, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_navigate, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_nes, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_phone_start, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_star_0, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_user, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_volume, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_volume_mute, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_volume_up, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_wheel, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_wifi, "Display"),
            new WidgetIcon(R.drawable.ic_shortcut_airplane, "Display")
                };

    public static WidgetIcon[] getIcons() {
        return mIconList;
    }
    
    /**
     * @param name
     * @return null if no icon found by that name
     */
    public static WidgetIcon getIconByName(String name) { 
        for (int i=0; i< mIconList.length; i++) {
            if (mIconList[i].equals(name)) {
                return mIconList[i];
            }
        }
        return null;
    }
    
}
