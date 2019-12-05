package com.media.notabadplayer.Audio.Players;

import java.util.ArrayList;
import java.util.List;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.math.MathUtils;
import android.util.Log;

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
import com.media.notabadplayer.Audio.Model.SafeMutableAudioPlaylist;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.Main.MainActivity;

public class AudioPlayerService extends Service implements AudioPlayer {
    // Use to access and communicate with the audio player directly (no need for IPC communication)
    class LocalBinder extends Binder {
        AudioPlayerService getService()
        {
            return AudioPlayerService.this;
        }
    }

    private final Object _lock = new Object();
    
    private final IBinder _binder = new LocalBinder();
    private AudioPlayerService.NotificationCenter _notificationCenter;
    
    private android.media.MediaPlayer _player;
    private @Nullable SafeMutableAudioPlaylist __unsafePlaylist;
    private AudioPlayOrder _playOrder = AudioPlayOrder.FORWARDS;

    private boolean _muted;
    
    private final AudioPlayerService.Observers _observers = new AudioPlayerService.Observers();
    private final AudioPlayerService.PlayHistory _playHistory = new AudioPlayerService.PlayHistory();

    private final String BROADCAST_ACTION_PLAY = "AudioPlayerService.play";
    private final String BROADCAST_ACTION_PAUSE = "AudioPlayerService.pause";
    private final String BROADCAST_ACTION_PREVIOUS = "AudioPlayerService.previous";
    private final String BROADCAST_ACTION_NEXT = "AudioPlayerService.next";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String value = intent.getAction();
            
