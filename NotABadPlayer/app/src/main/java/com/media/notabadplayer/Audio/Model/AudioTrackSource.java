package com.media.notabadplayer.Audio.Model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Utilities.HashUtilities;
import com.media.notabadplayer.Storage.GeneralStorage;

public class AudioTrackSource implements Serializable
{
    private static AudioTrackSourceCache _cache;

    private final @NonNull String _value;
    private final boolean _isAlbumSource;

    public static AudioTrackSource createAlbumSource(@NonNull String albumID)
    {
        AudioTrackSourceCache cache = getCache();
        albumID = cache.getSourceValueFlyweight(albumID);
        AudioTrackSource source = new AudioTrackSource(albumID, true);
        return cache.getFlyweight(source);
    }
    
    public static AudioTrackSource createPlaylistSource(@NonNull String playlistName)
    {
        AudioTrackSourceCache cache = getCache();
        playlistName = cache.getSourceValueFlyweight(playlistName);
        AudioTrackSource source = new AudioTrackSource(playlistName, false);
        return cache.getFlyweight(source);
    }
    
    private AudioTrackSource(@NonNull String value, boolean isAlbumSource)
    {
        this._value = value;
        this._isAlbumSource = isAlbumSource;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof AudioTrackSource))
        {
            return false;
        }

        AudioTrackSource otherSource = (AudioTrackSource) other;

        if (!this._value.equals(otherSource._value))
        {
            return false;
        }

        return this._isAlbumSource == otherSource._isAlbumSource;
    }

    @Override
    public int hashCode()
    {
        return HashUtilities.hashObjects(_value, _isAlbumSource);
    }

    public @NonNull String getValue()
    {
        return _value;
    }

    public boolean isAlbum()
    {
        return _isAlbumSource;
    }
    
    public boolean isPlaylist()
    {
        return !isAlbum();
    }

    public @Nullable BaseAudioPlaylist getSourcePlaylist(@NonNull AudioInfo audioInfo, @Nullable BaseAudioTrack playingTrack)
    {
        if (isAlbum())
        {
            AudioAlbum album = audioInfo.getAlbumByID(_value);
            
            if (album == null)
            {
                return null;
            }

            try {
                BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
                node.setName(album.albumTitle);
                node.setTracks(audioInfo.getAlbumTracks(album));

                if (playingTrack != null)
                {
                    node.setPlayingTrack(playingTrack);
                }

                return node.build();
            } catch (Exception e1) {
                
            }
        }
        
        if (isPlaylist())
        {
            List<BaseAudioPlaylist> playlists = GeneralStorage.getShared().getUserPlaylists();
            
            for (int e = 0; e < playlists.size(); e++)
            {
                if (_value.equals(playlists.get(e).getName()))
                {
                    BaseAudioPlaylist playlist = playlists.get(e);

                    try {
                        BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
                        node.setName(playlist.getName());
                        node.setTracks(playlist.getTracks());

                        if (playingTrack != null)
                        {
                            node.setPlayingTrack(playingTrack);
                        }

                        return node.build();
                    } catch (Exception e2) {

                    }
                }
            }
            
            return null;
        }
        
        return null;
    }

    private static synchronized @NonNull AudioTrackSourceCache getCache()
    {
        if (_cache == null)
        {
            _cache = new AudioTrackSourceCache();
        }

        return _cache;
    }
}

class AudioTrackSourceCache {
    private final Object _lock = new Object();

    private HashSet<AudioTrackSource> _sourceCache = new HashSet<>();
    private HashSet<String> _sourceValuesCache = new HashSet<>();

    public @NonNull AudioTrackSource getFlyweight(@NonNull AudioTrackSource trackSource)
    {
        synchronized (_lock)
        {
            // Return from cache, if already cached
            for (AudioTrackSource source : _sourceCache)
            {
                if (source.equals(trackSource))
                {
                    return source;
                }
            }

            // Otherwise add the given value and return it
            _sourceCache.add(trackSource);

            return trackSource;
        }
    }

    public String getSourceValueFlyweight(@Nullable String value)
    {
        synchronized (_lock)
        {

            if (value == null)
            {
                return null;
            }

            // Return from cache, if already cached
            for (String sourceValue : _sourceValuesCache)
            {
                if (sourceValue.equals(value))
                {
                    return sourceValue;
                }
            }

            // Otherwise add the given value and return it
            _sourceValuesCache.add(value);

            return value;
        }
    }
}
