package com.media.notabadplayer.Audio.Model;

import android.media.AudioTrack;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Utilities.MediaSorting;

public class AudioPlaylistV1 implements MutableAudioPlaylist, Serializable
{
    private final @NonNull String _name;
    private final @NonNull List<BaseAudioTrack> _tracks;
    private boolean _playing;
    private int _playingTrackPosition;
    private boolean _temporary;
    transient private Random _random;

    public AudioPlaylistV1(@NonNull MutableAudioPlaylist prototype)
    {
        _name = prototype.getName();
        _tracks = new ArrayList<>(prototype.getTracks());
        _playing = prototype.isPlaying();
        _playingTrackPosition = prototype.getPlayingTrackIndex();
        _temporary = prototype.isTemporary();
    }

    public AudioPlaylistV1(@NonNull String name, @NonNull List<BaseAudioTrack> tracks)
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }

        _name = name;
        _tracks = new ArrayList<>();
        _playing = false;
        _playingTrackPosition = 0;

        // Is album list?
        _tracks.add(tracks.get(0)); // Add one track just so we can determine isAlbum()
        boolean isAlbumList = isAlbum();
        _tracks.clear();

        // Make sure that all tracks have the correct source
        BaseAudioTrack firstTrack = tracks.get(0);

        AudioTrackSource thisSource = isAlbumList ? AudioTrackSource.createAlbumSource(firstTrack.getAlbumID()) : AudioTrackSource.createPlaylistSource(_name);

        for (int e = 0; e < tracks.size(); e++)
        {
            BaseAudioTrack track = tracks.get(e);
            AudioTrackSource trackSource = track.getSource();

            if (trackSource.equals(thisSource))
            {
                _tracks.add(track);
                continue;
            }

            BaseAudioTrackBuilderNode clone = AudioTrackBuilder.start(track);
            clone.setSource(thisSource);

            try {
                _tracks.add(clone.build());
            } catch (Exception exc) {
                Log.v(AudioPlaylistV1.class.getCanonicalName(), "Failed to copy audio track " + track.getFilePath());
            }
        }

        _random = new Random();
    }
    
    public AudioPlaylistV1(@NonNull String name,
                           @NonNull List<BaseAudioTrack> tracks,
                           @Nullable BaseAudioTrack playingTrack,
                           boolean startPlaying,
                           boolean temporaryList) throws Exception
    {
        this(name, tracks);
        
        int startWithTrackIndex = 0;
        
        if (playingTrack != null) {
            startWithTrackIndex = tracks.indexOf(playingTrack);
        }

        if (startWithTrackIndex < 0 || startWithTrackIndex >= tracks.size())
        {
            throw new IllegalArgumentException("Playlist cannot be created with a starting track not included in the given tracks");
        }

        _playing = startPlaying;
        _playingTrackPosition = startWithTrackIndex;
        _temporary = temporaryList;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof AudioPlaylistV1)
        {
            AudioPlaylistV1 playlist = (AudioPlaylistV1)o;
            
            return _name.equals(playlist._name) && _playingTrackPosition == playlist._playingTrackPosition && _tracks.equals(playlist._tracks);
        }

        return false;
    }

    // # BaseAudioPlaylist

    @Override
    public @NonNull String getName()
    {
        return _name;
    }

    @Override
    public int size()
    {
        return _tracks.size();
    }

    @Override
    public boolean isPlaying()
    {
        return _playing;
    }

    @Override
    public @NonNull List<BaseAudioTrack> getTracks()
    {
        return new ArrayList<>(_tracks);
    }

    @Override
    public @NonNull BaseAudioTrack getTrack(int index)
    {
        return _tracks.get(index);
    }

    @Override
    public boolean hasTrack(@NonNull BaseAudioTrack track)
    {
        return _tracks.contains(track);
    }

    @Override
    public int getPlayingTrackIndex()
    {
        return _playingTrackPosition;
    }
    
    @Override
    public @NonNull BaseAudioTrack getPlayingTrack()
    {
        return _tracks.get(_playingTrackPosition);
    }

    @Override
    public boolean isPlayingFirstTrack()
    {
        return _playingTrackPosition == 0;
    }

    @Override
    public boolean isPlayingLastTrack()
    {
        return _playingTrackPosition + 1 == _tracks.size();
    }
    
    @Override
    public boolean isAlbum()
    {
        return _name.equals(_tracks.get(0).getAlbumTitle());
    }
    
    @Override
    public boolean isTemporary()
    {
        return _temporary;
    }

    @Override
    public void playCurrent()
    {
        _playing = true;
    }

    @Override
    public void goToTrack(@NonNull BaseAudioTrack track)
    {
        int index = -1;

        for (BaseAudioTrack t : _tracks)
        {
            if (t.equals(track))
            {
                index = _tracks.indexOf(t);
                break;
            }
        }
        
        if (index != -1)
        {
            _playing = true;
            _playingTrackPosition = index;
            Log.v(AudioPlaylistV1.class.getCanonicalName(), "Go to track at index " + String.valueOf(index));
        }
        else
        {
            Log.v(AudioPlaylistV1.class.getCanonicalName(), "Cannot go to track, tracks list does not contain given track " + track.getFilePath());
        }
    }

    @Override
    public void goToTrackAt(int trackIndex)
    {
        if (trackIndex < 0 || trackIndex >= _tracks.size())
        {
            return;
        }
        
        goToTrack(_tracks.get(trackIndex));
    }

    @Override
    public void goToTrackBasedOnPlayOrder(AudioPlayOrder playOrder)
    {
        _playing = true;
        
        switch (playOrder)
        {
            case ONCE:
                _playing = false;
                break;
            case ONCE_FOREVER:
                break;
            case FORWARDS:
                goToNextPlayingTrack();
                break;
            case FORWARDS_REPEAT:
                goToNextPlayingTrackRepeat();
                break;
            case SHUFFLE:
                goToTrackByShuffle();
                break;
        }
    }

    @Override
    public void goToNextPlayingTrack()
    {
        _playing = true;

        // Stop playing upon reaching the end
        if (isPlayingLastTrack())
        {
            _playing = false;
        }
        else
        {
            _playingTrackPosition++;
        }
    }

    @Override
    public void goToNextPlayingTrackRepeat()
    {
        _playing = true;

        // Keep going until reaching the end
        // Once the end is reached, jump to the first track to loop the list again
        if (!isPlayingLastTrack())
        {
            goToNextPlayingTrack();
        }
        else
        {
            _playingTrackPosition = 0;
        }
    }

    @Override
    public void goToPreviousPlayingTrack()
    {
        _playing = true;
        
        if (isPlayingFirstTrack())
        {
            _playingTrackPosition = 0;
            _playing = false;
        }
        else
        {
            _playingTrackPosition--;
        }
    }

    @Override
    public void goToTrackByShuffle()
    {
        _playing = true;
        
        int min = 0;
        int max = _tracks.size()-1;
        _playingTrackPosition = _random.nextInt((max - min) + 1) + min;
    }

    @Override
    public @NonNull BaseAudioPlaylist sortedPlaylist(AppSettings.TrackSorting sorting)
    {
        List<BaseAudioTrack> tracks = MediaSorting.sortTracks(getTracks(), sorting);
        
        try {
            return new AudioPlaylistV1(getName(), tracks, getPlayingTrack(), _playing, _temporary);
        } catch (Exception e) {
            // This should never fail, but we need to handle this case
            return this;
        }
}

    // # Serializable

    private void writeObject(@NonNull ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(@NonNull ObjectInputStream in) throws IOException,ClassNotFoundException
    {
        in.defaultReadObject();
        
        _random = new Random();
    }
}
