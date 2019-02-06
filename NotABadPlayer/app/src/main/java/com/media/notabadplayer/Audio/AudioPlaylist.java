package com.media.notabadplayer.Audio;

import java.util.ArrayList;
import java.util.Random;

public class AudioPlaylist
{
    private AudioPlayOrder _playOrder;
    
    private ArrayList<AudioTrack> _tracks;
    
    private boolean _playing;
    private AudioTrack _playingTrack;
    private int _playingTrackPosition;
    
    private Random _random = new Random();
    
    public AudioPlaylist(AudioTrack track)
    {
        _tracks = new ArrayList<>();
        _tracks.add(track);
        _playing = true;
        _playingTrack = track;
        _playingTrackPosition = 0;
        _playOrder = AudioPlayOrder.FORWARDS;
    }

    public AudioPlaylist(ArrayList<AudioTrack> tracks) throws IllegalArgumentException
    {
        this(tracks, null);
    }

    public AudioPlaylist(ArrayList<AudioTrack> tracks, AudioTrack playingTrack) throws IllegalArgumentException
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }
        
        _tracks = tracks;
        _playing = true;
        _playingTrack = tracks.get(0);
        _playingTrackPosition = 0;
        _playOrder = AudioPlayOrder.FORWARDS;
        
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
    
    public int size()
    {
        return _tracks.size();
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
    
    public AudioTrack getPlayingTrack()
    {
        return _playingTrack;
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
    
    public ArrayList<String> getTracksAsStrings()
    {
        ArrayList<String> tracks = new ArrayList<>();

        for (int e = 0; e < size(); e++)
        {
            tracks.add(getTrack(e).toString());
        }
        
        return tracks;
    }

    public String getPlayingTrackAsString()
    {
        return _playingTrack != null ? _playingTrack.toString() : "";
    }
}
