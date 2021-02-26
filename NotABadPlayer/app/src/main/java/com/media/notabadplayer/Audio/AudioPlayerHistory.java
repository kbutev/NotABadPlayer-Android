package com.media.notabadplayer.Audio;

import java.util.List;
import androidx.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;

public interface AudioPlayerHistory
{
    @NonNull List<BaseAudioTrack> getPlayHistory();
    void setList(@NonNull List<BaseAudioTrack> playHistory);
    void playPreviousInHistory(@NonNull AudioInfo audioInfo) throws Exception;
}
