package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.media.notabadplayer.Utilities.Serializing;

public class AudioPlaylistBuilder {
    public static @NonNull BaseAudioPlaylistBuilderNode start()
    {
        return new AudioPlaylistBuilderNode();
    }

    public static @NonNull BaseAudioPlaylistBuilderNode start(@NonNull BaseAudioPlaylist prototype)
    {
        return new AudioPlaylistBuilderNode(prototype);
    }

    public static @NonNull MutableAudioPlaylist buildMutableFromImmutable(@NonNull BaseAudioPlaylist prototype) throws Exception
    {
        return new AudioPlaylistBuilderNode(prototype).buildMutable();
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
    private @NonNull String name;
    private @NonNull List<BaseAudioTrack> tracks;
    private boolean isPlaying;
    private int playingTrackIndex;
    private boolean isTemporary;

    AudioPlaylistBuilderNode()
    {
        name = "";
        tracks = new ArrayList<>();
        playingTrackIndex = 0;
        isTemporary = false;
    }

    AudioPlaylistBuilderNode(@NonNull BaseAudioPlaylist prototype)
    {
        name = prototype.getName();
        tracks = prototype.getTracks();
        isPlaying = prototype.isPlaying();
        playingTrackIndex = prototype.getPlayingTrackIndex();
        isTemporary = prototype.isTemporary();
    }

    @Override
    public @NonNull BaseAudioPlaylist build() throws Exception
    {
        return buildMutable();
    }

    @Override
    public @NonNull MutableAudioPlaylist buildMutable() throws Exception
    {
        if (tracks.size() == 0)
        {
            throw new IllegalArgumentException("Cannot build playlist with zero tracks");
        }

        return new AudioPlaylistV1(name, tracks, playingTrackIndex, isPlaying, isTemporary);
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
    public void setTracksToOneTrack(@NonNull BaseAudioTrack singleTrack)
    {
        ArrayList<BaseAudioTrack> tracks = new ArrayList<>();
        tracks.add(singleTrack);
        this.tracks = tracks;
    }
    
    @Override
    public void setPlayingTrack(@NonNull BaseAudioTrack playingTrack)
    {
        int index = tracks.indexOf(playingTrack);

        if (index == -1)
        {
            this.playingTrackIndex = 0;
            return;
        }

        this.playingTrackIndex = index;
    }
    
    @Override
    public void setPlayingTrackPosition(int trackIndex)
    {
        this.playingTrackIndex = trackIndex;
    }

    @Override
    public void setIsPlaying(boolean playing)
    {
        this.isPlaying = playing;
    }

    @Override
    public void setIsTemporaryPlaylist(boolean temporary)
    {
        isTemporary = temporary;
    }
}
