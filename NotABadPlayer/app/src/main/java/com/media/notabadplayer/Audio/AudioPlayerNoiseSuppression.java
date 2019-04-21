package com.media.notabadplayer.Audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;

public class AudioPlayerNoiseSuppression extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            KeyBinds.getShared().evaluateInput(ApplicationInput.EARPHONES_UNPLUG);
        }
    }
}
