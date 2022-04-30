package com.media.notabadplayer.Audio.Model;

import androidx.annotation.NonNull;

public class AudioAlbum {
    public final @NonNull String albumID;
    public final @NonNull String albumArtist;
    public final @NonNull String albumTitle;
    public final @NonNull AudioArtCover artCover;

    public AudioAlbum(@NonNull String albumID, @NonNull String albumArtist, @NonNull String albumTitle, @NonNull AudioArtCover artCover)
    {
        this.albumID = albumID;
        this.albumArtist = albumArtist;
        this.albumTitle = albumTitle;
        this.artCover = artCover;
    }

    public Long albumIDAsLong() {
        if (albumID.isEmpty()) {
            return 0L;
        }

        return Long.parseLong(albumID);
    }
}
