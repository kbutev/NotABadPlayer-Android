package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import java.util.List;

import com.media.notabadplayer.Constants.AppSettings;

public class SafeMutableAudioPlaylist implements MutableAudioPlaylist {
    private @NonNull final Object _lock = new Object();

    private @NonNull MutableAudioPlaylist _write;
    private @NonNull MutableAudioPlaylist _read;

    public static SafeMutableAudioPlaylist build(@NonNull MutableAudioPlaylist prototype) throws Exception {
        return new SafeMutableAudioPlaylist(prototype);
    }

    public SafeMutableAudioPlaylist(@NonNull MutableAudioPlaylist prototype) throws Exception {
        _write = AudioPlaylistBuilder.buildMutableFromImmutable(prototype);
        _read = AudioPlaylistBuilder.buildMutableFromImmutable(prototype);
    }

    public @NonNull MutableAudioPlaylist copy()
    {
        synchronized (_lock)
        {
            return _read;
        }
    }

    @Override
    public void playCurrent()
    {
        synchronized (_lock)
        {
            _write.playCurrent();
            updateReadPlaylist();
        }
    }

    @Override
    public void goToTrack(@NonNull BaseAudioTrack track)
    {
        synchronized (_lock)
        {
            _write.goToTrack(track);
            updateReadPlaylist();
        }
    }

    @Override
    public void goToTrackAt(int trackIndex)
    {
        synchronized (_lock)
        {
            _write.goToTrackAt(trackIndex);
            updateReadPlaylist();
        }
    }

    @Override
    public void goToTrackBasedOnPlayOrder(AudioPlayOrder playOrder)
    {
        synchronized (_lock)
        {
            _write.goToTrackBasedOnPlayOrder(playOrder);
            updateReadPlaylist();
        }
    }

    @Override
    public void goToNextPlayingTrack()
    {
        synchronized (_lock)
        {
            _write.goToNextPlayingTrack();
            updateReadPlaylist();
        }
    }

    @Override
    public void goToNextPlayingTrackRepeat()
    {
        synchronized (_lock)
        {
            _write.goToNextPlayingTrackRepeat();
            updateReadPlaylist();
        }
    }

    @Override
    public void goToPreviousPlayingTrack()
    {
        synchronized (_lock)
        {
            _write.goToPreviousPlayingTrack();
            updateReadPlaylist();
        }
    }

    @Override
    public void goToTrackByShuffle()
    {
        synchronized (_lock)
        {
            _write.goToTrackByShuffle();
            updateReadPlaylist();
        }
    }

    @NonNull
    @Override
    public String getName()
    {
        synchronized (_lock)
        {
            return _read.getName();
        }
    }

    @Override
    public int size()
    {
        synchronized (_lock)
        {
            return _read.size();
        }
    }

    @Override
    public boolean isPlaying()
    {
        synchronized (_lock)
        {
            return _read.isPlaying();
        }
    }

    @NonNull
    @Override
    public List<BaseAudioTrack> getTracks()
    {
        synchronized (_lock)
        {
            return _read.getTracks();
        }
    }

    @NonNull
    @Override
    public BaseAudioTrack getTrack(int index)
    {
        synchronized (_lock)
        {
            return _read.getTrack(index);
        }
    }

    @Override
    public boolean hasTrack(@NonNull BaseAudioTrack track)
    {
        synchronized (_lock)
        {
            return _read.hasTrack(track);
        }
    }

    @Override
    public int getPlayingTrackIndex()
    {
        synchronized (_lock)
        {
            return _read.getPlayingTrackIndex();
        }
    }

    @NonNull
    @Override
    public BaseAudioTrack getPlayingTrack()
    {
        synchronized (_lock)
        {
            return _read.getPlayingTrack();
        }
    }

    @Override
    public boolean isPlayingFirstTrack()
    {
        synchronized (_lock)
        {
            return _read.isPlayingFirstTrack();
        }
    }

    @Override
    public boolean isPlayingLastTrack()
    {
        synchronized (_lock)
        {
            return _read.isPlayingLastTrack();
        }
    }

    @Override
    public boolean isAlbumPlaylist()
    {
        synchronized (_lock)
        {
            return _read.isAlbumPlaylist();
        }
    }

    @Override
    public boolean isTemporaryPlaylist()
    {
        synchronized (_lock)
        {
            return _read.isTemporaryPlaylist();
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
            _read = AudioPlaylistBuilder.buildMutableFromImmutable(_write);
        } catch (Exception e) {

        }
    }
}
