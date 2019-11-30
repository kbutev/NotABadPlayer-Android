package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Utilities.StringUtilities;

public class AudioTrackBuilder {
    public static @NonNull BaseAudioTrackBuilderNode start()
    {
        return new AudioTrackBuilderNode();
    }

    public static @NonNull BaseAudioTrackBuilderNode start(@NonNull BaseAudioTrack prototype)
    {
        return new AudioTrackBuilderNode(prototype);
    }

    public static @NonNull List<BaseAudioTrack> buildListLatestVersionFromSerializedData(String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull List<BaseAudioTrack> buildListVersion1FromSerializedData(String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull List<BaseAudioTrack> buildArrayListFromSerializedData(String data) throws Exception
    {
        Object result = Serializing.deserializeObject(data);

        if (result instanceof ArrayList)
        {
            ArrayList array = (ArrayList)result;

            if (array.size() > 0)
            {
                if (array.get(0) instanceof AudioTrackV1)
                {
                    @SuppressWarnings("unchecked")
                    List<BaseAudioTrack> tracksArray = (List<BaseAudioTrack>)result;
                    return tracksArray;
                }

                throw new ClassNotFoundException("Cannot deserialize audio track, unrecognized class type");
            }
        }

        return new ArrayList<>();
    }
}

class AudioTrackBuilderNode implements BaseAudioTrackBuilderNode {
    private AudioTrackV1 track;

    AudioTrackBuilderNode()
    {
        this.track = new AudioTrackV1();
    }

    AudioTrackBuilderNode(@NonNull BaseAudioTrack prototype)
    {
        this.track = new AudioTrackV1();
        this.track.filePath = prototype.getFilePath();
        this.track.title = prototype.getTitle();
        this.track.artist = prototype.getArtist();
        this.track.albumTitle = prototype.getAlbumTitle();
        this.track.albumID = prototype.getAlbumID();
        this.track.artCover = prototype.getArtCover();
        this.track.trackNum = prototype.getTrackNum();
        this.track.duration = prototype.getDuration();
        this.track.durationInSeconds = prototype.getDurationInSeconds();
        this.track.source = prototype.getSource();
        this.track.lyrics = prototype.getLyrics();
        this.track.numberOfTimesPlayed = prototype.getNumberOfTimesPlayed();
        this.track.totalTimesPlayed = prototype.getTotalTimePlayed();
        this.track.date = prototype.getDate();
    }

    @Override
    @NonNull public BaseAudioTrack build() throws Exception {
        return this.track;
    }

    @Override
    public void setFilePath(@NonNull String value) {
        this.track.filePath = value;
    }

    @Override
    public void setTitle(@NonNull String value) {
        this.track.title = value;
    }

    @Override
    public void setArtist(@NonNull String value) {
        this.track.artist = value;
    }

    @Override
    public void setAlbumTitle(@NonNull String value) {
        this.track.albumTitle = value;
    }

    @Override
    public void setAlbumID(@NonNull String value) {
        this.track.albumID = value;
    }

    @Override
    public void setArtCover(@NonNull String value) {
        this.track.artCover = value;
    }

    @Override
    public void setTrackNum(int number) {
        this.track.trackNum = number;
    }

    @Override
    public void setDuration(double duration) {
        this.track.durationInSeconds = duration;
        this.track.duration = StringUtilities.secondsToString(duration);
    }

    @Override
    public void setSource(AudioTrackSource source) {
        this.track.source = source;
    }

    @Override
    public void setLyrics(@NonNull String value) {
        this.track.lyrics = value;
    }

    @Override
    public void setNumberOfTimesPlayed(int count) {
        this.track.numberOfTimesPlayed = count;
    }

    @Override
    public void setTotalTimePlayed(int count) {
        this.track.totalTimesPlayed = count;
    }

    @Override
    public void setDateAdded(@NonNull Date value)
    {
        this.track.date = AudioTrackV1.buildDefaultAudioTrackDate(value,
                this.track.date.getModified(),
                this.track.date.getFirstPlayed(),
                this.track.date.getLastPlayed());
    }

    @Override
    public void setDateModified(@NonNull Date value)
    {
        this.track.date = AudioTrackV1.buildDefaultAudioTrackDate(this.track.date.getAdded(),
                value,
                this.track.date.getFirstPlayed(),
                this.track.date.getLastPlayed());
    }

    @Override
    public void setDateFirstPlayed(@NonNull Date value)
    {
        this.track.date = AudioTrackV1.buildDefaultAudioTrackDate(this.track.date.getAdded(),
                this.track.date.getModified(),
                value,
                this.track.date.getLastPlayed());
    }

    @Override
    public void setDateLastAdded(@NonNull Date value)
    {
        this.track.date = AudioTrackV1.buildDefaultAudioTrackDate(this.track.date.getAdded(),
                this.track.date.getModified(),
                this.track.date.getFirstPlayed(),
                value);
    }
}

