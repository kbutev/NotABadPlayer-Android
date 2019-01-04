package com.media.notabadplayer.Audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioPlayer {
    private static AudioPlayer _singleton;
    
    private MediaPlayer _player;
    
    private ArrayList<AudioPlayerObserver> _observers;
    
    private AudioPlayer()
    {
        _player = new MediaPlayer();
        _observers = new ArrayList<>();
    }
    
    public static synchronized AudioPlayer getShared()
    {
        if (_singleton == null)
        {
            _singleton = new AudioPlayer();
        }
        
        return _singleton;
    }
    
    private void onPlay(AudioTrack track)
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerPlay(track);}
    }
  
    private void onFinish()
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerStop();}
    }
    
    public final MediaPlayer getPlayer()
    {
        return _player;
    }
    
    public void attachObserver(AudioPlayerObserver observer)
    {
        if (_observers.contains(observer))
        {
            return;
        }
        
        _observers.add(observer);
    }
    
    public void detachObserver(AudioPlayerObserver observer)
    {
        _observers.remove(observer);
    }
    
    public void playTrack(Context applicationContext, AudioTrack track)
    {
        Uri path = Uri.parse(Uri.decode(track.filePath));
        
        _player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        try {
            _player.reset();
            _player.setDataSource(applicationContext, path);
            _player.prepare();
            _player.start();
            onPlay(track);
        }
        catch (Exception e)
        {
            
        }
    }
}
