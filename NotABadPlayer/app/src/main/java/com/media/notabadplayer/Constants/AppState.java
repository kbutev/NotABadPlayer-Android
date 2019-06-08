package com.media.notabadplayer.Constants;

public enum AppState {
    INACTIVE, LAUNCHING, REQUESTING_PERMISSIONS, STARTING, RUNNING;

    public boolean isInactive()
    {
        return this == AppState.INACTIVE;
    }
    
    public boolean isLaunching()
    {
        return this == AppState.LAUNCHING;
    }

    public boolean isRequestingPermissions()
    {
        return this == AppState.REQUESTING_PERMISSIONS;
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
