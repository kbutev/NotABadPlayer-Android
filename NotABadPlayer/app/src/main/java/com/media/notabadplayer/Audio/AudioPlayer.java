package com.media.notabadplayer.Audio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.MutableAudioPlaylist;

public interface AudioPlayer
{
    // It takes time for the player to start. If this returns false, the player may have some
    // uninitialized properties.
    boolean isInitialized();

    boolean isPlaying();
    boolean isCompletelyStopped();
    @Nullable BaseAudioPlaylist getPlaylist();
    boolean hasPlaylist();
    AudioPlayOrder getPlayOrder();
    void setPlayOrder(AudioPlayOrder order);
    void playPlaylist(@NonNull BaseAudioPlaylist playlist) throws Exception;
    void playPlaylistAndPauseImmediately(@NonNull BaseAudioPlaylist playlist) throws Exception;
    void resume();
    void pause();
    void stop();
    void pauseOrResume();
    void playNext() throws Exception;
    void playPrevious() throws Exception;
    void playNextBasedOnPlayOrder() throws Exception;
    void shuffle() throws Exception;
    void jumpBackwards(int msec);
    void jumpForwards(int msec);
    int getDurationMSec();
    int getCurrentPositionMSec();
    void seekTo(int msec);
    int getVolume();
    void setVolume(int volume);
    void volumeUp();
    void volumeDown();
    boolean isMuted();
    void muteOrUnmute();
    void mute();
    void unmute();

    @NonNull AudioPlayerObservers observers();
    @NonNull AudioPlayerHistory playHistory();
}
