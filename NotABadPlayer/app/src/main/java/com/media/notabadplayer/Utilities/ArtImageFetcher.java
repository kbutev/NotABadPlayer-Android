package com.media.notabadplayer.Utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioArtCover;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.atomic.AtomicBoolean;

public class ArtImageFetcher {
    public class AsyncToken {
        private AtomicBoolean valid = new AtomicBoolean(true);

        public boolean isValid() {
            return valid.get();
        }

        public void invalidate() {
            valid.set(false);
        }
    }

    final @NonNull Context context;
    @Nullable Size thumbSize;

    public ArtImageFetcher(@NonNull Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= 29) {
            this.thumbSize = new Size(512, 512);
        }
    }

    // Fetches an art cover synchronously.
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

    // Fetches an art cover asynchronously. Callback is always performed on main thread.
    // Returns a thread safe token which can be used to cancel the operation.
    @Nullable
    public AsyncToken fetchAsync(@NonNull final AudioArtCover artCover, final Function<Bitmap, Void> callback) {
        final ArtImageFetcher self = this;
        final LooperService looper = LooperService.getShared();
        final AsyncToken token = new AsyncToken();

        looper.runOnBackground(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {
                looper.runOnMain(new Function<Void, Void>() {
                    @NullableDecl
                    @Override
                    public Void apply(@NullableDecl Void input) {
                        if (token.isValid()) {
                            callback.apply(self.fetch(artCover));
                        }

                        return null;
                    }
                });
                return null;
            }
        });

        return token;
    }
}
