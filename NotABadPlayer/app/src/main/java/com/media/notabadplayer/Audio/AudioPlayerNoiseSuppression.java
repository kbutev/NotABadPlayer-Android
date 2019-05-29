package com.media.notabadplayer.Audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;

public class AudioPlayerNoiseSuppression extends BroadcastReceiver implements AudioManager.OnAudioFocusChangeListener {
    
    public void start(@NonNull Context context)
    {
        context.registerReceiver(this, new IntentFilter(ACTION_AUDIO_BECOMING_NOISY));

        AudioManager am =(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
    
    @Override
    public void onReceive(@NonNull Context context, Intent intent)
    {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            KeyBinds.getShared().evaluateInput(ApplicationInput.EARPHONES_UNPLUG);
        }
    }
    
    @Override
    public void onAudioFocusChange(int focusChange) 
    {
        switch (focusChange) {
            case (AudioManager.AUDIOFOCUS_LOSS):
                KeyBinds.getShared().evaluateInput(ApplicationInput.EARPHONES_UNPLUG);
                break;
            case (AudioManager.AUDIOFOCUS_GAIN):
                KeyBinds.getShared().evaluateInput(ApplicationInput.EXTERNAL_PLAY);
                break;
            default:
                break;
        }
    }
}
