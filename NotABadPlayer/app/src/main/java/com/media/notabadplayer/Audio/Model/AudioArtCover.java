package com.media.notabadplayer.Audio.Model;

import android.net.Uri;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class AudioArtCover implements Serializable {
    // For older Android.
    public final @Nullable String key;

    // For newer Android (API >= 29).
    public final @Nullable String path;

    public AudioArtCover() {
        this.key = "";
        this.path = null;
    }

    public AudioArtCover(@Nullable String key, @Nullable Uri uri) {
        this.key = key;
        this.path = uri != null ? uri.toString() : null;
    }

    public boolean isValid() {
        if (path != null) {
            return true;
        }

        return key != null && !key.isEmpty();
    }

    public @Nullable Uri buildImageUri() {
        if (key != null && !key.isEmpty()) {
            return Uri.parse(Uri.decode(key));
        }

        return Uri.parse(path);
    }
}
