package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

import com.media.notabadplayer.Utilities.Serializing;

public class AudioTrackBuilder {
    public static @NonNull BaseAudioTrackBuilderNode start()
    {
        return new AudioTrackBuilderNode();
    }

    public static @NonNull BaseAudioTrackBuilderNode start(@NonNull BaseAudioTrack prototype)
    {
        return new AudioTrackBuilderNode(prototype);
    }

    public static @NonNull ArrayList<BaseAudioTrack> buildArrayListLatestVersionFromSerializedData(@NonNull String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull ArrayList<BaseAudioTrack> buildArrayListVersion1FromSerializedData(@NonNull String data) throws Exception
    {
        return buildArrayListFromSerializedData(data);
    }

    public static @NonNull ArrayList<BaseAudioTrack> buildArrayListFromSerializedData(@NonNull String data) throws Exception
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
                    ArrayList<BaseAudioTrack> tracksArray = (ArrayList<BaseAudioTrack>)result;
                    return tracksArray;
                }

                throw new ClassNotFoundException("Cannot deserialize audio track, unrecognized class type");
            }
        }

        return new ArrayList<>();
    }
}

class AudioTrackBuilderNode implements BaseAudioTrackBuilderNode {
    private static BaseAudioTrack genericOrigin = new AudioTrackV1();
    private static Date genericDate = AudioTrackDateBuilder.getGenericDate();
    
    private @NonNull BaseAudioTrack template;
    
    private AudioTrackV1 result;
    private @NonNull Date resultDateAdded = genericDate;
    private @NonNull Date resultDateModified = genericDate;
    private @Nullable Date resultDateFirstPlayed;
    private @Nullable Date resultDateLastPlayed;
    
    AudioTrackBuilderNode()
    {
        this.template = genericOrigin;
        reset();
    }

    AudioTrackBuilderNode(@NonNull BaseAudioTrack prototype)
    {
        this.template = prototype;
        reset();
    }

    @Override
    public @NonNull BaseAudioTrack build() throws Exception {
        this.result.date = AudioTrackDateBuilder.build(this.resultDateAdded,
                this.resultDateModified,
                this.resultDateFirstPlayed,
                this.resultDateLastPlayed);
        
        return this.result;
    }
    
    @Override
    public void reset() {
        this.result = new AudioTrackV1(template);
        this.resultDateAdded = this.result.getDate().getAdded();
        this.resultDateModified = this.result.getDate().getModified();
        this.resultDateFirstPlayed = this.result.getDate().getFirstPlayed();
        this.resultDateLastPlayed = this.result.getDate().getLastPlayed();
    }

    @Override
    public void setFilePath(@NonNull String value) {
        this.result.filePath = value;
    }

    @Override
    public void setTitle(@NonNull String value) {
        this.result.title = value;
    }

    @Override
    public void setArtist(@NonNull String value) {
        this.result.artist = value;
    }

    @Override
    public void setAlbumTitle(@NonNull String value) {
        this.result.albumTitle = value;
    }

    @Override
    public void setAlbumID(@NonNull String value) {
        this.result.albumID = value;
    }

    @Override
    public void setArtCover(@NonNull String value) {
        this.result.artCover = value;
    }

    @Override
    public void setTrackNum(int number) {
        this.result.trackNum = number;
    }

    @Override
    public void setDuration(double duration) {
        this.result.durationInSeconds = duration;
    }

    @Override
    public void setSource(AudioTrackSource source) {
        this.result.source = source;
    }

    @Override
    public void setLyrics(@NonNull String value) {
        this.result.lyrics = value;
    }

    @Override
    public void setNumberOfTimesPlayed(int count) {
        this.result.numberOfTimesPlayed = count;
    }

    @Override
    public void setLastPlayedPosition(double position) {
        this.result.lastPlayedPosition = position;
    }

    @Override
    public void setDateAdded(@NonNull Date value)
    {
        this.resultDateAdded = value;
    }

    @Override
    public void setDateModified(@NonNull Date value)
    {
        this.resultDateModified = value;
    }

    @Override
    public void setDateFirstPlayed(@NonNull Date value)
    {
        this.resultDateFirstPlayed = value;
    }

    @Override
    public void setDateLastPlayed(@NonNull Date value)
    {
        this.resultDateLastPlayed = value;
    }
}

