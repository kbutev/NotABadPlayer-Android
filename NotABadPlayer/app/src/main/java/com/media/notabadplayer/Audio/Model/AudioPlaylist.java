package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class AudioPlaylist implements Serializable
{
    private final @NonNull String _name;
    
    private @NonNull ArrayList<BaseAudioTrack> _tracks;
    
    private boolean _playing;
    private int _playingTrackPosition;
    
    transient private Random _random;

    public AudioPlaylist(@NonNull String name, @NonNull List<BaseAudioTrack> tracks)
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }

        _name = name;

        // Fill the list with all the given tracks,
        // just so we can determine if its an album list or not
        _tracks = new ArrayList<>(tracks);
        _playing = false;
        _playingTrackPosition = 0;

        // Is album list?
        boolean isAlbumList = isAlbumPlaylist();
        _tracks = new ArrayList<>();

        // Copy the given tracks
        BaseAudioTrack firstTrack = tracks.get(0);

        for (int e = 0; e < tracks.size(); e++)
        {
            AudioTrackSource source = isAlbumList ? AudioTrackSource.createAlbumSource(firstTrack.getAlbumID()) : AudioTrackSource.createPlaylistSource(_name);

            BaseAudioTrack track = tracks.get(e);
            BaseAudioTrackBuilderNode clone = AudioTrackBuilder.start(track);
            clone.setSource(source);

            try {
                _tracks.add(clone.build());
            } catch (Exception exc) {
                Log.v(AudioPlaylist.class.getCanonicalName(), "Failed to copy audio track " + track.getFilePath());
            }
        }

        _random = new Random();
    }
    
    public AudioPlaylist(@NonNull String name, @NonNull BaseAudioTrack startWithTrack)
    {
        this(name, trackAsAList(startWithTrack));
        
        if (hasTrack(startWithTrack))
        {
            goToTrack(startWithTrack);
        }
    }
    
    public AudioPlaylist(@NonNull String name,
                         @NonNull List<BaseAudioTrack> tracks,
                         @Nullable BaseAudioTrack startWithTrack) throws Exception
    {
        this(name, tracks, startWithTrack, AppSettings.TrackSorting.NONE);
    }

    public AudioPlaylist(@NonNull String name,
                         @NonNull List<BaseAudioTrack> tracks,
                         AppSettings.TrackSorting sorting)
    {
        this(name, MediaSorting.sortTracks(tracks, sorting));
    }
    
    public AudioPlaylist(@NonNull String name,
                         @NonNull List<BaseAudioTrack> tracks,
                         @Nullable BaseAudioTrack startWithTrack,
                         AppSettings.TrackSorting sorting) throws Exception
    {
        this(name, MediaSorting.sortTracks(tracks, sorting));

        if (startWithTrack != null)
        {
            if (hasTrack(startWithTrack))
            {
                goToTrack(startWithTrack);
            }
            else
            {
                throw new IllegalArgumentException("Playlist cannot be created with a starting track not included in the given tracks");
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof AudioPlaylist)
        {
            AudioPlaylist playlist = (AudioPlaylist)o;
            
            return _name.equals(playlist._name) && _playingTrackPosition == playlist._playingTrackPosition && _tracks.equals(playlist._tracks);
        }

        return false;
    }

    public @NonNull AudioPlaylist sortedPlaylist(AppSettings.TrackSorting sorting)
    {
        AudioPlaylist playlist = new AudioPlaylist(getName(), getTracks(), sorting);
        playlist.goToTrack(getPlayingTrack());
        return playlist;
    }
    
    private static ArrayList<BaseAudioTrack> trackAsAList(@NonNull BaseAudioTrack track)
    {
        ArrayList<BaseAudioTrack> tracks = new ArrayList<>();
        tracks.add(track);
        return tracks;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public int size()
    {
        return _tracks.size();
    }
    
    public @NonNull ArrayList<BaseAudioTrack> getTracks()
    {
        return new ArrayList<>(_tracks);
    }
    
    public final BaseAudioTrack getTrack(int index)
    {
        return _tracks.get(index);
    }
    
    public boolean isPlaying()
    {
        return _playing;
    }
    
    public @NonNull BaseAudioTrack getPlayingTrack()
    {
        return _tracks.get(_playingTrackPosition);
    }
    
    public boolean isAlbumPlaylist()
    {
        return _name.equals(_tracks.get(0).getAlbumTitle());
    }
    
    public boolean isPlayingFirstTrack()
    {
        return _playingTrackPosition == 0;
    }

    public boolean isPlayingLastTrack()
    {
        return _playingTrackPosition + 1 == _tracks.size();
    }

    public boolean hasTrack(@NonNull BaseAudioTrack track)
    {
        return _tracks.contains(track);
    }
    
    public void playCurrent()
    {
        _playing = true;
    }
    
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
        }
        else
        {
            Log.v(AudioPlaylist.class.getCanonicalName(), "Cannot go to track, tracks list does not contain given track " + track.getFilePath());
        }
    }
    
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
    
    public void goToTrackByShuffle()
    {
        _playing = true;
        
        int min = 0;
        int max = _tracks.size()-1;
        _playingTrackPosition = _random.nextInt((max - min) + 1) + min;
    }
    
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
