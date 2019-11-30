package com.media.notabadplayer.Audio.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

public interface BaseAudioTrackDate {
    @NonNull Date getAdded();
    @NonNull Date getModified();
    @Nullable Date getFirstPlayed();
    @Nullable Date getLastPlayed();
}
