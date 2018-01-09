package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.util.WakeUtils;

import java.util.HashMap;
import java.util.Locale;

public class TtsWorker extends BaseWorkerActivity implements OnInitListener{

    public static final String      EXTRA_TTS_MESSAGE = "ttsworker_extra_tts_message";
    public static final String      EXTRA_TTS_RESULT = "ttsworker_extra_result";
    
    private String                  mTtsText;
    private TextToSpeech            mTts;
    private HashMap<String, String> mParams         = new HashMap<String, String>();
    private int                     mCurrentVolume;
    private int                     mStream         = AudioManager.STREAM_MUSIC;
    private AudioManager            mManager;
    private boolean                 mChangedVolume  = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tag_empty);
        
        mTtsText = getIntent().getStringExtra(EXTRA_TTS_MESSAGE);
        mParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");

        if ((mTtsText != null) && (!mTtsText.isEmpty())) {
            WakeUtils.getInstance(this).acquireWakeLock(this, "nfctl-tts");
            mManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            checkVolume();
            mTts = new TextToSpeech(this, this);
        } else {
            finishWithFailure("No TTS Text found");
        }
    }

    @Override
    public void onDestroy() {

        if (mChangedVolume) {
            // Restore stream volume if modified
            mManager.setStreamVolume(mStream, mCurrentVolume, 0);
        }
        
        if(mTts != null) {
            mTts.shutdown();
            Logger.d("TTS: Destroyed");
        }
        super.onDestroy();
    }


    @Override
    public void onInit(int status) {
        Logger.i("TTS Init Status " + status);
        switch(status) {
            case TextToSpeech.SUCCESS:
                boolean success = setupTtsLanguage(Locale.getDefault());
                if (!success) {
                    Logger.d("TTS: Missing Language or unsupported language using locale.  Trying English");
                    success = setupTtsLanguage(Locale.ENGLISH);
                }

                if (success) {
                    // TTS should have successfully set a language now
                    setupProgressListener();
                    speakText();
                } else {
                    Logger.d("TTS: Failed to set a language for use, exiting");
                    finishWithFailure(getString(R.string.tts_failed));
                }
                break;
            default:
                Logger.d("TTS: Init Failed, finishing");
                finishWithFailure(getString(R.string.tts_failed));
                break;

        }
    }

    @SuppressWarnings("deprecation")
    private void setupProgressListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    WakeUtils.getInstance(TtsWorker.this).releaseWakeLock();
                    resumeProcessing(RESULT_OK);
                }

                @Override
                public void onError(String utteranceId) {
                    finishWithFailure(getString(R.string.tts_failed));
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });
        } else {
            mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String arg0) {
                    WakeUtils.getInstance(TtsWorker.this).releaseWakeLock();
                    resumeProcessing(RESULT_OK);
                }
            });
        }

    }
    

    private void finishWithFailure(String message) {
        WakeUtils.getInstance(TtsWorker.this).releaseWakeLock();
        Intent data = new Intent();
        data.putExtra(EXTRA_TTS_RESULT, message);
        resumeProcessing(RESULT_CANCELED, data);
    }


    private boolean setupTtsLanguage(Locale locale) {
        Logger.d("TTS: Setting tts locale");
        int result = mTts.setLanguage(locale);
        return (!(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED));
    }
    
    private void speakText() {
        mTts.speak(mTtsText, TextToSpeech.QUEUE_FLUSH, mParams);
    }
    
    private void checkVolume() {
        mCurrentVolume = mManager.getStreamVolume(mStream);
        if (mCurrentVolume == 0) {
            /* Temporarily turn up the volume for TTS */
            mManager.setStreamVolume(mStream, mManager.getStreamMaxVolume(mStream), 0);
            mChangedVolume = true;
        }
    }
}
