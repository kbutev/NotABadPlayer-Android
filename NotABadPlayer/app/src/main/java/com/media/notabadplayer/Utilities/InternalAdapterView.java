package com.media.notabadplayer.Utilities;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InternalAdapterView {
    @NonNull public final View view;
    @Nullable public ArtImageFetcher.AsyncToken token;

    public InternalAdapterView(@NonNull View view) {
        this.view = view;
    }

    public void reset() {
        if (token != null) {
            token.invalidate();
            token = null;
        }
    }
}
