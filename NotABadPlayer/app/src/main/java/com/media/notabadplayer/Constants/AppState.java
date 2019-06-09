package com.media.notabadplayer.Constants;

public enum AppState {
    INACTIVE, LAUNCHING, STARTING, RUNNING;

    public boolean isInactive()
    {
        return this == AppState.INACTIVE;
    }
    
    public boolean isLaunching()
    {
        return this == AppState.LAUNCHING;
    }
    
    public boolean isStarting()
    {
        return this == AppState.STARTING;
    }
    
    public boolean isRunning()
    {
        return this == AppState.RUNNING;
    }
}
