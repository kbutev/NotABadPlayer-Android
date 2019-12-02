package com.media.notabadplayer.Audio.Players;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerHistory;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlayerObservers;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.MutableAudioPlaylist;
import com.media.notabadplayer.PlayerApplication;
import com.media.notabadplayer.Storage.GeneralStorage;

// Standard audio player for the app.
// Before using the player, you MUST call start().
//
// The player automatically restores its state from user storage
// as soon as the audio player service is running.
// The player does not save its own state, it must be saved by an external state.
//
// Dependant on storage access permission (must have read permission for storage).
// Dependant on @AudioLibrary (must be initialized before using the player).
public class Player implements AudioPlayer {
    private static Player _singleton;
    
    private final Object lock = new Object();
    
    private AudioPlayerServiceBinding _serviceBinding = new AudioPlayerServiceBinding();
    private AudioPlayer _player = getPlayerDummy();
    
    private Application _application;
    private AudioInfo _audioInfo;

    public final Player.Observers observers = new Player.Observers();
    public final Player.PlayHistory playHistory = new Player.PlayHistory();
    
    private Player()
    {
        Log.v(Player.class.getCanonicalName(), "Initializing...");
        _application = PlayerApplication.getShared();
        Log.v(Player.class.getCanonicalName(), "Initialized!");
    }
    
    public synchronized static Player getShared()
    {
        if (_singleton == null)
        {
            _singleton = new Player();
        }
        
        return _singleton;
    }
    
    public @NonNull AudioInfo getAudioInfo()
    {
        if (_application == null)
        {
            throw new UncheckedExecutionException(new Exception("Player is not initialized, start() has never been called"));
        }
        
        return _audioInfo;
    }
    
    public boolean isStarted()
    {
        synchronized (lock)
        {
            return _audioInfo != null;
        }
    }

    public void start(@NonNull AudioInfo audioInfo)
    {
        if (isStarted())
        {
            throw new UncheckedExecutionException(new Exception("Must not call start() twice"));
        }
        
        Log.v(Player.class.getCanonicalName(), "Player starting...");

        synchronized (lock)
        {
            _audioInfo = audioInfo;
        }
        
        // Start the audio service
        _serviceBinding.startService();
    }
    
    private AudioPlayer getPlayer()
    {
        return _player;
    }

    private void setPlayer(AudioPlayerService player)
    {
        if (player != null)
        {
            Log.v(Player.class.getCanonicalName(), "Audio service started!");

            _player = player;

            // Transfer observers from the dummy player to the real player
            for (AudioPlayerObserver observer : observers._observers)
            {
                _player.observers().attach(observer);
            }

            // Restore audio state here
            restorePlayerStateFromStorage(true);
            restorePlayHistoryFromStorage();

            // Alert observers of the current state
            BaseAudioPlaylist playlist = getPlaylist();

            if (playlist != null)
            {
                BaseAudioTrack track = playlist.getPlayingTrack();

                for (AudioPlayerObserver observer : observers._observers)
                {
                    observer.onPlayerPause(track);
                }
            }
            else
            {
                for (AudioPlayerObserver observer : observers._observers)
                {
                    observer.onPlayerStop();
                }
            }
        }
        else
        {
            Log.v(Player.class.getCanonicalName(), "Audio service has been terminated!");

            _player = getPlayerDummy();
        }
    }

    private AudioPlayer getPlayerDummy()
    {
        // This is a dummy object to which any normal audio player request can be forwarded to
        // The purpose of this object is to avoid null pointer exception
        // When the audio service is not running, the @_player is set to equal to a dummy
        // Note: while the dummy can be used to ignore most player requests, the player observers
        // must be recorded and transferred to the real audio service when it gets set
        return new AudioPlayerDummy(observers);
    }

