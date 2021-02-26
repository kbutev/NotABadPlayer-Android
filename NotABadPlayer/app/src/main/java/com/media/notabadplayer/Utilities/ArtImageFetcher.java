package com.media.notabadplayer.Utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.AudioArtCover;

public class ArtImageFetcher {
    final @NonNull Context context;
    @NonNull Size thumbSize;

    public ArtImageFetcher(@NonNull Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= 29) {
            this.thumbSize = new Size(512, 512);
        } else {
            thumbSize = null;
        }
    }

    @Nullable
    public Bitmap fetch(@NonNull AudioArtCover artCover) {
        Uri uri = artCover.buildImageUri();

        if (uri == null) {
            return null;
        }

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentResolver contentResolver = context.getContentResolver();
                return contentResolver.loadThumbnail(uri, thumbSize, null);
            }

            ContentResolver contentResolver = context.getContentResolver();
            return MediaStore.Images.Media.getBitmap(contentResolver, uri);
        } catch (Exception e) {
            return null;
        }
    }
}
