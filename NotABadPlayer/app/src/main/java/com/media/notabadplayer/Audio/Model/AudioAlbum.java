package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;

public class AudioAlbum {
    public final String albumID;
    public final String albumArtist;
    public final String albumTitle;
    public final String albumCover;
    
    public AudioAlbum(@NonNull String albumID, @NonNull String albumArtist, @NonNull String albumTitle, @NonNull String albumCover)
    {
        this.albumID = albumID;
        this.albumArtist = albumArtist;
        this.albumTitle = albumTitle;
        this.albumCover = albumCover;
    }
}
