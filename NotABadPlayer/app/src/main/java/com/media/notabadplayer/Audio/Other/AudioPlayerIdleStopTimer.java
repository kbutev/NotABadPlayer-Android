package com.media.notabadplayer.Audio.Other;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.LooperClient;
import com.media.notabadplayer.Utilities.LooperService;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPlayerIdleStopTimer implements AudioPlayerTimerTrigger, LooperClient {
    private static int LOOPER_INTERVAL_MS = 10000;
    private static LooperService singleton;

    private final Object lock = new Object();

    private @NonNull Date _lastUserInteraction = new Date();
    private @NonNull AtomicBoolean _isTimedOut = new AtomicBoolean(false);

    public @Nullable
    AudioPlayerIdleStopDelegate delegate;

    public AudioPlayerIdleStopTimer()
    {

    }

    synchronized public static LooperService getLooperService()
    {
        if (singleton == null)
        {
            singleton = new LooperService(LOOPER_INTERVAL_MS);
        }

        return singleton;
    }

    public void start()
    {
        getLooperService().subscribe(this);
    }

    public void stop()
    {
        getLooperService().unsubscribe(this);
    }

    static long idleTimeoutInMS()
    {
        return GeneralStorage.getShared().getAudioIdleStopTimer().asTimeValue().inMilliseconds();
    }

    public long timeLeftToIdleTimeout()
    {
        Date now = new Date();

        synchronized (lock)
        {
            return now.getTime() - _lastUserInteraction.getTime();
        }
    }

    public boolean isActive()
    {
        return GeneralStorage.getShared().getAudioIdleStopTimer() != AudioPlayerTimerValue.NONE;
    }

    public boolean isIdle()
    {
        long timeout = idleTimeoutInMS();
        long timeLeft = timeLeftToIdleTimeout();
        return timeLeft > timeout;
    }

    // Checks and handles idle app state.
    public void update()
    {
        if (!isActive()) {
            return;
        }

        if (!isIdle()) {
            _isTimedOut.set(false);
            return;
        }

        boolean isAlreadyTimedOut = _isTimedOut.getAndSet(true);

        if (isAlreadyTimedOut) {
            return;
        }

        onHandleIdleApp();
    }

    // Handle idle app by pausing the player.
    public void onHandleIdleApp()
    {
        AudioPlayerIdleStopDelegate d = delegate;

        if (d != null) {
            d.handleIdle();
        }
    }

    // # AudioPlayerTimer

    @Override
    public void onUserInteraction()
    {
        synchronized (lock)
        {
            _lastUserInteraction = new Date();
        }
    }

    // # LooperClient

    @Override
    public void loop()
    {
        update();
    }
}
