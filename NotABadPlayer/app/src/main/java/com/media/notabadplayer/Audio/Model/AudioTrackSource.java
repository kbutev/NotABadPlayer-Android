package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Storage.GeneralStorage;

public class AudioTrackSource implements Serializable
{
    private final @NonNull String _value;
    private final boolean _isAlbumSource;

    public static AudioTrackSource createAlbumSource(@NonNull String albumID)
    {
        return new AudioTrackSource(albumID, true);
    }
    
    public static AudioTrackSource createPlaylistSource(@NonNull String playlistName)
    {
        return new AudioTrackSource(playlistName, false);
    }
    
    private AudioTrackSource(@NonNull String value, boolean isAlbumSource)
    {
        this._value = value;
        this._isAlbumSource = isAlbumSource;
    }
    
    public boolean isAlbum()
    {
        return _isAlbumSource;
    }
    
    public boolean isPlaylist()
    {
        return !isAlbum();
    }

    public @Nullable AudioPlaylist getSourcePlaylist(@NonNull AudioInfo audioInfo, @Nullable BaseAudioTrack playingTrack)
    {
        if (isAlbum())
        {
            AudioAlbum album = audioInfo.getAlbumByID(_value);
            
            if (album == null)
            {
                return null;
            }

            try {
                return new AudioPlaylist(album.albumTitle, audioInfo.getAlbumTracks(album), playingTrack);
            } catch (Exception e1) {
                
            }
        }
        
        if (isPlaylist())
        {
            ArrayList<AudioPlaylist> playlists = GeneralStorage.getShared().getUserPlaylists();
            
            for (int e = 0; e < playlists.size(); e++)
            {
                if (_value.equals(playlists.get(e).getName()))
                {
                    AudioPlaylist pl = playlists.get(e);

                    try {
                        return new AudioPlaylist(pl.getName(), pl.getTracks(), playingTrack);
                    } catch (Exception e2) {

                    }
                }
            }
            
            return null;
        }
        
        return null;
    }
}
