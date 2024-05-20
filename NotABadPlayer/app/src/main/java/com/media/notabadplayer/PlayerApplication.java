package com.media.notabadplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.google.common.base.Function;
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

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] permissions = {
            Manifest.permission.READ_MEDIA_AUDIO
    };

    public static String[] old_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static String[] all_app_permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = permissions;
        } else {
            p = old_permissions;
        }
        return p;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        
        Log.v(PlayerApplication.class.getCanonicalName(), "Initialized. About to start launching...");

        // Set shared instance value
        application = this;
        
        // Set app lifecycle, in order to monitor the activities lifecycles
        if (_lifecycleCallbacks != null)
        {
            unregisterActivityLifecycleCallbacks(_lifecycleCallbacks);
        }

        _lifecycleCallbacks = LIFECYCLE;

        registerActivityLifecycleCallbacks(_lifecycleCallbacks);
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

    private void broadcastActivityStart()
    {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.broadcast_activity_start));
        sendBroadcast(intent);
    }

    private void broadcastActivityPause()
    {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.broadcast_activity_pause));
        sendBroadcast(intent);
    }
    
    private void terminate()
    {
        Log.v(PlayerApplication.class.getCanonicalName(), "Terminating application...");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
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
        public final int PERMISSION_REQUEST_USE_MEDIA_AUDIO = 1;

        PermissionsStatus evaluatePermissions(@NonNull final Activity activity)
        {
            // Request for permission, handle it with the activity method onRequestPermissionsResult()
            try {
                for (String permission : all_app_permissions()) {
                    int result = ContextCompat.checkSelfPermission(activity, permission);
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        return PermissionsStatus.DENIED;
                    }
                }

                return PermissionsStatus.GRANTED;
            }
            catch (Exception e)
            {
                
            }
            
            return PermissionsStatus.PERMANENTLY_DENIED;
        }
        
        private void requestPermissions(@NonNull final Activity activity)
        {
            ActivityCompat.requestPermissions(activity, all_app_permissions(), 1);
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

        public boolean isRunning()
        {
            return primaryState.isRunning();
        }
        
        private void launch(@NonNull final Activity activity)
        {
            if (!primaryState.isInactive())
            {
                throw new IllegalStateException("Invalid app state, cannot start launching, state is " + primaryState.name());
            }

            Log.v(PlayerApplication.class.getCanonicalName(), "Launching...");

            primaryState = AppState.LAUNCHING;

            // General storage initialize
            GeneralStorage.getShared();
            
            // Check if the app has the required permissions
            // Request in the background (performance optimization)
            Log.v(PlayerApplication.class.getCanonicalName(), "Checking if app has required permissions to start properly...");
            
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
            Log.v(PlayerApplication.class.getCanonicalName(), "All required permissions have been granted!");

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
            if (!primaryState.isLaunching())
            {
                throw new IllegalStateException("Invalid app state, cannot start starting, state is " + primaryState.name());
            }
            
            primaryState = AppState.STARTING;
            
            Log.v(PlayerApplication.class.getCanonicalName(), "Successfully launched! Starting...");

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
            
            Log.v(PlayerApplication.class.getCanonicalName(), "Successfully started! Running...");

            primaryState = AppState.RUNNING;
            
            broadcastRunningState();
        }
        
        private void end()
        {
            terminate();
        }
    }

    private ActivityLifecycleCallbacks LIFECYCLE = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
        @Override
        public void onActivityStarted(Activity activity)
        {
            broadcastActivityStart();
        }
        @Override
        public void onActivityResumed(Activity activity) 
        {
            // Called in three cases:
            // 1. When the app is inactive and the main activity is starting for the first time
            // 2. When the app is launching and the main activity dialog is dismissed
            // 3. App is running, just broadcast running state
            
            if (_state.isInactive())
            {
                // Inactive -> Launch
                _state.launch(activity);

                return;
            }
            
            if (_state.isLaunching())
            {
                // When the dialog box for permissions is dismissed, the main activity resumes again
                // Launch -> Start
                _state.onRequestPermissionsResult(activity);
                
                return;
            }
            
            if (_state.isRunning())
            {
                broadcastRunningState();
                
                return;
            }
        }
        @Override
        public void onActivityPaused(Activity activity)
        {
            broadcastActivityPause();
        }
        @Override
        public void onActivityStopped(Activity activity) {}
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        @Override
        public void onActivityDestroyed(Activity activity) {}
    };
}
