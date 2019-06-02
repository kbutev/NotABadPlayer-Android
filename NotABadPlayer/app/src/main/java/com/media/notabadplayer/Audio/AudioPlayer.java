package com.media.notabadplayer.Audio;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.util.Log;
import java.util.ArrayList;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;

public class AudioPlayer {
    private static AudioPlayer _singleton;
    
    private android.media.MediaPlayer _player;
    private Application _application;
    private AudioInfo _audioInfo;
    private AudioPlaylist _playlist;
    private AudioPlayOrder _playOrder = AudioPlayOrder.FORWARDS;
     
    public final Observers observers = new Observers();
    public final PlayHistory playHistory = new PlayHistory();
    
    private boolean _muted;
    
    private AudioPlayer()
    {
        _player = new android.media.MediaPlayer();
        _player.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                observers.onFinish();
                playNextBasedOnPlayOrder();
            }
        });
        
        _player.setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                observers.onFinish();
                return true;
            }
        });

        _muted = false;
    }
    
    public static synchronized AudioPlayer getShared()
    {
        if (_singleton == null)
        {
            _singleton = new AudioPlayer();
        }
        
        return _singleton;
    }
    
    public boolean isInitialized()
    {
        return _application != null;
    }

    private void checkIfPlayerIsInitialized()
    {
        if (!isInitialized())
        {
            throw new UncheckedExecutionException(new Exception("AudioPlayer is not initialized, initialize() has never been called"));
        }
    }

    private Context getContext()
    {
        checkIfPlayerIsInitialized();
        
        return _application;
    }
    
    public @NonNull AudioInfo getAudioInfo()
    {
        checkIfPlayerIsInitialized();
        
        return _audioInfo;
    }
    
    public final android.media.MediaPlayer getPlayer()
    {
        return _player;
    }
    
    public void initialize(@NonNull Application application, @NonNull AudioInfo audioInfo)
    {
        if (_application != null)
        {
            throw new UncheckedExecutionException(new Exception("Must not call initialize() twice"));
        }
        
        Log.v(AudioPlayer.class.getCanonicalName(), "Initializing...");
        
        _application = application;
        _audioInfo = audioInfo;
        
        Log.v(AudioPlayer.class.getCanonicalName(), "Initialized!");
    }
    
    public boolean isPlaying()
    {
        return _player.isPlaying();
    }
    
    public boolean isCompletelyStopped()
    {
        return !_playlist.isPlaying();
    }
    
    public @Nullable AudioPlaylist getPlaylist()
    {
        return _playlist;
    }
    
    public boolean hasPlaylist() {return _playlist != null;}
    
    public AudioPlayOrder getPlayOrder()
    {
        return _playOrder;
    }
    
    public void setPlayOrder(AudioPlayOrder order)
    {
        _playOrder = order;
        
        observers.onPlayOrderChange(order);
    }
    
    public void playPlaylist(@NonNull AudioPlaylist playlist) throws Exception
    {
        checkIfPlayerIsInitialized();

        AudioTrack previousTrack = _playlist != null ? _playlist.getPlayingTrack() : null;
        
        try {
            playTrack(playlist.getPlayingTrack(), previousTrack, true);
        } catch (Exception e) {
            stop();
            throw e;
        }

        _playlist = playlist;
        _playlist.playCurrent();
    }

    private void playTrack(@NonNull AudioTrack newTrack, boolean usePlayHistory) throws Exception
    {
        playTrack(newTrack, null, usePlayHistory);
    }
    
    private void playTrack(@NonNull AudioTrack newTrack, AudioTrack previousTrack, boolean usePlayHistory) throws Exception
    {
        checkIfPlayerIsInitialized();
        
        Uri path = Uri.parse(Uri.decode(newTrack.filePath));
        
        _player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        try {
            _player.reset();
            _player.setDataSource(getContext(), path);
            _player.prepare();
            _player.start();
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play track, " + e.toString());

            if (previousTrack != null)
            {
                Uri pathOfPreviousTrack = Uri.parse(Uri.decode(previousTrack.filePath));

                try {
                    _player.reset();
                    _player.setDataSource(getContext(), pathOfPreviousTrack);
                    _player.prepare();
                    _player.start();
                } catch (Exception e2) {
                    _playlist = null;
                }
            }
            
            stop();
            throw e;
        }
        
        Log.v(AudioPlayer.class.getCanonicalName(), "Playing track: " + newTrack.title);
        
        if (usePlayHistory)
        {
            playHistory.addTrack(newTrack);
        }
        
        observers.onPlay(newTrack);
    }
    
    public void resume()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }
        
        try
        {
            if (!isPlaying())
            {
                _player.start();

                observers.onResume(_playlist.getPlayingTrack());
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot resume, " + e.toString());
        }
    }

    public void pause()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }
        
        try
        {
            if (isPlaying())
            {
                _player.pause();

                observers.onPause(_playlist.getPlayingTrack());
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot pause, " + e.toString());
        }
    }
    
    public void stop()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }
        
        try
        {
            _player.seekTo(0);
            
            if (isPlaying())
            {
                _player.pause();

                observers.onStop();
            }
        }
        catch (Exception e)
        {
            
        }
    }

    public void pauseOrResume()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }

        if (!isPlaying())
        {
            resume();
        }
        else
        {
            pause();
        }
    }
    
    public void playNext()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }

        _playlist.goToNextPlayingTrack();
        
        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing next track...");

            try {
                playTrack(_playlist.getPlayingTrack(), true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play next, " + e.toString());
                stop();
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            observers.onStop();
        }
    }
    
    public void playPrevious()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }

        _playlist.goToPreviousPlayingTrack();
        
        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing previous track...");

            try {
                playTrack(_playlist.getPlayingTrack(), true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play previous, " + e.toString());
                stop();
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, cannot go before first track");

            stop();

            observers.onStop();
        }
    }
    
    public void playNextBasedOnPlayOrder()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }

        _playlist.goToTrackBasedOnPlayOrder(_playOrder);
        
        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing next track based on play order...");

            try {
                playTrack(_playlist.getPlayingTrack(), true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play next based on play order, " + e.toString());
                stop();
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            observers.onStop();
        }
    }

    public void shuffle()
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }
        
        _playlist.goToTrackByShuffle();

        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing random track...");
            
            try {
                playTrack(_playlist.getPlayingTrack(), true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play random, " + e.toString());
                stop();
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            observers.onStop();
        }
    }

    public void jumpBackwards(int msec)
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }

        int duration = getDurationMSec();
        int currentPosition = getCurrentPositionMSec();
        int destination = currentPosition - msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }

    public void jumpForwards(int msec)
    {
        checkIfPlayerIsInitialized();

        if (!hasPlaylist())
        {
            return;
        }
        
        int duration = getDurationMSec();
        int currentPosition = getCurrentPositionMSec();
        int destination = currentPosition + msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }

    public int getDurationMSec()
    {
        checkIfPlayerIsInitialized();

        return _player.getDuration() / 1000;
    }
    
    public int getCurrentPositionMSec()
    {
        checkIfPlayerIsInitialized();

        return _player.getCurrentPosition() / 1000;
    }
    
    public void seekTo(int msec)
    {
        checkIfPlayerIsInitialized();

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
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot seek to, " + e.toString());
        }
    }
    
    public int getVolume()
    {
        checkIfPlayerIsInitialized();

        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        double max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        
        double result = (manager.getStreamVolume(AudioManager.STREAM_MUSIC) / max) * 100;
        return (int)result;
    }

    public void setVolume(int volume)
    {
        checkIfPlayerIsInitialized();

        if (volume < 0)
        {
            volume = 0;
        }
        
        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        
        double v = (double)volume;
        double max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        double result = (v / 100.0) * max;
        result = result > max ? max : result;
        
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)result,0);
    }
    
    public void volumeUp()
    {
        checkIfPlayerIsInitialized();

        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int incrementVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10;
        int result = currentVolume + incrementVolume;
        
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, result,0);
    }

    public void volumeDown()
    {
        checkIfPlayerIsInitialized();

        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int incrementVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10;
        int result = currentVolume - incrementVolume > 0 ? currentVolume - incrementVolume : 0;
        
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, result,0);
    }
    
    public boolean isMuted()
    {
        return _muted;
    }
    
    public void muteOrUnmute()
    {
        checkIfPlayerIsInitialized();

        if (!_muted)
        {
            mute();
        }
        else
        {
            unmute();
        }
    }

    public void mute()
    {
        checkIfPlayerIsInitialized();

        if (!_muted)
        {
            _player.setVolume(0, 0);

            _muted = true;

            Log.v(AudioPlayer.class.getCanonicalName(), "Mute");
        }
    }

    public void unmute()
    {
        checkIfPlayerIsInitialized();

        if (_muted)
        {
            _player.setVolume(1, 1);

            _muted = false;

            Log.v(AudioPlayer.class.getCanonicalName(), "Unmute");
        }
    }

    public class Observers
    {
        private ArrayList<AudioPlayerObserver> _observers = new ArrayList<>();
        
        public void attach(AudioPlayerObserver observer)
        {
            if (_observers.contains(observer))
            {
                return;
            }

            _observers.add(observer);

            fullyUpdateObserver(observer);
        }

        public void detach(AudioPlayerObserver observer)
        {
            _observers.remove(observer);
        }

        public void fullyUpdateObserver(AudioPlayerObserver observer)
        {
            observer.onPlayOrderChange(_playOrder);
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

        private void onPlayOrderChange(AudioPlayOrder order)
        {
            for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayOrderChange(order);}
        }
    }

    public class PlayHistory
    {
        private ArrayList<AudioTrack> _playHistory = new ArrayList<>();
        
        public @NonNull ArrayList<AudioTrack> getPlayHistory()
        {
            return _playHistory;
        }

        public void setList(@NonNull ArrayList<AudioTrack> playHistory)
        {
            _playHistory = playHistory;
        }

        private void addTrack(@NonNull AudioTrack newTrack)
        {
            // Make sure that the history tracks are unique
            for (AudioTrack track : _playHistory)
            {
                if (track.equals(newTrack))
                {
                    _playHistory.remove(track);
                    break;
                }
            }

            _playHistory.add(0, newTrack);

            // Do not exceed the play history capacity
            int capacity = GeneralStorage.getShared().getPlayerPlayedHistoryCapacity();

            while (_playHistory.size() > capacity)
            {
                _playHistory.remove(_playHistory.size()-1);
            }
        }

        public void playPrevious()
        {
            stop();

            if (_playHistory.size() <= 1)
            {
                return;
            }

            _playHistory.remove(0);

            AudioTrack previousTrack = _playHistory.get(0);

            AudioPlaylist playlist = previousTrack.source.getSourcePlaylist(_audioInfo, previousTrack);

            if (playlist == null)
            {
                String playlistName = _application.getResources().getString(R.string.playlist_name_previously_played);
                playlist = new AudioPlaylist(playlistName, previousTrack);
            }
            
            try {
                playPlaylist(playlist);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play previous from play history, " + e.toString());
                
                stop();
            }
        }
    }
}
