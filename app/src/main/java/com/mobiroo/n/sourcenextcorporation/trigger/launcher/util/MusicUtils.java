package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;

import java.util.Iterator;
import java.util.List;

public class MusicUtils {


    public static List<ResolveInfo> getMediaReceivers(Context context) {
        return context.getPackageManager().queryBroadcastReceivers(new Intent("android.intent.action.MEDIA_BUTTON"), 96);
    }
    public static void sendKeyCodeToReceiver(Context context, int keycode, BroadcastReceiver receiver) {
        sendKeyCodeToReceiver(context, keycode, receiver, true, true);
        
    }
    
    public static void sendKeyCodeToReceiver(Context context, int keycode, BroadcastReceiver receiver, boolean useComponentIfDataPresent, boolean dispatchBoth) {
        Logger.d("Sending keycode " + keycode);
        long event_time = SystemClock.uptimeMillis();
        String preferredPackage = SettingsHelper.getPrefString(context, Constants.PREF_DESIRED_MEDIA_PACKAGE, "");
        
        ResolveInfo info = null;
        Iterator<ResolveInfo> localIterator = MusicUtils.getMediaReceivers(context).iterator();
        while (localIterator.hasNext())
        {
          ResolveInfo localResolveInfo = (ResolveInfo)localIterator.next();
          if (localResolveInfo.activityInfo.packageName.equals(preferredPackage)) {
              info = localResolveInfo;
              break;
          }
        }
        
        
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(event_time, event_time, KeyEvent.ACTION_DOWN, keycode, 0));
        
        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(event_time, event_time, KeyEvent.ACTION_UP, keycode, 0));
        
        if (useComponentIfDataPresent) {
            
            if (info != null) {
                Logger.d("Setting component to " + info.activityInfo.packageName + ", " + info.activityInfo.name);
                ComponentName componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                downIntent.setComponent(componentName);
                upIntent.setComponent(componentName);
                
                /*
                Intent localIntent = context.getPackageManager().getLaunchIntentForPackage(componentName.getPackageName());
                if (localIntent != null) {
                    localIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(localIntent);
                }*/
                
            }
            
            context.sendOrderedBroadcast(downIntent, null, receiver, null, -1, null, null);
            context.sendOrderedBroadcast(upIntent, null, receiver, null, -1, null, null);
            
        } else {
            Logger.d("Sending with no component info");
            context.sendOrderedBroadcast(downIntent, null);
            context.sendOrderedBroadcast(upIntent, null);
        }

    }

    public static void sendShushBroadcast(Context context) {
        Intent intent = new Intent("com.androidintents.PRE_RINGER_MODE_CHANGE");
        intent.putExtra("com.androidintents.EXTRA_SENDER", "com.mobiroo.n.sourcenextcorporation.trigger");
        try {
            context.sendBroadcast(intent);
        } catch (Exception e) {

        }
    }
}
