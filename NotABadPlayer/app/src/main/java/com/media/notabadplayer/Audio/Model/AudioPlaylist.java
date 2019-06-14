package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    
    private @NonNull ArrayList<AudioTrack> _tracks;
    
    private boolean _playing;
    private int _playingTrackPosition;
    
    transient private Random _random;

    public AudioPlaylist(@NonNull String name, @NonNull List<AudioTrack> tracks)
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }

        _name = name;
        _tracks = new ArrayList<>(tracks);
        _playing = false;
        _playingTrackPosition = 0;

        // Set proper source value
        boolean isAlbumList = isAlbumPlaylist();
        _tracks = new ArrayList<>();
        AudioTrack firstTrack = tracks.get(0);

        for (int e = 0; e < tracks.size(); e++)
        {
            AudioTrackSource source = isAlbumList ? AudioTrackSource.createAlbumSource(firstTrack.albumID) : AudioTrackSource.createPlaylistSource(_name);
            _tracks.add(new AudioTrack(tracks.get(e), source));
        }

        _random = new Random();
    }
    
    public AudioPlaylist(@NonNull String name, @NonNull AudioTrack startWithTrack)
    {
        this(name, trackAsAList(startWithTrack));
        
        if (hasTrack(startWithTrack))
        {
            goToTrack(startWithTrack);
        }
    }
    
    public AudioPlaylist(@NonNull String name,
                         @NonNull List<AudioTrack> tracks,
                         @Nullable AudioTrack startWithTrack) throws Exception
    {
        this(name, tracks, startWithTrack, AppSettings.TrackSorting.TITLE);
    }

    public AudioPlaylist(@NonNull String name,
                         @NonNull List<AudioTrack> tracks,
                         AppSettings.TrackSorting sorting)
    {
        this(name, MediaSorting.sortTracks(tracks, sorting));
    }
    
    public AudioPlaylist(@NonNull String name,
                         @NonNull List<AudioTrack> tracks,
                         @Nullable AudioTrack startWithTrack,
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

    public @NonNull AudioPlaylist sortedPlaylist(AppSettings.TrackSorting sorting)
    {
        AudioPlaylist playlist = new AudioPlaylist(getName(), getTracks(), sorting);
        playlist._playingTrackPosition = _playingTrackPosition;
        return playlist;
    }
    
    private static ArrayList<AudioTrack> trackAsAList(@NonNull AudioTrack track)
    {
        ArrayList<AudioTrack> tracks = new ArrayList<>();
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
    
    public @NonNull ArrayList<AudioTrack> getTracks()
    {
        return new ArrayList<>(_tracks);
    }
    
    public final AudioTrack getTrack(int index)
    {
        return _tracks.get(index);
    }
    
    public boolean isPlaying()
    {
        return _playing;
    }
    
    public @NonNull AudioTrack getPlayingTrack()
    {
        return _tracks.get(_playingTrackPosition);
    }
    
    public boolean isAlbumPlaylist()
    {
        return _name.equals(_tracks.get(0).albumTitle);
    }
    
    public boolean isPlayingFirstTrack()
    {
        return _playingTrackPosition == 0;
    }

    public boolean isPlayingLastTrack()
    {
        return _playingTrackPosition + 1 == _tracks.size();
    }

    public boolean hasTrack(@NonNull AudioTrack track)
    {
        return _tracks.indexOf(track) != -1;
    }
    
    public void playCurrent()
    {
        _playing = true;
    }
    
    public void goToTrack(@NonNull AudioTrack track)
    {
        int index = _tracks.indexOf(track);
        
        if (index != -1)
        {
            _playing = true;
            _playingTrackPosition = index;
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
