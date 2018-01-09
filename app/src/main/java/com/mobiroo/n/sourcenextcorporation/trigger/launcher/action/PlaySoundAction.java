package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

public class PlaySoundAction extends BaseAction implements OnClickListener {

    public int mPosition = -1;
    public String mName = "";
    public Uri mUri = null;
    private Context mContext;
    private View mView;

    public static final int ACTIVITY_PICK_SOUND = 18;


    public void setData(String text, int position, Uri uri) {
        ((TextView) mView.findViewById(R.id.RingtoneName)).setText(text);
        mName = text;
        mPosition = position;
        mUri = uri;
    }

    @Override
    public String getCommand() {
        return Constants.COMMAND_PLAY_SOUND;
    }

    @Override
    public String getCode() {
        return Codes.PLAY_SOUND;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        mContext = context;
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option084, null, false);

        RingtoneManager rm = new RingtoneManager(context);
        Uri defRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        mPosition = rm.getRingtonePosition(defRingtone);

        mName = context.getString(R.string.ringerTypeSilent);
        TextView ringtoneTV = (TextView) dialogView.findViewById(R.id.RingtoneName);
        ringtoneTV.setText(mName);

        LinearLayout selectRingtoneButton = (LinearLayout) dialogView.findViewById(R.id.RingerSoundsButton);
        selectRingtoneButton.setBackgroundResource(android.R.drawable.list_selector_background);

        selectRingtoneButton.setOnClickListener(this);

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            mPosition = Integer.parseInt(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            mName = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO);
            ((TextView) dialogView.findViewById(R.id.RingtoneName)).setText(mName);
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE);
            if (!value.isEmpty()) {
                try {
                    mUri = Uri.parse(value);
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
        String message = Constants.COMMAND_PLAY_SOUND + ":" + mPosition + ":" + mName;
        if (mUri != null) {
            message += ":" + Utils.encodeData(mUri.toString());
        }
        return new String[]{message, context.getString(R.string.play_sound), mName};
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
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseEncodedString(args, 2, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.tryParseEncodedString(args, 3, ""))
        );
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        // Current URI
        try {
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALL));
        } catch (Exception e) {
        }
        ((Activity) mContext).startActivityForResult(i, ACTIVITY_PICK_SOUND);
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        int type = Utils.tryParseInt(args, 1, 1);
        mName = Utils.tryParseEncodedString(args, 2, "");
        String uriString = Utils.tryParseEncodedString(args, 3, "");
        Logger.d("PlaySound: Uri is " + uriString);

        if (uriString.isEmpty()) {
            return;
        }

        Uri uri = Uri.parse(uriString);

        try {
            final MediaPlayer p = MediaPlayer.create(context, uri);
            p.setLooping(false);
            p.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    p.release();
                }
            });
            p.start();
        } catch (Exception e) {
            Logger.e("Exception playin sound: " + e, e);
        }

    }


    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetToText(context, operation, context.getString(R.string.play_sound), Utils.tryParseEncodedString(mArgs, 2, ""));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetToText(context, operation, context.getString(R.string.play_sound), Utils.tryParseEncodedString(mArgs, 2, ""));
    }


}
