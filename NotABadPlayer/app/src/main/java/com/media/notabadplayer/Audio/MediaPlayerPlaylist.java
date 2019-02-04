package com.media.notabadplayer.Audio;

import android.support.v4.math.MathUtils;

import java.util.ArrayList;
import java.util.Random;

public class MediaPlayerPlaylist
{
    private MediaPlayerPlaylistPlayOrder _playOrder;
    
    private ArrayList<MediaTrack> _tracks;
    
    private boolean _playing;
    private MediaTrack _playingTrack;
    private int _playingTrackPosition;
    
    private Random _random = new Random();
    
    public MediaPlayerPlaylist(MediaTrack track)
    {
        _tracks = new ArrayList<>();
        _tracks.add(track);
        _playing = true;
        _playingTrack = track;
        _playingTrackPosition = 0;
        _playOrder = MediaPlayerPlaylistPlayOrder.FORWARDS;
    }

    public MediaPlayerPlaylist(ArrayList<MediaTrack> tracks, String playingTrack) throws IllegalArgumentException
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }
        
        _tracks = tracks;
        _playing = false;
        _playingTrack = tracks.get(0);
        _playingTrackPosition = 0;
        _playOrder = MediaPlayerPlaylistPlayOrder.FORWARDS;
        
        if (playingTrack != null)
        {
            for (int e = 0; e < _tracks.size(); e++)
            {
                if (_tracks.get(e).title.equals(playingTrack))
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
    
    public final MediaTrack getTrack(int index)
    {
        return _tracks.get(index);
    }
    
    public MediaPlayerPlaylistPlayOrder getPlayOrder()
    {
        return _playOrder;
    }
    
    public void setPlayOrder(MediaPlayerPlaylistPlayOrder order)
    {
        _playOrder = order;
    }
    
    public boolean isPlaying()
    {
        return _playing;
    }
    
    public MediaTrack getPlayingTrack()
    {
        return _playingTrack;
    }

    public MediaTrack goToNextPlayingTrack()
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
                if (_playingTrackPosition + 1 == _tracks.size())
                {
                    _playing = false;
                }
                else
                {
                    _playingTrackPosition++;
                    _playingTrack = _tracks.get(_playingTrackPosition);
                }
                break;
            case FORWARDS_REPEAT:
                if (_playingTrackPosition + 1 == _tracks.size())
                {
                    _playingTrack = _tracks.get(0);
                    _playingTrackPosition = 0;
                }
                break;
            case SHUFFLE:
                goToTrackByShuffle();
                break;
        }
        
        return _playingTrack;
    }

    public MediaTrack goToPreviousPlayingTrack()
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
    
    public MediaTrack goToTrackByShuffle()
    {
        int min = 0;
        int max = _tracks.size()-1;
        _playingTrackPosition = _random.nextInt((max - min) + 1) + min;
        _playingTrack = _tracks.get(_playingTrackPosition);
        
        _playing = true;
        
        return _playingTrack;
    }
}