    private void restorePlayerStateFromStorage(boolean pause)
    {
        GeneralStorage storage = GeneralStorage.getShared();

        // Restore play order
        AudioPlayOrder playOrder = storage.retrievePlayerStatePlayOrder();
        setPlayOrder(playOrder);

        // Restore playlist
        MutableAudioPlaylist playlist = storage.retrievePlayerStateCurrentPlaylist();

        if (playlist != null)
        {
            try {
                playPlaylist(playlist);
            } catch (Exception e) {
                Log.v(Player.class.getCanonicalName(), "Failed to restore the player state from storage.");
                return;
            }
        }

        // Start player as paused?
        if (pause && playlist != null)
        {
            pause();
        }

        // Restore play position
        int playPosition = storage.retrievePlayerStatePlayPosition();
        seekTo(playPosition);
    }

    private void restorePlayHistoryFromStorage()
    {
        List<BaseAudioTrack> history = GeneralStorage.getShared().retrievePlayerPlayHistoryState();

        if (history != null)
        {
            playHistory.setList(history);
        }
    }

    @Override
    public boolean isPlaying()
    {
        return getPlayer().isPlaying();
    }

    @Override 
    public boolean isCompletelyStopped() {
        return getPlayer().isCompletelyStopped();
    }

    @Override
    public @Nullable BaseAudioPlaylist getPlaylist()
    {
        return getPlayer().getPlaylist();
    }

