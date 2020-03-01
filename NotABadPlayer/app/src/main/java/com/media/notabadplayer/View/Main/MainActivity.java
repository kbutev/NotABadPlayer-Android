package com.media.notabadplayer.View.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
import com.media.notabadplayer.R;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Presenter.ListsPresenter;
import com.media.notabadplayer.Presenter.QuickPlayerPresenter;
import com.media.notabadplayer.Storage.AudioLibrary;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.AlbumsPresenter;
import com.media.notabadplayer.Presenter.MainPresenter;
import com.media.notabadplayer.Presenter.SearchPresenter;
import com.media.notabadplayer.Presenter.SettingsPresenter;
import com.media.notabadplayer.Storage.GeneralStorage;
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
    public static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private boolean _appIsRunning = false;
    
    private boolean _launchedFromFile;
    private Uri _launchedFromFileUri;
    
    private AudioLibrary _audioLibrary = AudioLibrary.getShared();
    private MainPresenter _presenter;
    
    private TabNavigation _tabNavigation = new TabNavigation();
    
    private QuickPlayerFragment _quickPlayer = null;
    private BasePresenter _quickPlayerPresenter = null;
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
        _presenter = new MainPresenter();
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
        _quickPlayerPresenter = new QuickPlayerPresenter(_audioLibrary);
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

        // Save the current id to storage
        GeneralStorage.getShared().saveCurrentlySelectedNavigationTab(itemID);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioInfo audioInfo, @NonNull BaseAudioPlaylist playlist, @NonNull OpenPlaylistOptions options)
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
                    view.openPlaylistScreen(_audioLibrary, playlist, options);
                }
            }
        }
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull List<AudioAlbum> albums)
    {

    }

    @Override
    public void onPlaylistLoad(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void onUserPlaylistsLoad(@NonNull List<BaseAudioPlaylist> playlists)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void updatePlayerScreen(@NonNull BaseAudioPlaylist playlist)
    {

    }

    @Override
    public void updateSearchQueryResults(@NonNull String searchQuery, com.media.notabadplayer.Constants.SearchFilter filter, @NonNull List<BaseAudioTrack> songs, @Nullable String searchState)
    {

    }

    @Override
    public void openCreatePlaylistScreen(@Nullable BaseAudioPlaylist playlistToEdit)
    {

    }

    @Override
    public void onAppSettingsLoad(com.media.notabadplayer.Storage.GeneralStorage storage)
    {

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

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {
        Log.v(MainActivity.class.getSimpleName(), "App appearance changed.");

        _tabNavigation.clearTabCache();
        
        _quickPlayer.onShowVolumeBarSettingChange(value);
    }

    @Override
    public void onDeviceLibraryChanged()
    {

    }

    @Override
    public void onFetchDataErrorEncountered(@NonNull Exception error)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

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
            cacheCurrentTab();
            
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
                    presenter = new SettingsPresenter();
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
            if (currentTab != null)
            {
                boolean cacheTab = false;

                switch (currentTabID)
                {
                    case R.id.navigation_albums:
                        cacheTab = cachingPolicy.cacheAlbumsTab();
                        break;
                    case R.id.navigation_lists:
                        cacheTab = cachingPolicy.cacheListsTab();
                        break;
                    case R.id.navigation_search:
                        cacheTab = cachingPolicy.cacheSearchTab();
                        break;
                    case R.id.navigation_settings:
                        cacheTab = cachingPolicy.cacheSettingsTab();
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

            // Since tab is getting removed, alert the presenter of its destruction
            CachedTab previousTab = cachedTabs.get(previousTabID);

            if (previousTab != null)
            {
                previousTab.presenter.onDestroy();
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
