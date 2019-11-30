package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Utilities.Serializing;

import java.util.ArrayList;
import java.util.List;

public class AudioPlaylistBuilder {
    public static @NonNull BaseAudioPlaylistBuilderNode start()
    {
        return new AudioPlaylistBuilderNode();
    }

    public static @NonNull ArrayList<BaseAudioPlaylist> buildListLatestVersionFromSerializedData(String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull ArrayList<BaseAudioPlaylist> buildListVersion1FromSerializedData(String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull ArrayList<BaseAudioPlaylist> buildArrayListFromSerializedData(String data) throws Exception
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

    AudioPlaylistBuilderNode()
    {
        name = "";
        tracks = new ArrayList<>();
        sorting = AppSettings.TrackSorting.NONE;
    }

    @Override
    public @NonNull BaseAudioPlaylist build() throws Exception
    {
        if (startWithTrack != null)
        {
            // Playlist with one single track
            if (tracks.size() == 0)
            {
                return new AudioPlaylistV1(name, startWithTrack);
            }

            return new AudioPlaylistV1(name, tracks, startWithTrack, sorting);
        }

        return new AudioPlaylistV1(name, tracks, sorting);
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
    public void setSorting(AppSettings.TrackSorting sorting)
    {
        this.sorting = sorting;
    }

    @Override
    public void setStartingTrack(@Nullable BaseAudioTrack startWithTrack)
    {
        this.startWithTrack = startWithTrack;
    }
}
