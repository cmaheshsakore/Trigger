package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class RingerSoundAction extends BaseAction implements OnClickListener {

    public int ringerPosition = -1;
    public String name = "";
    public Uri ringerUri = null;
    private Context mContext;
    private View mView;
    
    public static final int ACTIVITY_PICK_RINGTONE = 11;
    
    
    public void setRingtoneData(String text, int position, Uri uri) {
        ((TextView) mView.findViewById(R.id.RingtoneName)).setText(text);
        name = text;
        ringerPosition = position;
        ringerUri = uri;
    }
    @Override
    public String getCommand() {
        return Constants.COMMAND_RINGER_SOUND;
    }

    @Override
    public String getCode() {
        return Codes.SOUND_RINGER_SOUND;
    }

    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        mContext = context;
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option019, null, false);

        RingtoneManager rm = new RingtoneManager(context);
        Uri defRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        ringerPosition = rm.getRingtonePosition(defRingtone);

        name = context.getString(R.string.ringerTypeSilent);
        TextView ringtoneTV = (TextView) dialogView.findViewById(R.id.RingtoneName);
        ringtoneTV.setText(name);

        LinearLayout selectRingtoneButton = (LinearLayout) dialogView.findViewById(R.id.RingerSoundsButton);
        selectRingtoneButton.setBackgroundResource(android.R.drawable.list_selector_background);
        
        selectRingtoneButton.setOnClickListener(this);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ringerPosition = Integer.parseInt(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            name = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO);
            ((TextView) dialogView.findViewById(R.id.RingtoneName)).setText(name);
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE);
            if (!value.isEmpty()) {
                try {
                    ringerUri = Uri.parse(value);
                } catch (Exception e) {
                    
                }
            }
        }
        
        mView = dialogView;
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Ringtone";
    }


    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_RINGER_SOUND + ":" + ringerPosition + ":" + name;
        if ((ringerPosition == -1) && (!context.getString(R.string.ringerTypeSilent).equals(name))) {
            // Also append URI
            if (ringerUri != null) {
                message += ":" + Utils.encodeData(ringerUri.toString());
            }
        }
        return new String[] {message, context.getString(R.string.listSoundRingtone), name};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listSoundRingtone);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "0")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.tryParseEncodedString(args, 3, ""))
                );
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        // Current URI
        try {
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE));
        } catch (Exception e) {}
        ((Activity) mContext).startActivityForResult(i, ACTIVITY_PICK_RINGTONE);
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        int position = Utils.tryParseInt(args, 1, 1);
        name = Utils.tryParseString(args,2,"");
        
        Logger.d("Setting ringer as: " + position + ", " + name);
        
        RingtoneManager rm = new RingtoneManager(context);
        rm.setType(RingtoneManager.TYPE_RINGTONE);

        Uri newUri = null; // Null can / will also indicate a desire for silent
        Uri currentUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);

        if (position != -1) {
            newUri = lookupUri(rm, position);

            // If the lookup fails, try falling back onto the encoded URI
            if (newUri == null) {
                // See if we got a possible URI here
                String temp = "";
                if (args.length > 3) {
                    try {
                        temp = Utils.decodeData(args[3]);
                        newUri = Uri.parse(temp);
                    } catch (Exception e) {
                        /* Fail silently */
                    }
                }

            }
        }

        Logger.d("Setting Ringtone URI as " + newUri);
        if ((newUri != null) || (newUri == null) && (position == -1)) {
            try {
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
            } catch (Exception e) {
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, currentUri);
                Logger.e("Exception setting Ringtone: " + e, e);
                showError(context);
            }
        } else {
            Logger.d("URI is null for non silent, re-setting current tone");
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, currentUri);
            showError(context);
        }
    }

    private Uri lookupUri(RingtoneManager manager, int position) {
        Uri newUri = null;
        // Query ringtone manager media store for name - prefer name over ID
        Cursor c = manager.getCursor();
        if (!name.isEmpty()) {
            while(c.moveToNext()) {
                String title = c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                if (title.equals(name)) {
                        /* This is our desired tone, get the URI */
                    String uri = c.getString(RingtoneManager.URI_COLUMN_INDEX);
                    Logger.d("URI mBaseView " + name + " is " + uri);
                    String id = c.getString(RingtoneManager.ID_COLUMN_INDEX);
                    Logger.d("Tone ID is " + id);
                    newUri = Uri.parse(Utils.scrubURI(uri + "/" + id, id));
                }
            }
        }
        else {
                /* Fallback to legacy method of lookin up ID in media store */
            if (position != -1) {
                newUri = manager.getRingtoneUri(position);

                if (newUri == null) {
                    c = manager.getCursor();
                    c.moveToPosition(position);

                    String uri = "content://media/internal/audio/media";
                    Logger.d("Setting default URI prefix to " + uri);

                    uri = c.getString(RingtoneManager.URI_COLUMN_INDEX);
                    Logger.d("URI mBaseView " + name + " is " + uri);
                    String id = c.getString(RingtoneManager.ID_COLUMN_INDEX);
                    Logger.d("Tone ID is " + id);
                    newUri = Uri.parse(Utils.scrubURI(uri + "/" + id, id));
                }
            }
        }

        c.close();

        return newUri;
    }
    private void showError(Context context) {

    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetToText(context, operation, context.getString(R.string.soundOptionsRingtone), Utils.tryParseEncodedString(mArgs, 2, ""));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetToText(context, operation, context.getString(R.string.soundOptionsRingtone), Utils.tryParseEncodedString(mArgs, 2, ""));
    }

    
}
