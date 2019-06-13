package com.media.notabadplayer.Audio;

import java.util.List;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.AudioTrack;

public interface AudioPlayerHistory
{
    @NonNull List<AudioTrack> getPlayHistory();
    void setList(@NonNull List<AudioTrack> playHistory);
    void playPreviousInHistory(@NonNull AudioInfo audioInfo) throws Exception;
}
