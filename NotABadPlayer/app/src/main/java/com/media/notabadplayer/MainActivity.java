package com.media.notabadplayer;

import java.util.HashMap;
import java.util.Map;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.media.notabadplayer.Audio.Model.AudioPlaylistBuilder;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylistBuilderNode;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.MVP.BaseRootView;
import com.media.notabadplayer.Presenter.Albums.AlbumsPresenter;
import com.media.notabadplayer.Presenter.Lists.ListsPresenter;
import com.media.notabadplayer.Presenter.Lists.ListsPresenterImpl;
import com.media.notabadplayer.Presenter.Player.QuickPlayerPresenter;
import com.media.notabadplayer.Presenter.Player.QuickPlayerPresenterImpl;
import com.media.notabadplayer.Presenter.Search.SearchPresenter;
import com.media.notabadplayer.Presenter.Settings.SettingsPresenter;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.Albums.AlbumsPresenterImpl;
import com.media.notabadplayer.Presenter.Main.MainPresenterImpl;
import com.media.notabadplayer.Presenter.Search.SearchPresenterImpl;
import com.media.notabadplayer.Presenter.Settings.SettingsPresenterImpl;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AppThemeUtility;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.MVP.BaseView;
import com.media.notabadplayer.Other.CachedTab;
import com.media.notabadplayer.View.Albums.AlbumsView;
import com.media.notabadplayer.View.Lists.ListsView;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Player.QuickPlayerFragment;
import com.media.notabadplayer.View.Lists.ListsFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Search.SearchView;
import com.media.notabadplayer.View.Settings.SettingsFragment;
import com.media.notabadplayer.View.Settings.SettingsView;

public class MainActivity extends AppCompatActivity implements BaseRootView {
    public static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private boolean _appIsRunning = false;
    
    private boolean _launchedFromFile;
    private Uri _launchedFromFileUri;
    
    private AudioLibrary _audioLibrary = AudioLibrary.getShared();
    private MainPresenterImpl _presenter;
    
    private TabNavigation _tabNavigation = new TabNavigation();
    