            if (value != null)
            {
                Log.v(AudioPlayerService.class.getCanonicalName(), "Responding to notification action: " + value);

                performBroadcastAction(value);
            }
        }
    };
    
    private void initialize()
    {
        _notificationCenter = new AudioPlayerService.NotificationCenter();
        
        _player = new android.media.MediaPlayer();
        _player.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                _observers.onFinish();
                try {
                    playNextBasedOnPlayOrder();
                } catch (Exception e) {
                    
                }
            }
        });

        _player.setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                _observers.onFinish();
                return true;
            }
        });

        _muted = false;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION_PLAY);
        filter.addAction(BROADCAST_ACTION_PAUSE);
        filter.addAction(BROADCAST_ACTION_PREVIOUS);
        filter.addAction(BROADCAST_ACTION_NEXT);
        registerReceiver(receiver, filter);
    }
    
    private @NonNull Context getContext()
    {
        return getApplicationContext();
    }

    public final android.media.MediaPlayer getPlayer()
    {
        return _player;
    }

    @Override
    public boolean isPlaying()
    {
        return _player.isPlaying();
    }

    @Override
    public boolean isCompletelyStopped()
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();
        return playlist == null || !playlist.isPlaying();
    }

    @Override
    public @Nullable BaseAudioPlaylist getPlaylist()
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();
        return playlist != null ? playlist.copy() : null;
    }
    
    @Override
    public boolean hasPlaylist()
    {
        return getSafeMutablePlaylist() != null;
    }

    public @Nullable SafeMutableAudioPlaylist getSafeMutablePlaylist()
    {
        synchronized (_lock)
        {
            return __unsafePlaylist;
        }
    }

    public void setSafeMutablePlaylist(@NonNull SafeMutableAudioPlaylist playlist)
    {
        synchronized (_lock)
        {
            __unsafePlaylist = playlist;
        }
    }

    @Override
    public AudioPlayOrder getPlayOrder()
    {
        return _playOrder;
    }

    @Override
    public void setPlayOrder(AudioPlayOrder order)
    {
        _playOrder = order;

        _observers.onPlayOrderChange(order);
    }

    @Override
    public void playPlaylist(@NonNull BaseAudioPlaylist playlist) throws Exception
    {
        SafeMutableAudioPlaylist currentPlaylist = getSafeMutablePlaylist();
        BaseAudioTrack previousTrack = currentPlaylist != null ? currentPlaylist.getPlayingTrack() : null;

        try {
            playTrack(playlist.getPlayingTrack(), previousTrack, true);
        } catch (Exception e) {
            throw e;
        }

        SafeMutableAudioPlaylist newPlaylist = SafeMutableAudioPlaylist.build(AudioPlaylistBuilder.buildMutableFromImmutable(playlist));
        setSafeMutablePlaylist(newPlaylist);
        newPlaylist.playCurrent();
    }

    private void playTrack(@NonNull BaseAudioTrack newTrack, @Nullable BaseAudioTrack previousTrack, boolean usePlayHistory) throws Exception
    {
        boolean isPlaying = _player.isPlaying();
        
        Uri path = Uri.parse(Uri.decode(newTrack.getFilePath()));

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
            
            // If track cannot be played, go back to previous track
            if (previousTrack != null)
            {
                Uri pathOfPreviousTrack = Uri.parse(Uri.decode(previousTrack.getFilePath()));

                try {
                    _player.reset();
                    _player.setDataSource(getContext(), pathOfPreviousTrack);
                    _player.prepare();
                    _player.start();
                    
                    if (!isPlaying)
                    {
                        _player.pause();
                    }
                } catch (Exception e2) {
                    
                }
            }
            else
            {
                stop();
            }
            
            throw e;
        }

        Log.v(AudioPlayer.class.getCanonicalName(), "Playing track: " + newTrack.getTitle());

        if (usePlayHistory)
        {
            _playHistory.addTrack(newTrack);
        }

        _observers.onPlay(newTrack);
        
        _notificationCenter.showNotificationForPlayingTrack(newTrack, true);
    }

    @Override
    public void resume()
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }

        try
        {
            if (!isPlaying())
            {
                _player.start();

                Log.v(AudioPlayer.class.getCanonicalName(), "Resume");
                
                _observers.onResume(playlist.getPlayingTrack());

                _notificationCenter.showNotificationForPlayingTrack(playlist.getPlayingTrack(), true);
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot resume, " + e.toString());
        }
    }

    @Override
    public void pause()
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }

        try
        {
            if (isPlaying())
            {
                _player.pause();

                Log.v(AudioPlayer.class.getCanonicalName(), "Pause");
                
                _observers.onPause(playlist.getPlayingTrack());

                _notificationCenter.showNotificationForPlayingTrack(playlist.getPlayingTrack(), false);
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot pause, " + e.toString());
        }
    }

    @Override
    public void stop()
    {
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

                Log.v(AudioPlayer.class.getCanonicalName(), "Stop");
                
                _observers.onStop();

                _notificationCenter.clear();
            }
        }
        catch (Exception e)
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot stop, " + e.toString());
        }
    }

    @Override
    public void pauseOrResume()
    {
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

    @Override
    public void playNext() throws Exception
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }

        BaseAudioTrack previousTrack = playlist.getPlayingTrack();

        playlist.goToNextPlayingTrack();

        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing next track...");

            try {
                playTrack(playlist.getPlayingTrack(), previousTrack, true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play next, " + e.toString());
                playlist.goToTrack(previousTrack);
                _observers.onStop();
                throw e;
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            _observers.onStop();
        }
    }

    @Override
    public void playPrevious() throws Exception
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }

        BaseAudioTrack previousTrack = playlist.getPlayingTrack();

        playlist.goToPreviousPlayingTrack();

        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing previous track...");

            try {
                playTrack(playlist.getPlayingTrack(), previousTrack, true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play previous, " + e.toString());
                playlist.goToTrack(previousTrack);
                _observers.onPause(previousTrack);
                throw e;
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, cannot go before first track");

            stop();

            _observers.onStop();
        }
    }

    @Override
    public void playNextBasedOnPlayOrder() throws Exception
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }

        BaseAudioTrack previousTrack = playlist.getPlayingTrack();

        playlist.goToTrackBasedOnPlayOrder(_playOrder);

        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing next track based on play order...");

            try {
                playTrack(playlist.getPlayingTrack(), previousTrack, true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play next based on play order, " + e.toString());
                playlist.goToTrack(previousTrack);
                _observers.onPause(previousTrack);
                throw e;
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            _observers.onStop();
        }
    }

    @Override
    public void shuffle() throws Exception
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }

        BaseAudioTrack previousTrack = playlist.getPlayingTrack();

        playlist.goToTrackByShuffle();

        if (!isCompletelyStopped())
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Playing random track...");

            try {
                playTrack(playlist.getPlayingTrack(), previousTrack, true);
            } catch (Exception e) {
                Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot play random, " + e.toString());
                playlist.goToTrack(previousTrack);
                _observers.onPause(previousTrack);
                throw e;
            }
        }
        else
        {
            Log.v(AudioPlayer.class.getCanonicalName(), "Stop playing, got to last track");

            stop();

            _observers.onStop();
        }
    }

    @Override
    public void jumpBackwards(int msec)
    {
        if (!hasPlaylist())
        {
            return;
        }

        int duration = getDurationMSec();
        int currentPosition = getCurrentPositionMSec();
        int destination = currentPosition - msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }

    @Override
    public void jumpForwards(int msec)
    {
        if (!hasPlaylist())
        {
            return;
        }

        int duration = getDurationMSec();
        int currentPosition = getCurrentPositionMSec();
        int destination = currentPosition + msec;
        seekTo(MathUtils.clamp(destination, 0, duration));
    }

    @Override
    public int getDurationMSec()
    {
        return _player.getDuration() / 1000;
    }

    @Override
    public int getCurrentPositionMSec()
    {
        return _player.getCurrentPosition() / 1000;
    }

    @Override
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
            Log.v(AudioPlayer.class.getCanonicalName(), "Error: cannot seek to, " + e.toString());
        }
    }

    @Override
    public int getVolume()
    {
        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        double max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        double result = (manager.getStreamVolume(AudioManager.STREAM_MUSIC) / max) * 100;
        return (int)result;
    }

    @Override
    public void setVolume(int volume)
    {
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

    @Override
    public void volumeUp()
    {
        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);

        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int incrementVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10;
        int result = currentVolume + incrementVolume;

        manager.setStreamVolume(AudioManager.STREAM_MUSIC, result,0);
    }

    @Override
    public void volumeDown()
    {
        AudioManager manager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);

        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int incrementVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 10;
        int result = currentVolume - incrementVolume > 0 ? currentVolume - incrementVolume : 0;

        manager.setStreamVolume(AudioManager.STREAM_MUSIC, result,0);
    }

    @Override
    public boolean isMuted()
    {
        return _muted;
    }

    @Override
    public void muteOrUnmute()
    {
        if (!_muted)
        {
            mute();
        }
        else
        {
            unmute();
        }
    }

    @Override
    public void mute()
    {
        if (!_muted)
        {
            _player.setVolume(0, 0);

            _muted = true;

            Log.v(AudioPlayer.class.getCanonicalName(), "Mute");
        }
    }

    @Override
    public void unmute()
    {
        if (_muted)
        {
            _player.setVolume(1, 1);

            _muted = false;

            Log.v(AudioPlayer.class.getCanonicalName(), "Unmute");
        }
    }

    @Override
    public @NonNull AudioPlayerObservers observers()
    {
        return _observers;
    }
    
    @Override
    public @NonNull AudioPlayerHistory playHistory()
    {
        return _playHistory;
    }

    private void performBroadcastAction(@NonNull String value)
    {
        SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

        if (playlist == null)
        {
            return;
        }
        
        if (value.equals(BROADCAST_ACTION_PLAY))
        {
            resume();
        }

        if (value.equals(BROADCAST_ACTION_PAUSE))
        {
            pause();
        }

        if (value.equals(BROADCAST_ACTION_PREVIOUS))
        {
            if (!playlist.isPlayingFirstTrack())
            {
                try {
                    playPrevious();
                } catch (Exception e) {
                    
                }
            }
            else
            {
                pause();
            }
        }

        if (value.equals(BROADCAST_ACTION_NEXT))
        {
            if (!playlist.isPlayingLastTrack())
            {
                try {
                    playNext();
                } catch (Exception e) {

                }
            }
            else
            {
                pause();
            }
        }
    }
    
    public class Observers implements AudioPlayerObservers
    {
        private ArrayList<AudioPlayerObserver> _observers = new ArrayList<>();

        @Override
        public void attach(AudioPlayerObserver observer)
        {
            if (_observers.contains(observer))
            {
                return;
            }

            _observers.add(observer);

            fullyUpdateObserver(observer);
        }

        @Override
        public void detach(AudioPlayerObserver observer)
        {
            _observers.remove(observer);
        }

        private void fullyUpdateObserver(AudioPlayerObserver observer)
        {
            observer.onPlayOrderChange(_playOrder);
        }

        private void onPlay(@NonNull BaseAudioTrack track)
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

        private void onResume(@NonNull BaseAudioTrack track)
        {
            for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerResume(track);}
        }

        private void onPause(@NonNull BaseAudioTrack track)
        {
            for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayerPause(track);}
        }

        private void onPlayOrderChange(AudioPlayOrder order)
        {
            for (int e = 0; e < _observers.size(); e++) {_observers.get(e).onPlayOrderChange(order);}
        }
    }

    public class PlayHistory implements AudioPlayerHistory
    {
        private ArrayList<BaseAudioTrack> _playHistory = new ArrayList<>();

        @Override
        public @NonNull List<BaseAudioTrack> getPlayHistory()
        {
            return _playHistory;
        }

        @Override
        public void setList(@NonNull List<BaseAudioTrack> playHistory)
        {
            _playHistory = new ArrayList<>(playHistory);
        }
        
        @Override
        public void playPreviousInHistory(@NonNull AudioInfo audioInfo) throws Exception
        {
            stop();

            SafeMutableAudioPlaylist playlist = getSafeMutablePlaylist();

            if (playlist == null)
            {
                return;
            }

            if (_playHistory.size() <= 1)
            {
                return;
            }

            _playHistory.remove(0);

            BaseAudioTrack previousTrack = playlist.getPlayingTrack();
            BaseAudioTrack previouslyPlayed = _playHistory.get(0);

            BaseAudioPlaylist sourcePlaylist = previouslyPlayed.getSource().getSourcePlaylist(audioInfo, previouslyPlayed);
            MutableAudioPlaylist newPlaylist;

            if (sourcePlaylist == null)
            {
                // Create a playlist with just one track
                String playlistName = getContext().getResources().getString(R.string.playlist_name_previously_played);

                BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
                node.setName(playlistName);
                node.setTracksToOneTrack(previouslyPlayed);

                newPlaylist = node.buildMutable();
            }
            else
            {
                newPlaylist = AudioPlaylistBuilder.buildMutableFromImmutable(sourcePlaylist);
            }

            try {
                playPlaylist(newPlaylist);
            } catch (Exception e) {
                Log.v(Player.class.getCanonicalName(), "Error: cannot play previous from play history, " + e.toString());
                playlist.goToTrack(previousTrack);
                _observers.onStop();
                throw e;
            }
        }

        private void addTrack(@NonNull BaseAudioTrack newTrack)
        {
            // Make sure that the history tracks are unique
            for (BaseAudioTrack track : _playHistory)
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
    }
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return _binder;
    }

    @Override
    public void onCreate() 
    {
        super.onCreate();
        
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        Log.v(AudioPlayerService.class.getCanonicalName(), "Started!");
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        unregisterReceiver(receiver);

        stop();
        
        Log.v(AudioPlayerService.class.getCanonicalName(), "Destroyed!");
        
        // Cancel the persistent notification.
        _notificationCenter.clear();
    }
    
    private class NotificationCenter {
        private NotificationManager _notificationManager;
        private NotificationChannel _notificationChannel;
        
        private String _channelID = "NotABadPlayer";
        private String _channelName = "playing";
        private String _channelDescription = "playing audio in the background";
        private int _notificationID = 1;

        private String _actionResumeString;
        private String _actionPauseString;
        private String _actionPreviousString;
        private String _actionNextString;
        
        private String _notificationPrefix;
        private String _notificationSuffix;
        
        NotificationCenter()
        {
            _notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            
            // After Android 8, it is required to register with the system before pushing notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
            {
                _notificationChannel = new NotificationChannel(_channelID, _channelName, NotificationManager.IMPORTANCE_LOW);
                _notificationChannel.setDescription(_channelDescription);
                _notificationChannel.setShowBadge(false);
                _notificationManager.createNotificationChannel(_notificationChannel);
            }

            Resources resources = getContext().getResources();
            _actionResumeString = resources.getString(R.string.notification_resume);
            _actionPauseString = resources.getString(R.string.notification_pause);
            _actionPreviousString = resources.getString(R.string.notification_previous);
            _actionNextString = resources.getString(R.string.notification_next);
            _notificationPrefix = resources.getString(R.string.notification_title_prefix);
            _notificationSuffix = resources.getString(R.string.notification_title_suffix);
        }
        
        private void showNotificationForPlayingTrack(@NonNull BaseAudioTrack track, boolean isPlaying)
        {
            String playingTrackName = track.getTitle();
            String content = _notificationPrefix + playingTrackName + _notificationSuffix + track.getAlbumTitle();
            
            showNotification(content, isPlaying);
        }
        
        private void showNotification(@NonNull String content, boolean isPlaying)
        {
            Log.v(AudioPlayerService.class.getCanonicalName(), "Showing notification '" + content + "' " + (isPlaying ? "playing" : "paused"));
            
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

            Intent actionIntentData;

            // Build basics
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), _channelID)
                    .setSmallIcon(R.drawable.media_play)
                    .setContentTitle(content)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(contentIntent);
            
            // Previous and next actions
            actionIntentData = new Intent();
            actionIntentData.setAction(BROADCAST_ACTION_PREVIOUS);
            PendingIntent previousAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

            actionIntentData = new Intent();
            actionIntentData.setAction(BROADCAST_ACTION_NEXT);
            PendingIntent nextAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);
            
            // Build pause/play action
            actionIntentData = new Intent();
            String playPauseString = null;

            if (isPlaying)
            {
                actionIntentData.setAction(BROADCAST_ACTION_PAUSE);
                playPauseString = _actionPauseString;
            }
            else
            {
                actionIntentData.setAction(BROADCAST_ACTION_PLAY);
                playPauseString = _actionResumeString;
            }

            PendingIntent playPauseAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(0, _actionPreviousString, previousAction)
                    .addAction(0, playPauseString, playPauseAction)
                    .addAction(0, _actionNextString, nextAction);
            
            // Result
            Notification n = builder.build();
            
            // Notify
            _notificationManager.notify(_notificationID, n);
        }
        
        private void clear()
        {
            _notificationManager.cancel(_notificationID);
        }
    }
}
