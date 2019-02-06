package com.media.notabadplayer.Audio;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.math.MathUtils;
import android.util.Log;

import java.util.ArrayList;

public class AudioPlayer {
    private static AudioPlayer _singleton;
    
    private android.media.MediaPlayer _player;
    private Application _application;
    private AudioPlaylist _playlist;
    
    private ArrayList<AudioPlayerObserver> _observers;
    
    private AudioPlayer()
    {
        _player = new android.media.MediaPlayer();
        _player.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                onFinish();
                playNextBasedOnPlayOrder();
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
    
    private Context getContext()
    {
        return _application.getApplicationContext();
    }
    
    private void onPlay(AudioTrack track)
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerPlay(track);}
    }
  
    private void onFinish()
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerFinish();}
    }
    
    private void onStop()
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerStop();}
    }
    
    private void onResume(AudioTrack track)
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerResume(track);}
    }
    
    private void onPause(AudioTrack track)
    {
        for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerPause(track);}
    }
    
    public final android.media.MediaPlayer getPlayer()
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
    
    public boolean isPlaying()
    {
        return _player.isPlaying();
    }
    
    public AudioPlaylist getPlaylist()
    {
        return _playlist;
    }
    
    public boolean hasPlaylist() {return _playlist != null;}
    
    public void playPlaylist(Application application, @NonNull AudioPlaylist playlist)
    {
        AudioPlayOrder order = _playlist != null ? _playlist.getPlayOrder() : playlist.getPlayOrder();
        
        _application = application;
        _playlist = playlist;
        _playlist.setPlayOrder(order);
        
        playTrack(_playlist.getPlayingTrack());
    }
    
    private void playTrack(AudioTrack track)
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        if (track == null)
        {
            stop();
            return;
        }
        
        Uri path = Uri.parse(Uri.decode(track.filePath));
        
        _player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            _player.reset();
            _player.setDataSource(getContext(), path);
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
        if (!hasPlaylist())
        {
            return;
        }
        
        // Start, instead of resuming
        if (!_playlist.isPlaying())
        {
            playTrack(_playlist.getPlayingTrack());
            
            return;
        }
        
        try
        {
            if (!_player.isPlaying())
            {
                _player.start();
                
                onResume(_playlist.getPlayingTrack());
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Cannot resume: " + e.toString());
        }
    }

    public void pause()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        try
        {
            if (_player.isPlaying())
            {
                _player.pause();
                
                onPause(_playlist.getPlayingTrack());
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Cannot pause: " + e.toString());
        }
    }
    
    public void stop()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        try
        {
            seekTo(0);
            _player.stop();
            
            onStop();
        }
        catch (Exception e)
        {
            
        }
    }
    
    public void playNext()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        _playlist.goToNextPlayingTrack();
        
        if (!_playlist.isPlaying())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");
            
            stop();
            
            onStop();
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Play next track " + _playlist.getPlayingTrack().title);
            
            playTrack(_playlist.getPlayingTrack());
        }
    }
    
    public void playPrevious()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        _playlist.goToPreviousPlayingTrack();
        
        if (!_playlist.isPlaying())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, cannot go before first track");

            stop();
            
            onStop();
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Play previous track " + _playlist.getPlayingTrack().title);
            
            playTrack(_playlist.getPlayingTrack());
        }
    }
    
    public void playNextBasedOnPlayOrder()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        _playlist.goToTrackBasedOnPlayOrder();
        
        if (!_playlist.isPlaying())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            onStop();
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Play next track " + _playlist.getPlayingTrack().title);

            playTrack(_playlist.getPlayingTrack());
        }
    }

    public void shuffle()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        _playlist.goToTrackByShuffle();
    }
    
    public void pauseOrResume()
    {
        if (!hasPlaylist())
        {
            return;
        }
        
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
        if (!hasPlaylist())
        {
            return;
        }
        
        int duration = _player.getDuration() / 1000;
        int currentPosition = _player.getCurrentPosition() / 1000;
        int destination = currentPosition - msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }

    public void jumpForwards(int msec)
    {
        if (!hasPlaylist())
        {
            return;
        }
        
        int duration = _player.getDuration() / 1000;
        int currentPosition = _player.getCurrentPosition() / 1000;
        int destination = currentPosition + msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }
    
    public void seekTo(int msec)
    {
        if (!hasPlaylist())
        {
            return;
        }
        
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
        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int incrementVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10;
        int result = currentVolume + incrementVolume;
        
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, result,0);
    }

    public void volumeDown()
    {
        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int incrementVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10;
        int result = currentVolume - incrementVolume > 0 ? currentVolume - incrementVolume : 0;
        
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, result,0);
    }

    public void muteOrUnmute()
    {
        
    }
}
