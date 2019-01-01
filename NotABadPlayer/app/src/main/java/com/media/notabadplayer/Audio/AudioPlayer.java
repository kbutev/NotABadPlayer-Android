package com.media.notabadplayer.Audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

public class AudioPlayer {
    private static AudioPlayer _singleton;
    
    private MediaPlayer _player;
    
    private AudioPlayerObserver _observer;
    
    private AudioPlayer()
    {
        
    }
    
    public static synchronized AudioPlayer getShared()
    {
        if (_singleton == null)
        {
            _singleton = new AudioPlayer();
        }
        
        return _singleton;
    }
    
    public boolean hasObserver()
    {
        return _observer != null;
    }
    
    public void attachObserver(AudioPlayerObserver observer)
    {
        _observer = observer;
    }
    
    public void detachObserver()
    {
        _observer = null;
    }
    
    public void playTrack(Context applicationContext, AudioTrack track)
    {
        Uri path = Uri.parse(Uri.decode(track.filePath));
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        try {
            mediaPlayer.setDataSource(applicationContext, path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        catch (Exception e)
        {
            
        }
    }
}
