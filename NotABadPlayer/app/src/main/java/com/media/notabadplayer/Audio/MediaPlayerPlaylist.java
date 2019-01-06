package com.media.notabadplayer.Audio;

import android.support.v4.math.MathUtils;

import java.util.ArrayList;
import java.util.Random;

public class MediaPlayerPlaylist
{
    private MediaPlayerPlaylistPlayOrder _playOrder;
    
    private ArrayList<MediaTrack> _tracks;
    
    private MediaTrack _playingTrack;
    private int _playingTrackPosition;
    
    private Random _random = new Random();
    
    public MediaPlayerPlaylist(MediaTrack track)
    {
        _tracks = new ArrayList<>();
        _tracks.add(track);
        _playingTrack = track;
        _playingTrackPosition = 0;
        _playOrder = MediaPlayerPlaylistPlayOrder.ONCE_FOREVER;
    }

    public MediaPlayerPlaylist(ArrayList<MediaTrack> tracks) throws IllegalArgumentException
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Given playlist tracks must not be empty");
        }
        
        _tracks = tracks;
        _playingTrack = tracks.get(0);
        _playingTrackPosition = 0;
        _playOrder = MediaPlayerPlaylistPlayOrder.ONCE_FOREVER;
    }
    
    public MediaPlayerPlaylistPlayOrder getPlayOrder()
    {
        return _playOrder;
    }
    
    public void setPlayOrder(MediaPlayerPlaylistPlayOrder order)
    {
        _playOrder = order;
    }
    
    public MediaTrack getPlayingTrack()
    {
        return _playingTrack;
    }

    public MediaTrack goToNextPlayingTrack()
    {
        switch (_playOrder)
        {
            case ONCE:
                _playingTrack = null;
                break;
            case ONCE_FOREVER:
                break;
            case FORWARDS:
                if (_playingTrackPosition + 1 == _tracks.size())
                {
                    _playingTrack = null;
                    _playingTrackPosition = 0;
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
        if (_playingTrackPosition - 1 < 0)
        {
            _playingTrack = _tracks.get(0);
            _playingTrackPosition = 0;
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
        _playingTrackPosition = _random.nextInt() % (_tracks.size()-1);
        _playingTrack = _tracks.get(_playingTrackPosition);
        
        return _playingTrack;
    }
}
