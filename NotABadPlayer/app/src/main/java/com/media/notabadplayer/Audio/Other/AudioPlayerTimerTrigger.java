package com.media.notabadplayer.Audio.Other;

// Describes any component that controls the player based on time.
// These timers, may start or pause the player, after some time passes.
public interface AudioPlayerTimerTrigger {
    // Alert the timer that the user interacted with the app.
    // This will restart the timer.
    void onUserInteraction();
}
