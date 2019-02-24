package com.media.notabadplayer.Audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class AudioPlayerNoiseSuppression extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            AudioPlayer.getShared().pause();
        }
    }
}
