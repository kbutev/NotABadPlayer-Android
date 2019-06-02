package com.media.notabadplayer.Constants;

public enum AppState {
    SUSPENDED, LAUNCHING, STARTING, RUNNING;

    public boolean isSuspended()
    {
        return this == AppState.SUSPENDED;
    }
    
    public boolean isLaunching()
    {
        return this == AppState.LAUNCHING;
    }

    public boolean finishedLaunching()
    {
        return this != AppState.LAUNCHING;
    }

    public boolean isStarting()
    {
        return this == AppState.STARTING;
    }

    public boolean finishedStarting()
    {
        return this != AppState.STARTING;
    }
    
    public boolean isRunning()
    {
        return this == AppState.RUNNING;
    }
}
