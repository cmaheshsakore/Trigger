package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;

public class SoundPlayer extends AsyncTask<Boolean, Void, Void> {

    public static final boolean SET_LOOPING_ENABLED = true;
    public static final boolean SET_LOOPING_DISABLED = false;
    private Context mContext;
    private Uri mUri;
    private MediaPlayer mPlayer;

    public SoundPlayer(Context context, Uri uri) {
        this.mContext = context;
        this.mUri = uri;
    }
    
    public void setLooping(boolean shouldLoop) {
        if (mPlayer != null) {
            mPlayer.setLooping(shouldLoop);
        }
    }
    public void stopPlayback() {
        if (isPlaying()) {
           new ModifyPlaybackTask(mPlayer).execute(ModifyPlaybackTask.PLAYBACK_STOP);
        }
    }

    public void startPlayback() {
        if (!isPlaying()) {
            new ModifyPlaybackTask(mPlayer).execute(ModifyPlaybackTask.PLAYBACK_START);
         }
    }
    
    public boolean isPlaying() {
        return (mPlayer == null) ? false : mPlayer.isPlaying();
    }
    
    @Override
    protected Void doInBackground(Boolean... args) {

        if (mUri != null) {
            try {
                mPlayer = MediaPlayer.create(mContext, mUri);
                mPlayer.setLooping(args[0]);
                mPlayer.start();
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception playing notification sound", e);
            }

        }
        return null;

    }

    private class ModifyPlaybackTask extends AsyncTask<Integer, Void, Void> {

        public static final int PLAYBACK_STOP = 0;
        public static final int PLAYBACK_START = 1;
        private MediaPlayer mPlayer;
        
        public ModifyPlaybackTask(MediaPlayer player) {
            mPlayer = player;
        }
        @Override
        protected Void doInBackground(Integer... option) {
            if (option[0] == PLAYBACK_STOP) {
                mPlayer.stop();
            } else if (option[0] == PLAYBACK_START) {
                mPlayer.start();
            }
            return null;
        }
        
    }

}
