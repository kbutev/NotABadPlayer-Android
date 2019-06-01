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

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Presenter.ListsPresenter;
import com.media.notabadplayer.Presenter.QuickPlayerPresenter;
import com.media.notabadplayer.Storage.AudioStorage;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
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
import com.media.notabadplayer.View.CreateLists.CreateListsFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Settings.SettingsFragment;

enum MainActivityState {
    LAUNCHING, RUNNING;
    
    public boolean isLaunching()
    {
        return this == MainActivityState.LAUNCHING;
    }

    public boolean isRunning()
    {
        return this == MainActivityState.RUNNING;
    }
}

public class MainActivity extends AppCompatActivity implements BaseView {
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private MainActivityState _state = MainActivityState.LAUNCHING;

    private boolean _launchedFromFile;
    private Uri _launchedFromFileUri;
    
    private AudioStorage _audioStorage;
    private MainPresenter _presenter;

    private BottomNavigationView _navigation;
    
    private TabNavigation _tabNavigation = new TabNavigation();
    
    private QuickPlayerFragment _quickPlayer;
    
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        
        Log.v(MainActivity.class.getCanonicalName(), "Launching...");
        
        if (_state.isLaunching())
        {
            onCreateLaunch();
        }
        else
        {
            onCreateMain();
        }
    }
    
    private void onCreateLaunch()
    {
        GeneralStorage.getShared().init(getApplication());
        
        Intent intent = getIntent();
        
        if (intent != null)
        {
            _launchedFromFile = Intent.ACTION_VIEW.equals(intent.getAction());

            if (_launchedFromFile)
            {
                _launchedFromFileUri = intent.getData();
            }
        }
        
        requestPermissionForReadExtertalStorage();
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

        _audioStorage = new AudioStorage(this);

        // Audio Player initialization
        if (!AudioPlayer.getShared().isInitialized())
        {
            AudioPlayer.getShared().initialize(getApplication(), _audioStorage);
        }

        // UI
        initMainUI();

        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        _noiseSuppression.start(this);
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
        
        // Every time the main activity pauses, save the player state
        if (_state.isRunning())
        {
            saveCurrentAudioState();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        if (_noiseSuppression != null)
        {
            unregisterReceiver(_noiseSuppression);
        }
        
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
        if (!isOnAnRootTab())
        {
            super.onBackPressed();
            return;
        }

        // Currently on any of the first tabs? Send to background
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

    private void start()
    {
        if (!_state.isLaunching())
        {
            throw new IllegalStateException("MainActivity cannot start, its already started");
        }

        Log.v(MainActivity.class.getCanonicalName(), "Finished launching!");
        Log.v(MainActivity.class.getCanonicalName(), "Starting...");
        
        _state = MainActivityState.RUNNING;

        // Create main interface
        onCreateMain();

        // Start services here
        startServices();
    }
    
    private void finishedStarting()
    {
        // Performance optimizations
        performLaunchPerformanceOptimizations();

        // Handle launch from file request
        if (_launchedFromFile)
        {
            startAppWithTrack(_launchedFromFileUri);
        }
        
        // Done
        Log.v(MainActivity.class.getCanonicalName(), "Finished starting!");
    }
    
    private void startServices()
    {
        final MainActivity main = this;
        
        // Use background thread to start the services - user defaults, audio storage, etc...
        // Then, alert MainActivity on the main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(MainActivity.class.getCanonicalName(), "Starting services...");
                
                GeneralStorage.getShared().init(getApplication());
                
                _audioStorage.load();
                
                GeneralStorage.getShared().restorePlayerState();

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
        
        finishedStarting();
    }
    
    private void startAppWithTrack(Uri path)
    {
        Log.v(MainActivity.class.getCanonicalName(), "Launching player with initial track...");
        
        AudioTrack track = _audioStorage.findTrackByPath(path);

        if (track == null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with desired track: " + path.toString());
            return;
        }

        AudioPlaylist playlist = track.source.getSourcePlaylist(_audioStorage, track);

        if (playlist == null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with desired track: " + path.toString());
            return;
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("playlist", Serializing.serializeObject(playlist));
        startActivity(intent);

        // Transition animation
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
                        // Just start the app normally (transition from launch state)
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
        
        start();
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
        _navigation = findViewById(R.id.navigation);
        
        // Select default tab
        onTabItemSelected(DEFAULT_SELECTED_TAB_ID);
        
        // Create quick player and it's presenter
        BasePresenter presenter = new QuickPlayerPresenter(_audioStorage);
        _quickPlayer = QuickPlayerFragment.newInstance(presenter, this);
        presenter.setView(_quickPlayer);
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.quickPlayer, _quickPlayer).commit();
        
        // Start presenter
        _presenter.start();
        
        // Set bottom navigation menu listener
        _navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    
    private void performLaunchPerformanceOptimizations()
    {
        // Anything we can initialize early on here, so the user can get smoother experience
        // Optimization for the Settings screen
        GeneralStorage.getShared().retrieveAllSettingsActionValues();
    }
    
    private boolean isOnAnRootTab()
    {
        return getSupportFragmentManager().findFragmentById(R.id.mainLayout) == _tabNavigation.currentTab;
    }
    
    private void selectAlbumsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select albums tab");
        
        int tabID = R.id.navigation_albums;

        _tabNavigation.willSelectTab(tabID);
        _tabNavigation.selectTab(tabID);
        _tabNavigation.didSelectTab(tabID);
    }

    private void selectListsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select lists tab");

        int tabID = R.id.navigation_lists;

        _tabNavigation.willSelectTab(tabID);
        _tabNavigation.selectTab(tabID);
        _tabNavigation.didSelectTab(tabID);
    }

    private void selectSearchTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select search tab");

        int tabID = R.id.navigation_search;

        _tabNavigation.willSelectTab(tabID);
        _tabNavigation.selectTab(tabID);
        _tabNavigation.didSelectTab(tabID);
    }

    private void selectSettingsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select settings tab");

        int tabID = R.id.navigation_settings;

        _tabNavigation.willSelectTab(tabID);
        _tabNavigation.selectTab(tabID);
        _tabNavigation.didSelectTab(tabID);
    }

    private void onTabItemSelected(int itemID)
    {
        // If already selected, then try to go to the root tab
        if (_tabNavigation.currentTabID == itemID)
        {
            // Not on any of the first tabs? Go back
            if (!isOnAnRootTab())
            {
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

    private void updateQuickPlayerVisibility()
    {
        if (_quickPlayer != null)
        {
            if (_tabNavigation.currentTabID == R.id.navigation_settings)
            {
                if (_quickPlayer.getView() != null)
                {
                    final FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.detach(_quickPlayer);
                    transaction.commit();
                }
            }
            else
            {
                if (_quickPlayer.getView() == null)
                {
                    final FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.attach(_quickPlayer);
                    transaction.commit();
                }
            }
        }
    }
    
    private void saveCurrentAudioState()
    {
        GeneralStorage.getShared().savePlayerState();
        GeneralStorage.getShared().savePlayerPlayHistoryState();
    }
    
    private void restoreAudioPlayerState()
    {
        GeneralStorage.getShared().restorePlayerState();
        GeneralStorage.getShared().restorePlayerPlayHistoryState(getApplication());
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
            _tabNavigation.currentTab.openPlaylistScreen(_audioStorage, playlist);
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
    
    private class TabNavigation {
        private BaseView currentTab;
        private int currentTabID = 0;
        private int previousTabID = 0;
        private BaseView previousTab = null;
        private Map<Integer, CachedTab> cachedTabs = new HashMap<>();

        private void cacheCurrentTab()
        {
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
                    cachedTabs.put(currentTabID, CachedTab.create(currentTab, getSupportFragmentManager()));
                }
            }
        }
        
        private void clearTabCache()
        {
            cachedTabs.clear();
        }
        
        private void willSelectTab(int destinationTabID)
        {
            cacheCurrentTab();
            
            // As soon as we cache the tab, we no longer need the backstack
            clearCurrentTabBackStack();
        }

        private void selectTab(int destinationTabID)
        {
            previousTabID = currentTabID;
            previousTab = currentTab;
            currentTabID = destinationTabID;
            
            // If cached, load from cache
            CachedTab cachedTab = cachedTabs.get(destinationTabID);

            if (cachedTab != null)
            {
                Log.v(MainActivity.class.getCanonicalName(), "Loaded tab from cache instead from scratch");
                currentTab = cachedTab.tab;
                return;
            }
            
            currentTab = createTabFromScratch(currentTabID);
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
        
        private BaseView createTabFromScratch(int tabID)
        {
            BaseView tab = null;
            BasePresenter presenter = null;
            
            switch (tabID)
            {
                case R.id.navigation_albums:
                    presenter = new AlbumsPresenter(_audioStorage);
                    tab = AlbumsFragment.newInstance(presenter);
                    presenter.setView(tab);
                    break;
                case R.id.navigation_lists:
                    presenter = new ListsPresenter(_audioStorage);
                    tab = CreateListsFragment.newInstance(presenter);
                    presenter.setView(tab);
                    break;
                case R.id.navigation_search:
                    presenter = new SearchPresenter(getBaseContext(), _audioStorage);
                    tab = SearchFragment.newInstance(presenter);
                    presenter.setView(tab);
                    break;
                case R.id.navigation_settings:
                    presenter = new SettingsPresenter(_audioStorage);
                    tab = SettingsFragment.newInstance(presenter, MainActivity.this);
                    presenter.setView(tab);
                    break;
            }
            
            return tab;
        }
        
        private boolean deselectTab(int tabID)
        {
            // Returns true if tab will be removed instead of being hidden
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            
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
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.mainLayout, (Fragment) currentTab);
            transaction.commit();
        }
        
        private void addCurrentTab()
        {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            
            if (!currentTabIsAlreadyAdded())
            {
                transaction.add(R.id.mainLayout, (Fragment) currentTab);
            }
            else
            {
                transaction.show((Fragment) currentTab);
            }
            
            transaction.commit();
        }

        private void showCurrentTab()
        {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            
            CachedTab cachedTab = cachedTabs.get(currentTabID);

            // Show only if there is no subview in the cache tab
            if (cachedTab == null)
            {
                transaction.show((Fragment) currentTab);
            }
            // Else, try to restore subview
            else
            {
                // There is a cached tab subview - set the current tab hidden, add the subview
                if (cachedTab.tabSubview instanceof Fragment)
                {
                    Fragment fragment = (Fragment) cachedTab.tabSubview;
                    transaction.hide((Fragment) currentTab);
                    transaction.setCustomAnimations(0, R.anim.fade_in, 0, R.anim.hold);
                    transaction.addToBackStack(cachedTab.tabSubviewName);
                    transaction.add(R.id.mainLayout, fragment, cachedTab.tabSubviewName);
                }
                // There is a cached tab, but there is no subview, just show the tab
                else
                {
                    transaction.show((Fragment) currentTab);
                }
            }
            
            // Commit
            transaction.commit();
        }
        
        boolean cacheContains(int tabID)
        {
            return cachedTabs.get(tabID) != null;
        }
        
        boolean currentTabIsAlreadyAdded()
        {
            return cacheContains(currentTabID);
        }
        
        void clearCurrentTabBackStack()
        {
            FragmentManager manager = getSupportFragmentManager();

            while (manager.getBackStackEntryCount() > 0)
            {
                manager.popBackStackImmediate();
            }
        }
    }
}
