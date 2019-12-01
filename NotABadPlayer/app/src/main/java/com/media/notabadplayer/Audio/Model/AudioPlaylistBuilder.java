package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Utilities.Serializing;

public class AudioPlaylistBuilder {
    public static @NonNull BaseAudioPlaylistBuilderNode start()
    {
        return new AudioPlaylistBuilderNode();
    }

    public static @NonNull ArrayList<BaseAudioPlaylist> buildArrayListLatestVersionFromSerializedData(@NonNull String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull ArrayList<BaseAudioPlaylist> buildArrayListVersion1FromSerializedData(@NonNull String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull ArrayList<BaseAudioPlaylist> buildArrayListFromSerializedData(@NonNull String data) throws Exception
    {
        Object result = Serializing.deserializeObject(data);

        if (result instanceof ArrayList)
        {
            ArrayList array = (ArrayList)result;

            if (array.size() > 0)
            {
                if (array.get(0) instanceof AudioPlaylistV1)
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<BaseAudioPlaylist> playlistsList = (ArrayList<BaseAudioPlaylist>)result;
                    return playlistsList;
                }

                throw new ClassNotFoundException("Cannot deserialize playlist, unrecognized class type");
            }
        }

        return new ArrayList<>();
    }
}

class AudioPlaylistBuilderNode implements BaseAudioPlaylistBuilderNode {
    private String name;
    private List<BaseAudioTrack> tracks;
    private AppSettings.TrackSorting sorting;
    private @Nullable BaseAudioTrack startWithTrack;
    private boolean isTemporary;

    AudioPlaylistBuilderNode()
    {
        name = "";
        tracks = new ArrayList<>();
        sorting = AppSettings.TrackSorting.NONE;
        isTemporary = false;
    }

    @Override
    public @NonNull BaseAudioPlaylist build() throws Exception
    {
        if (startWithTrack != null)
        {
            // Playlist with one single track
            if (tracks.size() == 0)
            {
                AudioPlaylistV1 playlist = new AudioPlaylistV1(name, startWithTrack);
                playlist.setIsTemporatyPlaylist(isTemporary);
                return playlist;
            }

            AudioPlaylistV1 playlist = new AudioPlaylistV1(name, tracks, startWithTrack, sorting);
            playlist.setIsTemporatyPlaylist(isTemporary);
            return playlist;
        }

        AudioPlaylistV1 playlist = new AudioPlaylistV1(name, tracks, sorting);
        playlist.setIsTemporatyPlaylist(isTemporary);
        return playlist;
    }

    @Override
    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    @Override
    public void setTracks(@NonNull List<BaseAudioTrack> tracks)
    {
        this.tracks = tracks;
    }

    @Override
    public void setStartingTrack(@Nullable BaseAudioTrack startWithTrack)
    {
        this.startWithTrack = startWithTrack;
    }

    @Override
    public void setIsTemporaryPlaylist(boolean temporary)
    {
        isTemporary = temporary;
    }
}