    private QuickPlayerFragment _quickPlayer = null;
    private QuickPlayerPresenter _quickPlayerPresenter = null;
    private boolean _quickPlayerIsHidden = false;
    
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final int currentTabID = _tabNavigation.currentTabID;
            onTabItemSelected(item.getItemId());
            return currentTabID != _tabNavigation.currentTabID;
        }
    };

    private BroadcastReceiver applicationRunningListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String value = intent.getAction();

            if (value != null)
            {
                start();
            }
        }
    };

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
        
        // Starts listening to application events
        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.broadcast_application_is_running));
        registerReceiver(applicationRunningListener, filter);

        // Load data from intent
        Intent intent = getIntent();
        
        if (intent != null)
        {
            loadDataFromIntent(intent);
        }

        // App theme
        AppThemeUtility.setTheme(this, GeneralStorage.getShared().getAppThemeValue());
        
        // Content
        setContentView(R.layout.activity_main);

        // Presenter setup
        _presenter = new MainPresenterImpl();
        _presenter.setView(this);

        // UI
        initMainUI();
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();

        // Save audio player state
        if (_appIsRunning)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Saving audio player state.");
            GeneralStorage.getShared().savePlayerState();
            GeneralStorage.getShared().savePlayerPlayHistoryState();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        unregisterReceiver(applicationRunningListener);
        
        // Save audio player state
        if (_appIsRunning)
        {
            GeneralStorage.getShared().savePlayerState();
            GeneralStorage.getShared().savePlayerPlayHistoryState();
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

        // Currently on any of the first tabs?
        // If this is the root activity (aka was launched from home)
        // Return to home
        if (isTaskRoot())
        {
            Log.v(MainActivity.class.getCanonicalName(), "Navigate back to home screen");
            moveTaskToBack(true);
            return;
        }
        
        // If this activity was launched from another program, return to the previous activity
        Log.v(MainActivity.class.getCanonicalName(), "Navigate back to the client program");
        super.onBackPressed();
    }
    
    private void start()
    {
        if (_appIsRunning)
        {
            return;
        }
        
        // This is called by the broadcast receiver
        // which listens to application events
        // When the application is completely finished with initialization it sends that event,
        // which leads to this method being called
        _appIsRunning = true;
        
        // Alert all presenters
        _presenter.onAppStateChange(AppState.RUNNING);
        
        CachedTab currentTab = _tabNavigation.currentTab;

        currentTab.presenter.onAppStateChange(AppState.RUNNING);
        
        for (CachedTab tab : _tabNavigation.cachedTabs.values())
        {
            if (currentTab != tab)
            {
                tab.presenter.onAppStateChange(AppState.RUNNING);
            }
        }
        
        _quickPlayerPresenter.onAppStateChange(AppState.RUNNING);

        // When starting, try to play the intent file
        playTrackFromIntentRequest();
    }

    private void loadDataFromIntent(@NonNull Intent intent)
    {
        if (Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            _launchedFromFileUri = intent.getData();
            _launchedFromFile = _launchedFromFileUri != null;
        }
        else
        {
            _launchedFromFile = false;
        }
    }
    
    private void playTrackFromIntentRequest()
    {
        if (_launchedFromFile)
        {
            startAppWithTrack(_launchedFromFileUri);
        }
    }
    
    private void startAppWithTrack(@NonNull Uri path)
    {
        Log.v(MainActivity.class.getCanonicalName(), "Launching player with initial track...");

        BaseAudioTrack track = _audioLibrary.findTrackByPath(path);

        if (track == null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with non-existing track: " + path.toString());
            return;
        }

        BaseAudioPlaylist playlist = track.getSource().getSourcePlaylist(_audioLibrary, track);

        if (playlist == null)
        {
            BaseAudioPlaylistBuilderNode node = AudioPlaylistBuilder.start();
            node.setName(track.getTitle());
            node.setTracksToOneTrack(track);

            // Try to build
            try {
                playlist = node.build();
            } catch (Exception e) {
                Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with given track, failed to build playlist");
                return;
            }
        }

        BaseAudioPlaylist currentPlaylist = Player.getShared().getPlaylist();
        
        if (currentPlaylist != null)
        {
            if (playlist.getPlayingTrack().equals(currentPlaylist.getPlayingTrack()))
            {
                return;
            }
        }
        
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("playlist", Serializing.serializeObject(playlist));
        startActivity(intent);
        
        overridePendingTransition(0, 0);
    }
    
    private void initMainUI()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Initializing main UI components...");

        // Bottom navigation menu
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        
        // Select default tab
        GeneralStorage storage = GeneralStorage.getShared();
        int defaultSelectionId = storage.getCurrentlySelectedNavigationTab();

        if (!isTabIdValid(defaultSelectionId))
        {
            onTabItemSelected(DEFAULT_SELECTED_TAB_ID);
            navigationView.setSelectedItemId(DEFAULT_SELECTED_TAB_ID);
        }
        else
        {
            onTabItemSelected(defaultSelectionId);
            navigationView.setSelectedItemId(defaultSelectionId);
        }

        // Create quick player and it's presenter
        initQuickPlayer();
        
        // Set bottom navigation menu listener
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.v(MainActivity.class.getCanonicalName(), "Main UI components initialized!");
    }

    private void initQuickPlayer()
    {
        _quickPlayerPresenter = new QuickPlayerPresenterImpl(_audioLibrary);
        _quickPlayer = QuickPlayerFragment.newInstance(_quickPlayerPresenter, this);
        _quickPlayerPresenter.setView(_quickPlayer);

        FragmentManager manager = getSupportFragmentManager();

        if (!_quickPlayerIsHidden)
        {
            manager.beginTransaction().replace(R.id.quickPlayer, _quickPlayer).commit();
        }
        else
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.quickPlayer, _quickPlayer);
            transaction.detach(_quickPlayer);
            transaction.commit();
        }
    }

    private boolean isTabIdValid(int tabId)
    {
        return tabId == R.id.navigation_albums ||
                tabId == R.id.navigation_lists ||
                tabId == R.id.navigation_search ||
                tabId == R.id.navigation_settings;
    }

    private void selectAlbumsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select albums tab");
        
        int tabID = R.id.navigation_albums;

        _tabNavigation.setCurrentTabTo(tabID);
        showQuickPlayer();
    }

    private void selectListsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select lists tab");

        int tabID = R.id.navigation_lists;

        _tabNavigation.setCurrentTabTo(tabID);
        showQuickPlayer();
    }

    private void selectSearchTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select search tab");

        int tabID = R.id.navigation_search;

        _tabNavigation.setCurrentTabTo(tabID);
        showQuickPlayer();
    }

    private void selectSettingsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select settings tab");

        int tabID = R.id.navigation_settings;

        _tabNavigation.setCurrentTabTo(tabID);
        hideQuickPlayer();
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

        if (itemID == R.id.navigation_albums) {
            selectAlbumsTab();
        } else if (itemID == R.id.navigation_lists) {
            selectListsTab();
        } else if (itemID == R.id.navigation_search) {
            selectSearchTab();
        } else if (itemID == R.id.navigation_settings) {
            selectSettingsTab();
        } else {
            assert false;
        }

        // Save the current id to storage
        GeneralStorage.getShared().saveCurrentlySelectedNavigationTab(itemID);
    }

    // BaseRootView

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options)
    {
        // When not on the settings tab, let the view handle the request
        if (_tabNavigation.currentTabID != R.id.navigation_settings)
        {
            // Make sure that the current tab exists and has been created (onCreate() was called)
            if (_tabNavigation.currentTab != null)
            {
                BaseRootView view = _tabNavigation.currentTab.tab;
                Fragment fragment = (Fragment) view;
                
                if (fragment.getView() != null)
                {
                    try {
                        view.openPlaylistScreen(_audioLibrary, playlist, options);
                    } catch (Exception e) {
                        Log.v(MainActivity.class.getCanonicalName(), "Failed to open playlist screen, error: " + e);
                    }
                }
            }
        }
    }

    @Override
    public void onResetAppSettings()
    {
        Log.v(MainActivity.class.getSimpleName(), "App settings were reset");

        _tabNavigation.clearTabCache();
    }
    
    @Override
    public void onAppThemeChanged(AppSettings.AppTheme appTheme)
    {
        Log.v(MainActivity.class.getSimpleName(), "App theme changed to " + appTheme.name());

        _tabNavigation.clearTabCache();

        AppThemeUtility.setTheme(this, appTheme);
        
        _quickPlayer.onAppThemeChanged(appTheme);
    }
    
    @Override
    public void onAppTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {
        Log.v(MainActivity.class.getSimpleName(), "App track sorting changed.");

        _tabNavigation.clearTabCache();
        
        _quickPlayer.onAppTrackSortingChanged(trackSorting);
    }

    private void showQuickPlayer()
    {
        _quickPlayerIsHidden = false;

        if (_quickPlayer == null)
        {
            return;
        }

        if (_quickPlayer.getView() == null)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.attach(_quickPlayer);
            transaction.commit();
        }
    }

    private void hideQuickPlayer()
    {
        _quickPlayerIsHidden = true;

        if (_quickPlayer == null)
        {
            return;
        }

        if (_quickPlayer.getView() != null)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.detach(_quickPlayer);
            transaction.commit();
        }
    }
    
    private class TabNavigation {
        AppSettings.TabCachingPolicies cachingPolicy = AppSettings.TabCachingPolicies.ALBUMS_ONLY;
        
        private CachedTab currentTab = null;
        private int currentTabID = 0;
        
        private int previousTabID = 0;
        private @Nullable BaseView previousTabView = null;
        
        private Map<Integer, CachedTab> cachedTabs = new HashMap<>();
        
        private void setCurrentTabTo(int destinationTabID)
        {
            willSelectTab(destinationTabID);
            selectTab(destinationTabID);
            didSelectTab(destinationTabID);
        }
        
        private void willSelectTab(int destinationTabID)
        {
            boolean didCacheCurrentTab = cacheCurrentTab();

            // On destroy event
            if (!didCacheCurrentTab && currentTab != null) {
                currentTab.presenter.onDestroy();
            }
            
            // As soon as we cache the tab, clear the backstack
            clearCurrentTabBackStack();
        }

        private void selectTab(int destinationTabID)
        {
            if (currentTab != null)
            {
                previousTabID = currentTabID;
                previousTabView = currentTab.tab;
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

            // Alert presenter of app state
            if (_appIsRunning)
            {
                currentTab.presenter.onAppStateChange(AppState.RUNNING);
            }
        }
        
        private void didSelectTab(int destinationTabID)
        {
            if (previousTabView == null)
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
            BaseRootView v = null;
            BasePresenter p = null;

            if (tabID == R.id.navigation_albums) {
                AlbumsPresenter presenter = new AlbumsPresenterImpl(_audioLibrary);
                AlbumsView view = AlbumsFragment.newInstance(presenter);
                presenter.setView(view);
                p = presenter;
                v = view;
            } else if (tabID == R.id.navigation_lists) {
                ListsPresenter presenter = new ListsPresenterImpl(getBaseContext(), _audioLibrary);
                ListsView view = ListsFragment.newInstance(presenter);
                presenter.setView(view);
                p = presenter;
                v = view;
            } else if (tabID == R.id.navigation_search) {
                SearchPresenter presenter = new SearchPresenterImpl(getBaseContext(), _audioLibrary);
                SearchView view = SearchFragment.newInstance(presenter);
                presenter.setView(view);
                p = presenter;
                v = view;
            } else if (tabID == R.id.navigation_settings) {
                SettingsPresenter presenter = new SettingsPresenterImpl();
                SettingsView view = SettingsFragment.newInstance(presenter, MainActivity.this);
                presenter.setView(view);
                p = presenter;
                v = view;
            } else {
                return null;
            }

            return new CachedTab(v, p, null, null);
        }

        private boolean cacheCurrentTab()
        {
            if (currentTab != null)
            {
                boolean shouldCacheTab = false;

                if (currentTabID == R.id.navigation_albums) {
                    shouldCacheTab = cachingPolicy.cacheAlbumsTab();
                } else if (currentTabID == R.id.navigation_lists) {
                    shouldCacheTab = cachingPolicy.cacheListsTab();
                } else if (currentTabID == R.id.navigation_search) {
                    shouldCacheTab = cachingPolicy.cacheSearchTab();
                } else if (currentTabID == R.id.navigation_settings) {
                    shouldCacheTab = cachingPolicy.cacheSettingsTab();
                } else {
                    assert false;
                }

                if (shouldCacheTab)
                {
                    CachedTab current = currentTab;
                    
                    cachedTabs.put(currentTabID, CachedTab.create(current.tab, current.presenter, getSupportFragmentManager()));

                    return true;
                }
            }

            return false;
        }

        private void clearTabCache()
        {
            cachedTabs.clear();
        }
        
        private void deselectTab(int tabID)
        {
            if (previousTabView == null)
            {
                return;
            }
            
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            
            if (cacheContains(tabID))
            {
                transaction.hide((Fragment) previousTabView);
                transaction.commit();
                return;
            }

            // Remove view
            transaction.remove((Fragment) previousTabView);
            transaction.commit();
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
