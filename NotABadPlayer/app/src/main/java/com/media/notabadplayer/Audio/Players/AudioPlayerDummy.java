package com.media.notabadplayer.Audio.Players;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerHistory;
import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioPlayerObservers;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.MutableAudioPlaylist;

public class AudioPlayerDummy implements AudioPlayer {
    private final AudioPlayerObservers observers;
    private final AudioPlayerHistory playHistory = new AudioPlayerDummy.PlayHistory();

    AudioPlayerDummy(@NonNull AudioPlayerObservers observers)
    {
        this.observers = observers;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isCompletelyStopped() {
        return false;
    }

    @Override
    public @Nullable BaseAudioPlaylist getPlaylist() {
        return null;
    }
    
    @Override
    public boolean hasPlaylist() {
        return false;
    }

    @Override
    public AudioPlayOrder getPlayOrder() {
        return AudioPlayOrder.FORWARDS;
    }

    @Override
    public void setPlayOrder(AudioPlayOrder order) {

    }

    @Override
    public void playPlaylist(@NonNull BaseAudioPlaylist playlist) throws Exception {

    }

    @Override
    public void playPlaylistAndPauseImmediately(@NonNull BaseAudioPlaylist playlist) throws Exception {
        
    }
    
    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void pauseOrResume() {

    }

    @Override
    public void playNext() {

    }

    @Override
    public void playPrevious() {

    }

    @Override
    public void playNextBasedOnPlayOrder() {

    }

    @Override
    public void shuffle() {

    }

    @Override
    public void jumpBackwards(int msec) {

    }

    @Override
    public void jumpForwards(int msec) {

    }

    @Override
    public int getDurationMSec() {
        return 0;
    }

    @Override
    public int getCurrentPositionMSec() {
        return 0;
    }

    @Override
    public void seekTo(int msec) {

    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public void setVolume(int volume) {

    }

    @Override
    public void volumeUp() {

    }

    @Override
    public void volumeDown() {

    }

    @Override
    public boolean isMuted() {
        return false;
    }

    @Override
    public void muteOrUnmute() {

    }

    @Override
    public void mute() {

    }

    @Override
    public void unmute() {

    }

    @Override
    public @NonNull
    AudioPlayerObservers observers() {
        return observers;
    }

    @Override
    public @NonNull
    AudioPlayerHistory playHistory() {
        return playHistory;
    }

    public class Observers implements AudioPlayerObservers
    {
        public void attach(AudioPlayerObserver observer)
        {
            
        }

        public void detach(AudioPlayerObserver observer)
        {
            
        }
    }

    public class PlayHistory implements AudioPlayerHistory
    {
        public @NonNull List<BaseAudioTrack> getPlayHistory()
        {
            return new ArrayList<>();
        }

        public void setList(@NonNull List<BaseAudioTrack> playHistory)
        {
            
        }

        public void playPreviousInHistory(@NonNull AudioInfo audioInfo) throws Exception
        {
            
        }
    }
}
