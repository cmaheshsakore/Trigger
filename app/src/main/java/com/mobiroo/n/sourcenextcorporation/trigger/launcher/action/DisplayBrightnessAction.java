package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerBrightness;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

public class DisplayBrightnessAction extends BaseAction {

    public static final int REQUEST_BRIGHTNESS = 6;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SET_BRIGHTNESS;
    }

    @Override
    public String getCode() {
        return Codes.DISPLAY_BRIGHTNESS;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option025, null, false);

        int curBrightness = 0;
        try {
            curBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            Logger.d("Current Brightness is " + curBrightness);
        } catch (Settings.SettingNotFoundException e) {
            Logger.e("NFCT", "Couldn't find brightness setting");
        }
        int curMode = 0;
        try {
            curMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            Logger.e("NFCT", "Couldn't find brightness Mode setting");
        }

        SeekBar brightBar = (SeekBar) dialogView.findViewById(R.id.BrightnessLevelSeek);
        brightBar.setMax(100);
        curBrightness = (curBrightness* 100) / 255;
        brightBar.setProgress(curBrightness);

        final TextView sliderValue = (TextView) dialogView.findViewById(R.id.sliderLevel);
        sliderValue.setText(String.valueOf(curBrightness));
        
        brightBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    sliderValue.setText(String.valueOf(progress));
                } catch (Exception e) {
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        
        CheckBox bBox = (CheckBox) dialogView.findViewById(R.id.brightnessMode);
        if (curMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            bBox.setChecked(true);
        
        /* Check for any pre-populated arguments */
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            // This defines the Brightness valu
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            brightBar.setProgress(Integer.parseInt(state));
            sliderValue.setText(String.valueOf(state));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_BRIGHTNESS_AUTO_ENABLED)) {
            String state = arguments.getValue(CommandArguments.OPTION_BRIGHTNESS_AUTO_ENABLED);
            boolean checked = (state.equals("1")) ? true : false;
            bBox.setChecked(checked);
        }


        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            String state = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE);
            boolean checked = (state.equals("1")) ? true : false;
            ((CheckBox) dialogView.findViewById(R.id.showWindow)).setChecked(checked);
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Display Brightness";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        SeekBar brightnessBar = (SeekBar) actionView.findViewById(R.id.BrightnessLevelSeek);
        int curPosition = brightnessBar.getProgress();

        CheckBox bBox = (CheckBox) actionView.findViewById(R.id.brightnessMode);
        int bMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        if (bBox.isChecked())
            bMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;

        int show = ((CheckBox) actionView.findViewById(R.id.showWindow)).isChecked() ? 1 : 0;
        String message = Constants.COMMAND_SET_BRIGHTNESS + ":" + curPosition + ":" + bMode + ":" + show;

        if (bMode == 0) {
            return new String[] { message, context. getString(R.string.listDisplayBrightness), "" + curPosition};
        } else {
            return new String[] { message, context.getString(R.string.listDisplayBrightnessAuto), ""};
        }
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listDisplayBrightness);
        try {
            String mode = args[1];
            if (mode.equals("1")) {
                display = context.getString(R.string.listDisplayBrightnessAuto);
            } else {
                display += " " + args[0];
            }
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, Utils.tryParseString(args, 1, "100")),
                new BasicNameValuePair(CommandArguments.OPTION_BRIGHTNESS_AUTO_ENABLED, Utils.tryParseString(args, 2, "1")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.tryParseString(args, 3, "1"))
                );
    }

    private int DEFAULT_VALUE = -1;

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Setting brightness");
        int level = Utils.tryParseInt(args, 1, DEFAULT_VALUE);
        int mode = Utils.tryParseInt(args, 2, DEFAULT_VALUE);
        int show = Utils.tryParseInt(args, 3, DEFAULT_VALUE);

        if ((level != DEFAULT_VALUE) && (mode != DEFAULT_VALUE)) {
            level = setBrightness(context, level, mode);

            if (show == 1) {
                setAutoRestart(currentIndex + 1);
                Intent intent = new Intent(context, workerBrightness.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(workerBrightness.EXTRA_MODE, mode);
                intent.putExtra(workerBrightness.EXTRA_LEVEL, level);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent = buildReturnIntent(intent);
                context.startActivity(intent);
            }
        }
        
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetText(context, operation, context.getString(R.string.layoutDisplayBrightnessLevel));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetText(context, operation, context.getString(R.string.layoutDisplayBrightnessLevel));
    }

    public int setBrightness(Context context, int level, int mode) {

        Logger.d("Setting brightness to %s mode %s", String.valueOf(level), String.valueOf(mode));

        // set brightness level
        if (Utils.isLgDevice()) {
            Logger.d("Found LG device");
            level = calculateLGBrightness(level);
            if (level < LG_LOWER_BOUND) {
                level = LG_LOWER_BOUND;
            }
        } else {
            level = Math.round(level * 255 / 100);
            if (level == 0) {
                level = 1;
            }
        }

        if (level > 255) {
            level = 255;
        }

        Logger.d("Setting brightness to " + level);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, level);

        // Set brightness mode
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);

        if(!Utils.isAsusDevice()) {
            // Set an auto adjust mode from -1.0 to 1.0
            Settings.System.putFloat(context.getContentResolver(), "screen_auto_brightness_adj", (float) ((level / 127.5) - 1.0));
        } else {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                // For ASUS devices with Android 6.0+ i.e. (API 23+) brightness range is between [0, 2].
                Settings.System.putFloat(context.getContentResolver(), "screen_auto_brightness_adj", (float) ((level / 127.5)));
            }
        }

        return level;
    }

    /* Set some default LG values */
    private int     LG_MINIMUM          = 110;
    private double  LG_STEP             = 1.45;
    private int     LG_LOWER_BOUND      = 128;

    /*
     * LG LS970
     * 110 offset, 1.4 per step
     * Set to new default 10/10/2013
     */

    private int     LG_LS970_MINIMUM    = 110;
    private double  LG_LS970_STEP       = 1.45;
    private int     LG_LS970_LOWER_BOUND = 128;

    /*  LG Optimus L9
    *    0%   20
    *   10%   45
    *   20%   69
    *   30%   92
    *   40%   116
    *   50%   138
    *   60%   162
    *   70%   185
    *   80%   208
    *   90%   233
    *  100%   255
    *
    *  20 offset, 2.35 per step
    */

    private int     LG_L9_MINIMUM       = 20;
    private double  LG_L9_STEP          = 2.35;
    private int     LG_L9_LOWER_BOUND   = 20;

    /*
    G2
    100=255
    90=235
    80=215
    70=195
    60=175
    50=154
    40=134
    30=112
    20=92
    10=72
    0=50
     */

    private int     LG_G2_MINIMUM       = 50;
    private int     LG_G2_LOWER_BOUND   = 20;
    private double  LG_G2_STEP          = 2.05;

    private int calculateLGBrightness(int brightnessPercentage) {

        Logger.d("Calculating LG brightness for " + Build.MODEL);
        if (Build.MODEL.equalsIgnoreCase("LG-D800")
                || Build.MODEL.equalsIgnoreCase("LG-D801")
                || Build.MODEL.equalsIgnoreCase("LG-LS980")
                || Build.MODEL.equalsIgnoreCase("LG-VS980")
                ) {
            Logger.d("Setting G2 values");
            LG_MINIMUM = LG_G2_MINIMUM;
            LG_STEP = LG_G2_STEP;
            LG_LOWER_BOUND = LG_G2_LOWER_BOUND;

        }
        else if (Build.MODEL.equalsIgnoreCase("LS-970")) {
            Logger.d("Setting LS970 values");
            LG_MINIMUM = LG_LS970_MINIMUM;
            LG_STEP = LG_LS970_STEP;
            LG_LOWER_BOUND = LG_LS970_LOWER_BOUND;
        } else {
            Logger.d("Setting L9 values");
            LG_MINIMUM = LG_L9_MINIMUM;
            LG_STEP = LG_L9_STEP;
            LG_LOWER_BOUND = LG_L9_LOWER_BOUND;

        }

        return (int) (LG_MINIMUM + Math.round(brightnessPercentage * LG_STEP));
    }
}
