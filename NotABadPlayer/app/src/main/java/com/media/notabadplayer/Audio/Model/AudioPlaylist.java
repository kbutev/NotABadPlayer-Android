package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Utilities.MediaSorting;

public class AudioPlaylist implements Serializable
{
    private final @NonNull String _name;
    
    private @NonNull ArrayList<AudioTrack> _tracks;
    
    private boolean _playing;
    private int _playingTrackPosition;
    
    transient private Random _random;
    
    public AudioPlaylist(@NonNull String name, @NonNull AudioTrack startWithTrack) throws Exception
    {
        this(name, trackAsAList(startWithTrack), startWithTrack);
    }
    
    public AudioPlaylist(@NonNull String name, @NonNull List<AudioTrack> tracks)
    {
        this(name, tracks, null, false);
    }

    public AudioPlaylist(@NonNull String name,
                         @NonNull List<AudioTrack> tracks,
                         AppSettings.TrackSorting sorting)
    {
        this(name, MediaSorting.sortTracks(tracks, sorting), null, false);
    }

    public AudioPlaylist(@NonNull String name,
                         @NonNull List<AudioTrack> tracks,
                         AudioTrack startWithTrack,
                         AppSettings.TrackSorting sorting) throws Exception
    {
        this(name, MediaSorting.sortTracks(tracks, sorting), startWithTrack);
    }

    public AudioPlaylist(@NonNull String name,
                         @NonNull List<AudioTrack> tracks,
                         AudioTrack startWithTrack) throws Exception
    {
        this(name, tracks, startWithTrack, false);
    }

    private AudioPlaylist(@NonNull String name,
                          @NonNull List<AudioTrack> tracks,
                          AudioTrack startWithTrack,
                          boolean dummy)
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
        
        if (startWithTrack != null)
        {
            boolean trackWasFound = false;
            
            for (int e = 0; e < _tracks.size(); e++)
            {
                if (_tracks.get(e).equals(startWithTrack))
                {
                    _playingTrackPosition = e;
                    trackWasFound = true;
                    break;
                }
            }
            
            if (!trackWasFound)
            {
                throw new IllegalArgumentException("Playlist cannot be created with a starting track that is not part of the given playlist tracks");
            }
        }

        _random = new Random();
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
