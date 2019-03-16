package com.media.notabadplayer.Audio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Storage.AudioInfo;

import java.io.Serializable;

public class AudioTrackSource implements Serializable
{
    public static final String PLAYLIST_PREFIX = "Playlist::";
    
    private final String value;

    public static AudioTrackSource create(AudioAlbum album)
    {
        return new AudioTrackSource(album.albumID);
    }
    
    public static AudioTrackSource create(AudioPlaylist playlist)
    {
        return new AudioTrackSource(PLAYLIST_PREFIX + playlist.getName());
    }
    
    private AudioTrackSource(String value)
    {
        this.value = value;
    }
    
    public boolean isAlbum()
    {
        return !value.startsWith(PLAYLIST_PREFIX);
    }
    
    public boolean isPlaylist()
    {
        return !isAlbum();
    }
    
    public boolean isValid()
    {
        return true;
    }
    
    public AudioPlaylist getSourcePlaylist(@NonNull AudioInfo audioInfo, @Nullable AudioTrack playingTrack)
    {
        if (!isValid())
        {
            return null;
        }
        
        if (isAlbum())
        {
            AudioAlbum album = audioInfo.getAlbumByID(value);
            return new AudioPlaylist(album.albumTitle, audioInfo.getAlbumTracks(album), playingTrack);
        }
        
        if (isPlaylist())
        {
            
        }
        
        return null;
    }
}
