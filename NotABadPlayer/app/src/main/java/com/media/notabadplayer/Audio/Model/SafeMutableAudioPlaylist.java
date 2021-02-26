package com.media.notabadplayer.Audio.Model;

import androidx.annotation.NonNull;
import android.util.Log;

import java.util.List;

import com.media.notabadplayer.Constants.AppSettings;

// Wraps a single playlist that can be mutated at any time.
// Very memory/cpu efficient, use copy() to quickly get a copy of the latest playlist data.
// Thread safe: yes
public class SafeMutableAudioPlaylist implements MutableAudioPlaylist {
    private @NonNull final Object _lock = new Object();

    private @NonNull final MutableAudioPlaylist _write;
    private @NonNull MutableAudioPlaylist _read;

    public static SafeMutableAudioPlaylist build(@NonNull MutableAudioPlaylist prototype) throws Exception {
        return new SafeMutableAudioPlaylist(prototype);
    }

    public SafeMutableAudioPlaylist(@NonNull MutableAudioPlaylist prototype) throws Exception {
        _write = AudioPlaylistBuilder.buildMutableFromImmutable(prototype);
        _read = AudioPlaylistBuilder.buildMutableFromImmutable(prototype);

        if (!_read.getPlayingTrack().getTitle().equals(_write.getPlayingTrack().getTitle())) {
            Log.v(SafeMutableAudioPlaylist.class.getCanonicalName(), "Failed to update read playlist, error: " );
        }
    }

    public @NonNull MutableAudioPlaylist copy()
    {
        synchronized (_lock)
        {
            if (!_read.getPlayingTrack().getTitle().equals(_write.getPlayingTrack().getTitle())) {
                Log.v(SafeMutableAudioPlaylist.class.getCanonicalName(), "Failed to update read playlist, error: " );
            }
            
            return _read;
        }
    }
    
    @Override
    public void playCurrent()
    {
        synchronized (_lock)
        {
            _write.playCurrent();
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToTrack(@NonNull BaseAudioTrack track)
    {
        synchronized (_lock)
        {
            _write.goToTrack(track);
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToTrackAt(int trackIndex)
    {
        synchronized (_lock)
        {
            _write.goToTrackAt(trackIndex);
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToTrackBasedOnPlayOrder(AudioPlayOrder playOrder)
    {
        synchronized (_lock)
        {
            _write.goToTrackBasedOnPlayOrder(playOrder);
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToNextPlayingTrack()
    {
        synchronized (_lock)
        {
            _write.goToNextPlayingTrack();
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToNextPlayingTrackRepeat()
    {
        synchronized (_lock)
        {
            _write.goToNextPlayingTrackRepeat();
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToPreviousPlayingTrack()
    {
        synchronized (_lock)
        {
            _write.goToPreviousPlayingTrack();
        }
        
        updateReadPlaylist();
    }

    @Override
    public void goToTrackByShuffle()
    {
        synchronized (_lock)
        {
            _write.goToTrackByShuffle();
        }
        
        updateReadPlaylist();
    }

    @NonNull
    @Override
    public String getName()
    {
        synchronized (_lock)
        {
            return _write.getName();
        }
    }

    @Override
    public int size()
    {
        synchronized (_lock)
        {
            return _write.size();
        }
    }

    @Override
    public boolean isPlaying()
    {
        synchronized (_lock)
        {
            return _write.isPlaying();
        }
    }

    @NonNull
    @Override
    public List<BaseAudioTrack> getTracks()
    {
        synchronized (_lock)
        {
            return _write.getTracks();
        }
    }

    @NonNull
    @Override
    public BaseAudioTrack getTrack(int index)
    {
        synchronized (_lock)
        {
            return _write.getTrack(index);
        }
    }

    @Override
    public boolean hasTrack(@NonNull BaseAudioTrack track)
    {
        synchronized (_lock)
        {
            return _write.hasTrack(track);
        }
    }

    @Override
    public int getPlayingTrackIndex()
    {
        synchronized (_lock)
        {
            return _write.getPlayingTrackIndex();
        }
    }

    @NonNull
    @Override
    public BaseAudioTrack getPlayingTrack()
    {
        synchronized (_lock)
        {
            return _write.getPlayingTrack();
        }
    }

    @Override
    public boolean isPlayingFirstTrack()
    {
        synchronized (_lock)
        {
            return _write.isPlayingFirstTrack();
        }
    }

    @Override
    public boolean isPlayingLastTrack()
    {
        synchronized (_lock)
        {
            return _write.isPlayingLastTrack();
        }
    }

    @Override
    public boolean isAlbum()
    {
        synchronized (_lock)
        {
            return _write.isAlbum();
        }
    }

    @Override
    public boolean isTemporary()
    {
        synchronized (_lock)
        {
            return _write.isTemporary();
        }
    }

    @NonNull
    @Override
    public BaseAudioPlaylist sortedPlaylist(AppSettings.TrackSorting sorting)
    {
        synchronized (_lock)
        {
            return _write.sortedPlaylist(sorting);
        }
    }

    private void updateReadPlaylist()
    {
        try {
            synchronized (_lock)
            {
                _read = AudioPlaylistBuilder.buildMutableFromImmutable(_write);
                
                if (!_read.getPlayingTrack().getTitle().equals(_write.getPlayingTrack().getTitle())) {
                    Log.v(SafeMutableAudioPlaylist.class.getCanonicalName(), "Failed to update read playlist, error: " );
                }
            }
        } catch (Exception e) {
            Log.v(SafeMutableAudioPlaylist.class.getCanonicalName(), "Failed to update read playlist, error: " + e.toString());
        }
    }
}
