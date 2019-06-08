package com.media.notabadplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.google.common.base.Function;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.Utilities.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.LooperService;

public class PlayerApplication extends Application {
    private static PlayerApplication application;
    
    private PlayerApplication.State _state = new PlayerApplication.State();
    private PlayerApplication.Permissions _permissions = new PlayerApplication.Permissions();

    private ActivityLifecycleCallbacks _lifecycleCallbacks = null;

    private AudioPlayerNoiseSuppression _noiseSuppression;
    
    public static @NonNull PlayerApplication getShared()
    {
        return application;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        
        Log.v(PlayerApplication.class.getCanonicalName(), "Initialized!");

        // Set shared instance value
        application = this;
        
        // Wait for the first activity to start, then continue with start with the app launch
        setLifecycle(LIFECYCLE_LAUNCHING);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    }
    
    private void broadcastRunningState()
    {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.broadcast_application_is_running));
        sendBroadcast(intent);
    }
    
    private void setLifecycle(@NonNull ActivityLifecycleCallbacks callback)
    {
        if (_lifecycleCallbacks != null)
        {
            unregisterActivityLifecycleCallbacks(_lifecycleCallbacks);
        }
        
        _lifecycleCallbacks = callback;
        
        registerActivityLifecycleCallbacks(_lifecycleCallbacks);
    }
    
    private void terminate()
    {
        Log.v(PlayerApplication.class.getCanonicalName(), "Terminating application...");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
    
    private void restart()
    {
        Log.v(PlayerApplication.class.getCanonicalName(), "Restarting application...");
        
        terminate();
    }
    
    private void performLaunchPerformanceOptimizations()
    {
        // Anything we can start early on here, so the user can get smoother experience
        // Optimization for the Settings screen
        GeneralStorage.getShared().retrieveAllSettingsActionValues();
    }
    
    enum PermissionsStatus {
        GRANTED, DENIED, PERMANENTLY_DENIED
    }
    
    private class Permissions {
        public final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;

        PermissionsStatus evaluatePermissions(@NonNull final Activity activity)
        {
            // Request for permission, handle it with the activity method onRequestPermissionsResult()
            try {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    return PermissionsStatus.DENIED;
                }
                else
                {
                    return PermissionsStatus.GRANTED;
                }
            }
            catch (Exception e)
            {
                
            }
            
            return PermissionsStatus.PERMANENTLY_DENIED;
        }
        
        private void requestPermissions(@NonNull final Activity activity)
        {
            if (Build.VERSION.SDK_INT >= 16)
            {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            }
            else
            {
                ActivityCompat.requestPermissions(activity, new String[]{}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private class State {
        AppState primaryState = AppState.INACTIVE;

        public boolean isInactive()
        {
            return primaryState.isInactive();
        }

        public boolean isLaunching()
        {
            return primaryState.isLaunching();
        }

        public boolean isRequestingPermissions()
        {
            return primaryState.isRequestingPermissions();
        }
        
        public void update()
        {
            
        }
        
        private void launch()
        {
            if (!primaryState.isInactive())
            {
                throw new IllegalStateException("Invalid app state, cannot start launching, state is " + primaryState.name());
            }

            Log.v(PlayerApplication.class.getCanonicalName(), "Launching...");

            primaryState = AppState.LAUNCHING;

            // General storage initialize
            GeneralStorage.getShared();
        }
        
        private void startRequestPermissions(@NonNull final Activity activity)
        {
            if (!primaryState.isLaunching())
            {
                throw new IllegalStateException("Invalid app state, cannot start requesting permissions, state is " + primaryState.name());
            }

            Log.v(PlayerApplication.class.getCanonicalName(), "Checking if app has required permissions to start properly...");

            primaryState = AppState.REQUESTING_PERMISSIONS;
            
            // Request in the background (performance optimization)
            LooperService.getShared().runOnBackground(new Function<Void, Void>() {
                @NullableDecl
                @Override
                public Void apply(@NullableDecl Void input) {
                    final PermissionsStatus state = _permissions.evaluatePermissions(activity);
                    
                    // Evaluate on main thread
                    LooperService.getShared().runOnBackground(new Function<Void, Void>() {
                        @NullableDecl
                        @Override
                        public Void apply(@NullableDecl Void input) {
                            if (state == PermissionsStatus.GRANTED)
                            {
                                onRequestPermissionsGranted();
                            }
                            
                            if (state == PermissionsStatus.DENIED)
                            {
                                _permissions.requestPermissions(activity);
                            }

                            if (state == PermissionsStatus.PERMANENTLY_DENIED)
                            {
                                onRequestPermissionsDenied(activity);
                            }
                            
                            return null;
                        }
                    });
                    
                    return null;
                }
            });
        }
        
        private void onRequestPermissionsResult(@NonNull final Activity activity)
        {
            final PermissionsStatus state = _permissions.evaluatePermissions(activity);
            
            if (state == PermissionsStatus.GRANTED)
            {
                onRequestPermissionsGranted();
            }
            else
            {
                onRequestPermissionsDenied(activity);
            }
        }

        private void onRequestPermissionsGranted()
        {
            Log.v(PlayerApplication.class.getCanonicalName(), "All required permissions have been granted! Proceed with application launch process...");
            
            setLifecycle(LIFECYCLE_STARTED);

            _state.start();
        }
        
        private void onRequestPermissionsDenied(@NonNull final Activity activity)
        {
            Log.v(PlayerApplication.class.getCanonicalName(), "Application cannot launch, required permissions have been denied.");

            DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    _state.end();
                }
            };

            AlertWindows.showAlert(activity, 0, R.string.error_need_storage_permission, R.string.ok, action);
        }

        private void start()
        {
            if (!primaryState.isLaunching() && !primaryState.isRequestingPermissions())
            {
                throw new IllegalStateException("Invalid app state, cannot start starting, state is " + primaryState.name());
            }
            
            primaryState = AppState.STARTING;

            Log.v(PlayerApplication.class.getCanonicalName(), "Finished launching!");
            Log.v(PlayerApplication.class.getCanonicalName(), "Starting...");

            LooperService.getShared().runOnBackground(new Function<Void, Void>() {
                @NullableDecl
                @Override
                public Void apply(@NullableDecl Void input) {
                    // Audio library initialize
                    final AudioLibrary library = AudioLibrary.getShared();

                    // Noise suppression
                    _noiseSuppression = new AudioPlayerNoiseSuppression();
                    _noiseSuppression.start(PlayerApplication.this);

                    // Optimizations
                    performLaunchPerformanceOptimizations();
                    
                    // Run
                    LooperService.getShared().runOnMain(new Function<Void, Void>() {
                        @NullableDecl
                        @Override
                        public Void apply(@NullableDecl Void input) {
                            // Audio player start
                            Player.getShared().start(library);
                            
                            // Run
                            _state.run();
                            
                            return null;
                        }
                    });
                    
                    return null;
                }
            });
        }

        private void run()
        {
            if (!primaryState.isStarting())
            {
                throw new IllegalStateException("Invalid app state, cannot start running, state is " + primaryState.name());
            }

            Log.v(PlayerApplication.class.getCanonicalName(), "Finished starting!");
            Log.v(PlayerApplication.class.getCanonicalName(), "Running...");

            primaryState = AppState.RUNNING;
            
            broadcastRunningState();
        }
        
        private void end()
        {
            terminate();
        }
    }

    private ActivityLifecycleCallbacks LIFECYCLE_LAUNCHING = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // Called once:
            // When the first activity is created
            
            if (_state.isInactive())
            {
                // Idle -> Launch
                _state.launch();

                return;
            }

            throw new UncheckedExecutionException(new Exception("Internal application state error."));
        }
        @Override
        public void onActivityStarted(Activity activity) {}
        @Override
        public void onActivityResumed(Activity activity) 
        {
            // Called twice:
            // 1. When the main activity is starting for the first time
            // 2. When the main activity dialog for request permissions is dismissed
            
            if (_state.isLaunching())
            {
                // The main activity has resumed
                // Continue with the app launch process
                // Start requesting permissions
                // Launch -> Requesting Permissions
                _state.startRequestPermissions(activity);
                
                return;
            }
            
            if (_state.isRequestingPermissions())
            {
                // When the request for permissions are allowed/denied, the activity dialog will hide
                // Which will cause the main activity to show up
                // Requesting Permissions -> Start
                _state.onRequestPermissionsResult(activity);
                
                return;
            }

            throw new UncheckedExecutionException(new Exception("Internal application state error."));
        }
        @Override
        public void onActivityPaused(Activity activity) {}
        @Override
        public void onActivityStopped(Activity activity) {}
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        @Override
        public void onActivityDestroyed(Activity activity)
        {
            
        }
    };

    private ActivityLifecycleCallbacks LIFECYCLE_STARTED = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
        @Override
        public void onActivityStarted(Activity activity) {}
        @Override
        public void onActivityResumed(Activity activity) 
        {
            broadcastRunningState();
        }
        @Override
        public void onActivityPaused(Activity activity) {}
        @Override
        public void onActivityStopped(Activity activity) {}
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        @Override
        public void onActivityDestroyed(Activity activity) {}
    };
}