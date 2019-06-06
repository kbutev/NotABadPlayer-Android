package com.media.notabadplayer.Audio;

import java.util.ArrayList;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.Model.AudioTrack;

public interface AudioPlayerHistory
{
    @NonNull ArrayList<AudioTrack> getPlayHistory();
    void setList(@NonNull ArrayList<AudioTrack> playHistory);
    void playPreviousInHistory(@NonNull AudioInfo audioInfo);
}
