package com.media.notabadplayer.Audio;

import android.media.MediaRecorder;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Storage.AudioInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class AudioPlaylist implements Serializable
{
    private final @NonNull String _name;
    
    private AudioPlayOrder _playOrder;
    
    private @NonNull ArrayList<AudioTrack> _tracks;
    
    private boolean _playing;
    private @NonNull AudioTrack _playingTrack;
    private int _playingTrackPosition;
    
    transient private Random _random = new Random();
    
    public AudioPlaylist(@NonNull String name, @NonNull AudioTrack track)
    {
        this(name, trackAsAList(track), track);
    }
    
    public AudioPlaylist(@NonNull String name, @NonNull ArrayList<AudioTrack> tracks) throws IllegalArgumentException
    {
        this(name, tracks, null);
    }
    
    public AudioPlaylist(@NonNull String name, @NonNull ArrayList<AudioTrack> tracks, AudioTrack playingTrack) throws IllegalArgumentException
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }
        
        _name = name;
        _tracks = tracks;
        _playing = true;
        _playingTrack = tracks.get(0);
        _playingTrackPosition = 0;
        _playOrder = AudioPlayOrder.FORWARDS;
        
        // Set proper source value
        boolean isAlbumList = isAlbumPlaylist();
        _tracks = new ArrayList<>();
        AudioTrack firstTrack = tracks.get(0);
        
        for (int e = 0; e < tracks.size(); e++)
        {
            AudioTrackSource source = isAlbumList ? AudioTrackSource.createAlbumSource(firstTrack.albumID) : AudioTrackSource.createPlaylistSource(this);
            _tracks.add(new AudioTrack(tracks.get(e), source));
        }
        
        if (playingTrack != null)
        {
            for (int e = 0; e < _tracks.size(); e++)
            {
                if (_tracks.get(e).equals(playingTrack))
                {
                    _playingTrackPosition = e;
                    _playingTrack = _tracks.get(e);
                    break;
                }
            }
        }
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
    
    public AudioPlayOrder getPlayOrder()
    {
        return _playOrder;
    }
    
    public void setPlayOrder(AudioPlayOrder order)
    {
        _playOrder = order;
    }
    
    public boolean isPlaying()
    {
        return _playing;
    }
    
    public @NonNull AudioTrack getPlayingTrack()
    {
        return _playingTrack;
    }

    public boolean isAlbumPlaylist()
    {
        return _name.equals(_tracks.get(0).albumTitle);
    }
    
    public AudioAlbum getAlbum(@NonNull AudioInfo audioInfo)
    {
        for (int e = 0; e < _tracks.size(); e++)
        {
            AudioAlbum album = audioInfo.getAlbumByID(_tracks.get(e).albumID);
            
            if (album != null)
            {
                return album;
            }
        }
        
        return null;
    }
    
    public AudioTrack goToTrackBasedOnPlayOrder()
    {
        _playing = true;
        
        switch (_playOrder)
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
        
        return _playingTrack;
    }

    public AudioTrack goToNextPlayingTrack()
    {
        _playing = true;
        
        if (_playingTrackPosition + 1 == _tracks.size())
        {
            _playing = false;
        }
        else
        {
            _playingTrackPosition++;
            _playingTrack = _tracks.get(_playingTrackPosition);
        }
        
        return _playingTrack;
    }

    public AudioTrack goToNextPlayingTrackRepeat()
    {
        _playing = true;
        
        if (_playingTrackPosition + 1 < _tracks.size())
        {
            goToNextPlayingTrack();
        }
        else
        {
            _playingTrack = _tracks.get(0);
            _playingTrackPosition = 0;
        }
        
        return _playingTrack;
    }

    public AudioTrack goToPreviousPlayingTrack()
    {
        _playing = true;
        
        if (_playingTrackPosition - 1 < 0)
        {
            _playingTrack = _tracks.get(0);
            _playingTrackPosition = 0;
            _playing = false;
        }
        else
        {
            _playingTrackPosition--;
            _playingTrack = _tracks.get(_playingTrackPosition);
        }
        
        return _playingTrack;
    }
    
    public AudioTrack goToTrackByShuffle()
    {
        _playing = true;
        
        int min = 0;
        int max = _tracks.size()-1;
        _playingTrackPosition = _random.nextInt((max - min) + 1) + min;
        _playingTrack = _tracks.get(_playingTrackPosition);
        
        return _playingTrack;
    }
    
    private void writeObject(@NonNull ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(@NonNull ObjectInputStream in) throws IOException,ClassNotFoundException
    {
        in.defaultReadObject();
        
        AudioTrack playingTrack = _playingTrack;
        _playingTrack = null;
        
        for (AudioTrack track : _tracks)
        {
            if (track.equals(playingTrack))
            {
                _playingTrack = track;
                break;
            }
        }
        
    }
}
