package com.media.notabadplayer.Audio;

import androidx.annotation.NonNull;

public interface AudioPlayerObservers
{
    void attach(@NonNull AudioPlayerObserver observer);
    void detach(@NonNull AudioPlayerObserver observer);
}
