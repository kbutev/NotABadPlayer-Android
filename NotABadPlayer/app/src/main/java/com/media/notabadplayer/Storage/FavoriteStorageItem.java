package com.media.notabadplayer.Storage;

import java.io.Serializable;
import java.util.Date;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import android.support.annotation.NonNull;

public class FavoriteStorageItem implements Serializable {
    public final @NonNull String identifier;
    public final @NonNull Date dateFavorited;
    public final @NonNull String trackPath;
    
    FavoriteStorageItem(@NonNull BaseAudioTrack track) {
        this(track, new Date());
    }
    
    FavoriteStorageItem(@NonNull BaseAudioTrack track, @NonNull Date dateFavorited) {
        this.identifier = track.getFilePath();
        this.dateFavorited = dateFavorited;
        this.trackPath = track.getFilePath();
    }
    
    @Override
    public boolean equals(@NonNull Object other) {
        if (other instanceof FavoriteStorageItem) {
            return ((FavoriteStorageItem)other).identifier.equals(identifier);
        }
        
        return false;
    }
}
