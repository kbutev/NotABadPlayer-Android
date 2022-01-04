package com.media.notabadplayer.Utilities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioArtCover;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class InternalAdapterView {
    @NonNull public final View view;
    @Nullable public AudioArtCover currentArtCover;
    @Nullable public ArtImageFetcher.AsyncToken token;

    public InternalAdapterView(@NonNull View view) {
        this.view = view;
    }

    public void resetToken() {
        if (token != null) {
            token.invalidate();
            token = null;
        }
    }

    public void fetchArtCoverAsync(final ImageView cover,
                                   @NonNull ArtImageFetcher artImageFetcher,
                                   AudioArtCover artCover,
                                   final @NonNull Drawable coverArtNone)
    {
        resetToken();

        if (!isArtCoverEqual(artCover)) {
            currentArtCover = artCover;

            cover.setImageDrawable(coverArtNone);

            if (!artCover.isValid()) {
                return;
            }

            token = artImageFetcher.fetchAsync(artCover, new Function<Bitmap, Void>() {
                @NullableDecl
                @Override
                public Void apply(@NullableDecl Bitmap imageBitmap) {
                    if (imageBitmap != null) {
                        cover.setImageBitmap(imageBitmap);
                    } else {
                        cover.setImageDrawable(coverArtNone);
                    }
                    return null;
                }
            });
        }
    }

    public boolean isArtCoverEqual(AudioArtCover artCover)
    {
        if (artCover == null)
        {
            return currentArtCover == null;
        }

        if (currentArtCover == null)
        {
            return false;
        }

        return artCover.value().equals(currentArtCover.value());
    }
}
