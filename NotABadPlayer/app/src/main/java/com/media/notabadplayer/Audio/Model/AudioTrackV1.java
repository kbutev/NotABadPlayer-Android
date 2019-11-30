package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.media.notabadplayer.Utilities.StringUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class AudioTrackV1 implements BaseAudioTrack, Serializable {
    public @NonNull String filePath;

    public @NonNull String title;
    public @NonNull String artist;
    public @NonNull String albumTitle;
    public @NonNull String albumID;
    public @NonNull String artCover;
    public int trackNum;

    public double durationInSeconds;
    public @NonNull String duration;

    public @NonNull AudioTrackSource source;

    public @NonNull String lyrics;

    public int numberOfTimesPlayed;
    public double totalTimesPlayed;

    public @NonNull BaseAudioTrackDate date;

    public AudioTrackV1()
    {
        filePath = "";
        artist = "";
        albumTitle = "";
        albumID = "";
        artCover = "";

        duration = StringUtilities.secondsToString(0);

        source = AudioTrackSource.createAlbumSource("");

        lyrics = "";

        date = buildDefaultAudioTrackDate();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof AudioTrackV1)
        {
            AudioTrackV1 other = (AudioTrackV1)o;
            
            return filePath.equals(other.filePath);
        }
        
        return false;
    }

    // # Serialization

    private void writeObject(@NonNull ObjectOutputStream out) throws IOException 
    {
        out.defaultWriteObject();
    }
    
    private void readObject(@NonNull ObjectInputStream in) throws IOException,ClassNotFoundException 
    {
        in.defaultReadObject();
    }

    // # BaseAudioTrack

    @Override
    public @NonNull String getFilePath() {
        return filePath;
    }

    @Override
    public @NonNull String getTitle() {
        return title;
    }

    @Override
    public @NonNull String getArtist() {
        return artist;
    }

    @Override
    public @NonNull String getAlbumTitle() {
        return albumTitle;
    }

    @Override
    public @NonNull String getAlbumID() {
        return albumID;
    }

    @Override
    public @NonNull String getArtCover() {
        return artCover;
    }

    @Override
    public int getTrackNum() {
        return trackNum;
    }

    @Override
    public double getDurationInSeconds() {
        return durationInSeconds;
    }

    @Override
    public @NonNull String getDuration() {
        return duration;
    }

    @Override
    public @NonNull AudioTrackSource getSource() {
        return source;
    }

    @Override
    public @NonNull String getLyrics() {
        return lyrics;
    }

    @Override
    public int getNumberOfTimesPlayed() {
        return numberOfTimesPlayed;
    }

    @Override
    public double getTotalTimePlayed() {
        return totalTimesPlayed;
    }

    @Override
    public @NonNull BaseAudioTrackDate getDate() {
        return date;
    }

    // # Utilities

    private BaseAudioTrackDate buildDefaultAudioTrackDate()
    {
        final Date date = new Date();

        return new AudioTrackV1Date(date, date, null, null);
    }
}

class AudioTrackV1Date implements BaseAudioTrackDate, Serializable {
    private final @NonNull Date added;
    private final @NonNull Date modified;
    private final @Nullable Date firstPlayed;
    private final @Nullable Date lastPlayed;

    AudioTrackV1Date(@NonNull Date added, @NonNull Date modified, @Nullable Date firstPlayed, @Nullable Date lastPlayed)
    {
        this.added = added;
        this.modified = modified;
        this.firstPlayed = firstPlayed;
        this.lastPlayed = lastPlayed;
    }

    @Override
    public @NonNull Date getAdded()
    {
        return added;
    }

    @Override
    public @NonNull Date getModified()
    {
        return modified;
    }

    @Override
    public @Nullable Date getFirstPlayed()
    {
        return firstPlayed;
    }

    @Override
    public @Nullable Date getLastPlayed()
    {
        return lastPlayed;
    }
}
