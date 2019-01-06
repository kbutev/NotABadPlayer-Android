package com.media.notabadplayer.Audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.math.MathUtils;
import android.util.Log;

import java.util.ArrayList;

public class AudioPlayer {
    private static AudioPlayer _singleton;
    
    private MediaPlayer _player;
    private Context _applicationContext;
    private MediaPlayerPlaylist _playlist;
    
    private ArrayList<MediaPlayerObserver> _observers;
    
    private AudioPlayer()
    {
        _player = new MediaPlayer();
        _player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onFinish();
            }
        });
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
    
    private void onPlay(MediaTrack track)
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerPlay(track);}
    }
  
    private void onFinish()
    {
        next();
    }

    private void onStop()
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerStop();}
    }
    
    public final MediaPlayer getPlayer()
    {
        return _player;
    }
    
    public void attachObserver(MediaPlayerObserver observer)
    {
        if (_observers.contains(observer))
        {
            return;
        }
        
        _observers.add(observer);
    }
    
    public void detachObserver(MediaPlayerObserver observer)
    {
        _observers.remove(observer);
    }
    
    public boolean isPlaying()
    {
        return _player.isPlaying();
    }
    
    public MediaPlayerPlaylist getPlaylist()
    {
        return _playlist;
    }

    public void playPlaylist(Context applicationContext, MediaPlayerPlaylist playlist)
    {
        _applicationContext = applicationContext;
        _playlist = playlist;
        
        playTrack(_playlist.getPlayingTrack());
    }
    
    private void playTrack(MediaTrack track)
    {
        if (track == null)
        {
            stop();
            return;
        }
        
        Uri path = Uri.parse(Uri.decode(track.filePath));

        _player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            _player.reset();
            _player.setDataSource(_applicationContext, path);
            _player.prepare();
            _player.start();
            
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing track: " + track.title);
            
            onPlay(track);
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Cannot play track: " + e.toString());
        }
    }
    
    public void resume()
    {
        try
        {
            if (!_player.isPlaying())
            {
                _player.start();
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Cannot resume: " + e.toString());
        }
    }

    public void pause()
    {
        try
        {
            if (_player.isPlaying())
            {
                _player.pause();
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Cannot pause: " + e.toString());
        }
    }
    
    public void stop()
    {
        try
        {
            _player.stop();
        }
        catch (Exception e)
        {
            
        }
    }
    
    public void next()
    {
        _playlist.goToNextPlayingTrack();

        if (_playlist.getPlayingTrack() == null)
        {
            onStop();
        }
        else
        {
            playTrack(_playlist.getPlayingTrack());
        }
    }

    public void previous()
    {
        _playlist.goToNextPlayingTrack();
        
        if (_playlist.getPlayingTrack() == null)
        {
            onStop();
        }
        else
        {
            playTrack(_playlist.getPlayingTrack());
        }
    }

    public void shuffle()
    {
        _playlist.goToTrackByShuffle();
    }
    
    public void pauseOrResume()
    {
        if (!_player.isPlaying())
        {
            resume();
        }
        else
        {
            pause();
        }
    }

    public void jumpBackwards(int msec)
    {
        int duration = _player.getDuration() / 1000;
        int currentPosition = _player.getCurrentPosition() / 1000;
        int destination = currentPosition - msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }

    public void jumpForwards(int msec)
    {
        int duration = _player.getDuration() / 1000;
        int currentPosition = _player.getCurrentPosition() / 1000;
        int destination = currentPosition + msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }
    
    public void seekTo(int msec)
    {
        msec *= 1000;
        
        int destination = msec;
        
        try
        {
            if (destination < _player.getDuration())
            {
                _player.seekTo(msec);
            }
            else
            {
                _player.seekTo(0);
            }

            Log.v(AudioPlayer.class.getCanonicalName(), "Seek to " + String.valueOf(destination));
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Cannot seek to: " + e.toString());
        }
    }

    public void volumeUp()
    {

    }

    public void volumeDown()
    {

    }

    public void muteOrUnmute()
    {

    }
}
