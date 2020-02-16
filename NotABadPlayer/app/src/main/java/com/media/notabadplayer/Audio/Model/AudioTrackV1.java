package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.media.notabadplayer.Utilities.StringUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AudioTrackV1 implements BaseAudioTrack, Serializable {
    public @NonNull String filePath;

    public @NonNull String title;
    public @NonNull String artist;
    public @NonNull String albumTitle;
    public @NonNull String albumID;
    public @NonNull String artCover;
    public int trackNum;

    public double durationInSeconds;

    private @NonNull AudioTrackSource source;
    private @Nullable AudioTrackSource originalSource;

    public @NonNull String lyrics;
    public int numberOfTimesPlayed;
    public @NonNull AudioTrackDate date;
    public double lastPlayedPosition;

    public AudioTrackV1()
    {
        filePath = "";
        artist = "";
        albumTitle = "";
        albumID = "";
        artCover = "";

        source = AudioTrackSource.createAlbumSource("");
        originalSource = null;
        
        lyrics = "";
        date = AudioTrackDateBuilder.buildDefault();
        lastPlayedPosition = 0;
    }

    public AudioTrackV1(final BaseAudioTrack prototype)
    {
        filePath = prototype.getFilePath();
        title = prototype.getTitle();
        artist = prototype.getArtist();
        albumTitle = prototype.getAlbumTitle();
        albumID = prototype.getAlbumID();
        artCover = prototype.getArtCover();
        trackNum = prototype.getTrackNum();

        durationInSeconds = prototype.getDurationInSeconds();

        source = prototype.getSource();
        originalSource = prototype.getOriginalSource();

        lyrics = prototype.getLyrics();
        numberOfTimesPlayed = prototype.getNumberOfTimesPlayed();
        date = prototype.getDate();
        lastPlayedPosition = prototype.getLastPlayedPosition();
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
    
    // # Setters
    
    public void setSource(@NonNull AudioTrackSource source)
    {
        this.source = source;
        
        if (this.originalSource == null)
        {
            this.originalSource = this.source;
        }
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
        return StringUtilities.secondsToString(durationInSeconds);
    }

    @Override
    public @NonNull AudioTrackSource getSource() {
        return source;
    }

    @Override
    public @NonNull AudioTrackSource getOriginalSource() {
        return originalSource;
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
    public double getLastPlayedPosition() {
        return lastPlayedPosition;
    }

    @Override
    public @NonNull AudioTrackDate getDate() {
        return date;
    }
}
