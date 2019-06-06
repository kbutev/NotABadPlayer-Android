package com.media.notabadplayer.View.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Presenter.ListsPresenter;
import com.media.notabadplayer.Presenter.QuickPlayerPresenter;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Audio.Utilities.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.AlbumsPresenter;
import com.media.notabadplayer.Presenter.MainPresenter;
import com.media.notabadplayer.Presenter.SearchPresenter;
import com.media.notabadplayer.Presenter.SettingsPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.Utilities.AppThemeUtility;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Player.QuickPlayerFragment;
import com.media.notabadplayer.View.Lists.ListsFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Settings.SettingsFragment;

public class MainActivity extends AppCompatActivity implements BaseView {
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private State _state = new State();
    
    private boolean _launchedFromFile;
    private Uri _launchedFromFileUri;
    
    private AudioLibrary _audioLibrary;
    private MainPresenter _presenter;

    private BottomNavigationView _navigationView;
    
    private TabNavigation _tabNavigation = new TabNavigation();
    
    private QuickPlayerFragment _quickPlayer = null;
    private BasePresenter _quickPlayerPresenter = null;
    
    private AudioPlayerNoiseSuppression _noiseSuppression;
    
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final int currentTabID = _tabNavigation.currentTabID;
            onTabItemSelected(item.getItemId());
            return currentTabID != _tabNavigation.currentTabID;
        }
    };

    private static final String RESTART_APP_KEY = "RESTART_APP";
    private static final String RESTART_APP_WAS_PLAYING_KEY = "RESTART_APP";
    private boolean _restarted = false;
    private boolean _restartedWasPlaying = false;
    
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        
        // Load data from intent
        loadDataFromIntent(intent);
        
        // Play new file?
        playTrackFromIntentRequest();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(null);
        
        if (savedInstanceState != null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Application is being restored, deny, restart instead");
            restartApp();
            return;
        }
        
        // Load data from intent
        loadDataFromIntent(getIntent());
        
        if (!_restarted)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Application is launching...");
        }
        else
        {
            Log.v(MainActivity.class.getCanonicalName(), "Application restarted! Application is now launching...");
        }
        
        // State
        _state.launch();
        
        // UI
        onCreateLaunch();
    }
    
    private void loadDataFromIntent(@NonNull Intent intent)
    {
        _launchedFromFile = Intent.ACTION_VIEW.equals(intent.getAction());

        if (_launchedFromFile)
        {
            _launchedFromFileUri = intent.getData();
        }

        _restarted = intent.getBooleanExtra(RESTART_APP_KEY, false);
        _restartedWasPlaying = intent.getBooleanExtra(RESTART_APP_WAS_PLAYING_KEY, false);
    }
    
    private void onCreateLaunch()
    {
        
    }

    private void onCreateMain()
    {
        // App theme
        AppThemeUtility.setTheme(this, GeneralStorage.getAppTheme(this));
        
        // Content
        setContentView(R.layout.activity_main);

        // Presenter, audio model
        _presenter = new MainPresenter();
        _presenter.setView(this);

        _audioLibrary = new AudioLibrary(this);
        
        // UI
        initMainUI();

        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        _noiseSuppression.start(this);
    }

    private void restartApp()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Restarting application...");

        // Save player state
        savePlayerState();

        // End the player properly
        Player.getShared().end();
        
        // Start a new instance of this
        Intent intent = new Intent(this, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(RESTART_APP_KEY, true);
        intent.putExtra(RESTART_APP_WAS_PLAYING_KEY, Player.getShared().isPlaying());
        startActivity(intent);
        overridePendingTransition(0, 0);
        
        // Must restart, in order to wipe out the memory - singletons will be wiped out too
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();

        enableInteraction();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        disableInteraction();
        
        // Every time the main activity pauses, save the player primaryState
        savePlayerState();
    }
    
    @Override
    protected void onDestroy()
    {
        if (_noiseSuppression != null)
        {
            unregisterReceiver(_noiseSuppression);
        }
        
        Log.v(MainActivity.class.getCanonicalName(), "Application is terminated.");
        
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
        {
            KeyBinds.getShared().evaluateInput(ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
        {
            KeyBinds.getShared().evaluateInput(ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed()
    {
        // Not on any of the first tabs? Go back
        if (!_tabNavigation.isCurrentTabVisible())
        {
            Log.v(MainActivity.class.getCanonicalName(), "Navigate backwards");
            super.onBackPressed();
            return;
        }

        // Currently on any of the first tabs? Send to background
        Log.v(MainActivity.class.getCanonicalName(), "Navigate to home screen");
        moveTaskToBack(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        // Handle permission accepted request
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionForReadExtertalStorageGranted();
        }
        else
        {
            permissionForReadExtertalStorageNotGranted();
        }
    }
    
    private void startServices()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Starting services...");
        
        // Use background thread to start the services - audio storage
        // Then, alert self on the main thread
        final MainActivity main = this;
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Audio Storage start
                _audioLibrary.load();

                // Audio Player start
                Player.getShared().start(getApplication(), _audioLibrary, _restartedWasPlaying);
                
                // Finish on main thread
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        main.finishedStartingServices();
                    }
                };

                mainHandler.post(myRunnable);
            }
        });
        
        thread.start();
    }
    
    private void finishedStartingServices()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Finished starting services!");
        
        _state.run();
    }
    
    private void playTrackFromIntentRequest()
    {
        if (_launchedFromFile)
        {
            startAppWithTrack(_launchedFromFileUri);
        }
    }
    
    private void startAppWithTrack(Uri path)
    {
        Log.v(MainActivity.class.getCanonicalName(), "Launching player with initial track...");
        
        AudioTrack track = _audioLibrary.findTrackByPath(path);

        if (track == null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with desired track: " + path.toString());
            return;
        }

        AudioPlaylist playlist = track.source.getSourcePlaylist(_audioLibrary, track);

        if (playlist == null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with desired track: " + path.toString());
            return;
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("playlist", Serializing.serializeObject(playlist));
        startActivity(intent);
        
        overridePendingTransition(0, 0);
    }

    private void requestPermissionForReadExtertalStorage()
    {
        final MainActivity mainActivity = this;
        
        // Use background thread to check for permission
        // Then, alert update self on the main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean permissionGiven = true;
                
                // Request for permission, handle it with the activity method onRequestPermissionsResult()
                try {
                    if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(mainActivity,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                        return;
                    }
                } 
                catch (Exception e)
                {
                    permissionGiven = false;
                }
                
                final boolean hasPermission = permissionGiven;
                
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        // Permission granted:
                        // Just start the app normally (transition from launch primaryState)
                        if (hasPermission)
                        {
                            permissionForReadExtertalStorageGranted();
                        }
                        // Permission not granted:
                        // Stop app
                        else
                        {
                            permissionForReadExtertalStorageNotGranted();
                        }
                    }
                };

                mainHandler.post(myRunnable);
            }
        });
        
        thread.start();
    }

    private void permissionForReadExtertalStorageGranted()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Permission for read external storage has been granted.");
        
        if (_state.isLaunching())
        {
            _state.start();
        }
    }

    private void permissionForReadExtertalStorageNotGranted()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Error: permission for read external storage has not been granted.");
        
        DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertWindows.showAlert(this, 0, R.string.error_need_storage_permission, R.string.ok, action);
    }
    
    private void initMainUI()
    {
        // Bottom navigation menu
        _navigationView = findViewById(R.id.navigation);
        
        // Select default tab
        onTabItemSelected(DEFAULT_SELECTED_TAB_ID);
        
        // Create quick player and it's presenter
        _quickPlayerPresenter = new QuickPlayerPresenter(_audioLibrary);
        _quickPlayer = QuickPlayerFragment.newInstance(_quickPlayerPresenter, this);
        _quickPlayerPresenter.setView(_quickPlayer);
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.quickPlayer, _quickPlayer).commit();
        
        // Start presenter
        _presenter.start();
        
        // Set bottom navigation menu listener
        _navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    
    private void performLaunchPerformanceOptimizations()
    {
        // Anything we can start early on here, so the user can get smoother experience
        // Optimization for the Settings screen
        GeneralStorage.getShared().retrieveAllSettingsActionValues();
    }
    
    private void selectAlbumsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select albums tab");
        
        int tabID = R.id.navigation_albums;

        _tabNavigation.setCurrentTabTo(tabID, true);
    }

    private void selectListsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select lists tab");

        int tabID = R.id.navigation_lists;

        _tabNavigation.setCurrentTabTo(tabID, true);
    }

    private void selectSearchTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select search tab");

        int tabID = R.id.navigation_search;

        _tabNavigation.setCurrentTabTo(tabID, true);
    }

    private void selectSettingsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select settings tab");

        int tabID = R.id.navigation_settings;

        _tabNavigation.setCurrentTabTo(tabID, false);
    }

    private void onTabItemSelected(int itemID)
    {
        // If already selected, then try to go to the root tab
        if (_tabNavigation.currentTabID == itemID)
        {
            // Not on any of the first tabs? Go back
            if (!_tabNavigation.isCurrentTabVisible())
            {
                Log.v(MainActivity.class.getCanonicalName(), "Navigate tab backwards");
                super.onBackPressed();
            }
            
            return;
        }

        switch (itemID) {
            case R.id.navigation_albums:
                selectAlbumsTab();
                break;
            case R.id.navigation_lists:
                selectListsTab();
                break;
            case R.id.navigation_search:
                selectSearchTab();
                break;
            case R.id.navigation_settings:
                selectSettingsTab();
                break;
        }
    }

    @Override
    public void enableInteraction()
    {

    }

    @Override
    public void disableInteraction()
    {

    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull AudioPlaylist playlist)
    {
        // When not on the settings tab, let the view handle the request
        if (_tabNavigation.currentTabID != R.id.navigation_settings)
        {
            // Make sure that the current tab exists and has been created (onCreate() was called)
            if (_tabNavigation.currentTab != null)
            {
                BaseView view = _tabNavigation.currentTab.tab;
                Fragment fragment = (Fragment) view;
                
                if (fragment.getView() != null)
                {
                    view.openPlaylistScreen(_audioLibrary, playlist);
                }
            }
        }
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist)
    {

    }

    @Override
    public void onUserPlaylistsLoad(@NonNull List<AudioPlaylist> playlists)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull AudioPlaylist playlist)
    {

    }

    @Override
    public void updatePlayerScreen(@NonNull AudioPlaylist playlist)
    {

    }

    @Override
    public void searchQueryResults(@NonNull String searchQuery, @NonNull List<AudioTrack> songs, @Nullable String searchTip)
    {

    }

    @Override
    public void appSettingsReset()
    {
        Log.v(MainActivity.class.getSimpleName(), "App settings were reset");

        _tabNavigation.clearTabCache();
    }
    
    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {
        Log.v(MainActivity.class.getSimpleName(), "App theme changed to " + appTheme.name());

        _tabNavigation.clearTabCache();

        AppThemeUtility.setTheme(this, appTheme);
        
        _quickPlayer.appThemeChanged(appTheme);
    }
    
    @Override
    public void appTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {
        Log.v(MainActivity.class.getSimpleName(), "App track sorting changed.");

        _tabNavigation.clearTabCache();
        
        _quickPlayer.appTrackSortingChanged(trackSorting);
    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {
        Log.v(MainActivity.class.getSimpleName(), "App appearance changed.");

        _tabNavigation.clearTabCache();
        
        _quickPlayer.onShowVolumeBarSettingChange(value);
    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
    
    private void savePlayerState()
    {
        if (_state.isRunning())
        {
            GeneralStorage.getShared().savePlayerState();
            GeneralStorage.getShared().savePlayerPlayHistoryState();
        }
    }
    
    public class State {
        private AppState primaryState = AppState.SUSPENDED;
        
        boolean isLaunching()
        {
            return primaryState == AppState.LAUNCHING;
        }
        
        boolean isRunning()
        {
            return primaryState == AppState.RUNNING;
        }
        
        private void launch()
        {
            if (!primaryState.isSuspended())
            {
                throw new IllegalStateException("Invalid app state, cannot start launching, state is " + primaryState.name());
            }

            Log.v(MainActivity.class.getCanonicalName(), "Launching...");
            
            primaryState = AppState.LAUNCHING;

            // General storage initialize
            GeneralStorage.getShared().initialize(getApplication());

            // Mandatory storage permission request
            requestPermissionForReadExtertalStorage();
        }
        
        private void start()
        {
            if (!primaryState.isLaunching())
            {
                throw new IllegalStateException("Invalid app state, cannot start starting, state is " + primaryState.name());
            }
            
            primaryState = AppState.STARTING;

            Log.v(MainActivity.class.getCanonicalName(), "Finished launching!");
            Log.v(MainActivity.class.getCanonicalName(), "Starting...");
            
            // Create main interface
            onCreateMain();

            // Start services here
            startServices();
        }

        private void run()
        {
            if (!primaryState.isStarting())
            {
                throw new IllegalStateException("Invalid app state, cannot start running, state is " + primaryState.name());
            }

            Log.v(MainActivity.class.getCanonicalName(), "Finished starting!");
            Log.v(MainActivity.class.getCanonicalName(), "Running...");

            primaryState = AppState.RUNNING;

            // Performance optimizations
            performLaunchPerformanceOptimizations();
            
            // Handle launch from file request
            playTrackFromIntentRequest();
            
            // Alert presenters of app state
            alertPresentersOfAppState();
        }
        
        private void alertPresentersOfAppState()
        {
            if (!primaryState.isStarting() && !primaryState.isRunning())
            {
                throw new IllegalStateException("Invalid app state, cannot alert presenters of app state now, state is " + primaryState.name());
            }
            
            // Alert presenters that the app state has changed
            CachedTab currentTab = _tabNavigation.currentTab;
            
            if (currentTab != null)
            {
                currentTab.presenter.onAppStateChange(primaryState);
            }
            
            for (CachedTab tab : _tabNavigation.cachedTabs.values())
            {
                if (tab != currentTab)
                {
                    tab.presenter.onAppStateChange(primaryState);
                }
            }

            if (_quickPlayerPresenter != null)
            {
                _quickPlayerPresenter.onAppStateChange(primaryState);
            }
        }
    }
    
    private class TabNavigation {
        private CachedTab currentTab = null;
        private int currentTabID = 0;
        
        private int previousTabID = 0;
        private @Nullable BaseView previousTab = null;
        
        private Map<Integer, CachedTab> cachedTabs = new HashMap<>();
        
        private void setCurrentTabTo(int destinationTabID, boolean showQuickPlayer)
        {
            willSelectTab(destinationTabID);
            selectTab(destinationTabID);
            didSelectTab(destinationTabID);
            
            if (_quickPlayer != null)
            {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                
                if (showQuickPlayer)
                {
                    if (_quickPlayer.getView() == null)
                    {
                        transaction.attach(_quickPlayer);
                    }
                }
                else
                {
                    if (_quickPlayer.getView() != null)
                    {
                        transaction.detach(_quickPlayer);
                    }
                }
                
                transaction.commit();
            }
            
            // Alert presenters of app state
            _state.alertPresentersOfAppState();
        }
        
        private void willSelectTab(int destinationTabID)
        {
            cacheCurrentTab();
            
            // As soon as we cache the tab, we no longer need the backstack
            clearCurrentTabBackStack();
        }

        private void selectTab(int destinationTabID)
        {
            if (currentTab != null)
            {
                previousTabID = currentTabID;
                previousTab = currentTab.tab;
            }
            
            currentTabID = destinationTabID;
            
            // If cached, load from cache
            CachedTab cachedTab = cachedTabs.get(destinationTabID);

            if (cachedTab != null)
            {
                Log.v(MainActivity.class.getCanonicalName(), "Loaded tab from cache instead from scratch");
                currentTab = cachedTab;
                return;
            }
            
            CachedTab newTab = createTabFromScratch(currentTabID);
            
            if (newTab == null)
            {
                throw new RuntimeException("MainActivity could not create a new tab - invalid tab id");
            }

            currentTab = newTab;
        }
        
        private void didSelectTab(int destinationTabID)
        {
            if (previousTab == null)
            {
                replaceCurrentTab();
                return;
            }
            
            // First, deselect the previous tab
            deselectTab(previousTabID);
            
            // If current tab is already added, show it
            if (currentTabIsAlreadyAdded())
            {
                showCurrentTab();
            }
            // If current tab is not added, always add it
            else
            {
                addCurrentTab();
            }
        }
        
        private @Nullable CachedTab createTabFromScratch(int tabID)
        {
            BaseView tab = null;
            BasePresenter presenter = null;
            
            switch (tabID)
            {
                case R.id.navigation_albums:
                    presenter = new AlbumsPresenter(_audioLibrary);
                    tab = AlbumsFragment.newInstance(presenter);
                    presenter.setView(tab);
                    break;
                case R.id.navigation_lists:
                    presenter = new ListsPresenter(getBaseContext(), _audioLibrary);
                    tab = ListsFragment.newInstance(presenter);
                    presenter.setView(tab);
                    break;
                case R.id.navigation_search:
                    presenter = new SearchPresenter(getBaseContext(), _audioLibrary);
                    tab = SearchFragment.newInstance(presenter);
                    presenter.setView(tab);
                    break;
                case R.id.navigation_settings:
                    presenter = new SettingsPresenter(_audioLibrary);
                    tab = SettingsFragment.newInstance(presenter, MainActivity.this);
                    presenter.setView(tab);
                    break;
                default:
                    return null;
            }
            
            return new CachedTab(tab, presenter, null, null);
        }

        private void cacheCurrentTab()
        {
            if (!_state.isRunning())
            {
                return;
            }
            
            if (currentTab != null)
            {
                AppSettings.TabCachingPolicies policy = GeneralStorage.getShared().getCachingPolicy();

                boolean cacheTab = false;

                switch (currentTabID)
                {
                    case R.id.navigation_albums:
                        cacheTab = policy.cacheAlbumsTab();
                        break;
                    case R.id.navigation_lists:
                        cacheTab = policy.cacheListsTab();
                        break;
                    case R.id.navigation_search:
                        cacheTab = policy.cacheSearchTab();
                        break;
                    case R.id.navigation_settings:
                        cacheTab = policy.cacheSettingsTab();
                        break;
                }

                if (cacheTab)
                {
                    CachedTab current = currentTab;
                    
                    cachedTabs.put(currentTabID, CachedTab.create(current.tab, current.presenter, getSupportFragmentManager()));
                }
            }
        }

        private void clearTabCache()
        {
            cachedTabs.clear();
        }
        
        private boolean deselectTab(int tabID)
        {
            if (previousTab == null)
            {
                return true;
            }
            
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            
            // Returns true if tab will be removed instead of being hidden
            if (cacheContains(tabID))
            {
                transaction.hide((Fragment) previousTab);
                transaction.commit();
                return false;
            }

            transaction.remove((Fragment) previousTab);
            transaction.commit();
            return true;
        }
        
        private void replaceCurrentTab()
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.mainLayout, (Fragment) currentTab.tab);
            
            transaction.commit();
        }
        
        private void addCurrentTab()
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            
            if (!currentTabIsAlreadyAdded())
            {
                transaction.add(R.id.mainLayout, (Fragment) currentTab.tab);
            }
            else
            {
                transaction.show((Fragment) currentTab.tab);
            }

            transaction.commit();
        }

        private void showCurrentTab()
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            
            CachedTab cachedTab = cachedTabs.get(currentTabID);
            
            // Show only if there is no subview in the cache tab
            if (cachedTab == null)
            {
                transaction.show((Fragment) currentTab.tab);
            }
            // Else, try to restore subview
            else
            {
                // There is a cached tab subview - set the current tab hidden, add the subview
                if (cachedTab.tabSubview instanceof Fragment)
                {
                    Fragment fragment = (Fragment) cachedTab.tabSubview;
                    transaction.setCustomAnimations(R.anim.hold, R.anim.hold, R.anim.hold, R.anim.hold);
                    transaction.add(R.id.mainLayout, fragment, cachedTab.tabSubviewName);
                    transaction.addToBackStack(cachedTab.tabSubviewName);
                    transaction.hide((Fragment) currentTab.tab);
                }
                // There is a cached tab, but there is no subview, just show the tab
                else
                {
                    transaction.show((Fragment) currentTab.tab);
                }
            }
            
            transaction.commit();
        }

        private boolean isCurrentTabVisible()
        {
            return getSupportFragmentManager().getBackStackEntryCount() == 0;
        }

        private boolean cacheContains(int tabID)
        {
            return cachedTabs.get(tabID) != null;
        }

        private boolean currentTabIsAlreadyAdded()
        {
            return cacheContains(currentTabID);
        }

        private void clearCurrentTabBackStack()
        {
            FragmentManager manager = getSupportFragmentManager();

            while (manager.getBackStackEntryCount() > 0)
            {
                manager.popBackStackImmediate();
            }
        }
    }
}
