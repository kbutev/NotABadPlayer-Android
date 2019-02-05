package com.media.notabadplayer.Audio;

import android.support.annotation.NonNull;

public class MediaAlbum {
    public final String albumID;
    public final String albumArtist;
    public final String albumTitle;
    public final String albumCover;
    
    public MediaAlbum(@NonNull String albumID, @NonNull String albumArtist, @NonNull String albumTitle, @NonNull String albumCover)
    {
        this.albumID = albumID;
        this.albumArtist = albumArtist;
        this.albumTitle = albumTitle;
        this.albumCover = albumCover;
    }
}