    @Override
    public @Nullable MutableAudioPlaylist getMutablePlaylistCopy()
    {
        BaseAudioPlaylist playlist = getPlaylist();
        
        if (playlist == null)
        {
            return null;
        }
        
        try {
            return AudioPlaylistBuilder.buildMutableFromImmutable(playlist);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public boolean hasPlaylist()
    {
        return getPlayer().hasPlaylist();
    }

    @Override
    public AudioPlayOrder getPlayOrder()
    {
        return getPlayer().getPlayOrder();
    }

    @Override
    public void setPlayOrder(AudioPlayOrder order)
    {
        getPlayer().setPlayOrder(order);
    }

    @Override
    public void playPlaylist(@NonNull BaseAudioPlaylist playlist) throws Exception
    {
        getPlayer().playPlaylist(playlist);
    }

    @Override
    public void resume()
    {
        getPlayer().resume();
    }

    @Override
    public void pause()
    {
        getPlayer().pause();
    }

    @Override
    public void stop()
    {
        getPlayer().stop();
    }

    @Override
    public void pauseOrResume()
    {
        getPlayer().pauseOrResume();
    }

    @Override
    public void playNext() throws Exception
    {
        getPlayer().playNext();
    }

    @Override
    public void playPrevious() throws Exception
    {
        getPlayer().playPrevious();
    }

    @Override
    public void playNextBasedOnPlayOrder() throws Exception
    {
        getPlayer().playNextBasedOnPlayOrder();
    }

    @Override
    public void shuffle() throws Exception
    {
        getPlayer().shuffle();
    }

    @Override
    public void jumpBackwards(int msec)
    {
        getPlayer().jumpBackwards(msec);
    }

    @Override
    public void jumpForwards(int msec)
    {
        getPlayer().jumpForwards(msec);
    }

    @Override
    public int getDurationMSec()
    {
        return getPlayer().getDurationMSec();
    }

    @Override
    public int getCurrentPositionMSec()
    {
        return getPlayer().getCurrentPositionMSec();
    }

    @Override
    public void seekTo(int msec)
    {
        getPlayer().seekTo(msec);
    }

    @Override
    public int getVolume()
    {
        return getPlayer().getVolume();
    }

    @Override
    public void setVolume(int volume)
    {
        getPlayer().setVolume(volume);
    }

    @Override
    public void volumeUp()
    {
        getPlayer().volumeUp();
    }

    @Override
    public void volumeDown()
    {
        getPlayer().volumeDown();
    }

    @Override
    public boolean isMuted()
    {
        return getPlayer().isMuted();
    }

    @Override
    public void muteOrUnmute()
    {
        getPlayer().muteOrUnmute();
    }

    @Override
    public void mute()
    {
        getPlayer().mute();
    }

    @Override
    public void unmute()
    {
        getPlayer().unmute();
    }

    @Override
    public @NonNull AudioPlayerObservers observers() {
        return getPlayer().observers();
    }

    @Override
    public @NonNull AudioPlayerHistory playHistory() {
        return getPlayer().playHistory();
    }

    public class Observers implements AudioPlayerObservers
    {
        // Have a copy of all the attached observers so they can be transfered from the dummy player
        // to the real audio player service
        private ArrayList<AudioPlayerObserver> _observers = new ArrayList<>();

        @Override
        public void attach(AudioPlayerObserver observer)
        {
            getPlayer().observers().attach(observer);
            
            if (_observers.contains(observer))
            {
                return;
            }

            _observers.add(observer);
        }

        @Override
        public void detach(AudioPlayerObserver observer)
        {
            _observers.remove(observer);
            
            getPlayer().observers().detach(observer);
        }
    }

    public class PlayHistory implements AudioPlayerHistory
    {
        @Override
        public @NonNull List<BaseAudioTrack> getPlayHistory()
        {
            return getPlayer().playHistory().getPlayHistory();
        }
        
        public @Nullable BaseAudioPlaylist getPlayHistoryAsPlaylist(@NonNull String playlistName)
        {
            List<BaseAudioTrack> tracks = getPlayer().playHistory().getPlayHistory();
            
            if (tracks.size() == 0)
            {
                return null;
            }

            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(playlistName);
            node.setTracks(tracks);
            node.setIsTemporaryPlaylist(true);

            // Try to build
            try {
                return node.build();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void setList(@NonNull List<BaseAudioTrack> playHistory)
        {
            getPlayer().playHistory().setList(playHistory);
        }

        public void playPreviousInHistory() throws Exception
        {
            getPlayer().playHistory().playPreviousInHistory(_audioInfo);
        }

        @Override
        public void playPreviousInHistory(@NonNull AudioInfo audioInfo) throws Exception
        {
            getPlayer().playHistory().playPreviousInHistory(audioInfo);
        }
    }
    
    private class AudioPlayerServiceBinding {
        private boolean shouldUnbind;
        
        private ServiceConnection connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service)
            {
                Log.e(AudioPlayerService.class.getCanonicalName(), "Audio service started!");
                setPlayer(((AudioPlayerService.LocalBinder)service).getService());
            }

            public void onServiceDisconnected(ComponentName className)
            {
                Log.e(AudioPlayerService.class.getCanonicalName(), "Audio service stopped!");
                setPlayer(null);
            }
            
            public void onBindingDied(ComponentName name)
            {
                Log.e(AudioPlayerService.class.getCanonicalName(), "Audio service stopped!");
                setPlayer(null);
            }
        };
        
        private void startService()
        {
            if (shouldUnbind)
            {
                Log.e(AudioPlayerService.class.getCanonicalName(), "No need to start audio service, its already running!");
                return;
            }
            
            Log.e(AudioPlayerService.class.getCanonicalName(), "Starting audio service...");
            
            Intent intent = new Intent(_application, AudioPlayerService.class);
            
            _application.startService(intent);

            if (_application.bindService(intent, connection, Context.BIND_AUTO_CREATE)) 
            {
                shouldUnbind = true;
            } 
            else 
            {
                Log.e(AudioPlayerService.class.getCanonicalName(), "Failed to start audio service.");
            }
        }
        
        private void stopService()
        {
            if (shouldUnbind)
            {
                Log.e(AudioPlayerService.class.getCanonicalName(), "Stopping audio service...");
                
                _application.unbindService(connection);
                Intent intent = new Intent(_application, AudioPlayerService.class);
                _application.stopService(intent);
                shouldUnbind = false;
            }
        }
    }
}
