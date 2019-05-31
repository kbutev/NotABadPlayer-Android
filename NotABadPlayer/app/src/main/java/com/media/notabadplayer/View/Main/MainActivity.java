package com.media.notabadplayer.View.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
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

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Launch.LaunchActivity;
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
import com.media.notabadplayer.Utilities.AppThemeUtility;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Player.QuickPlayerFragment;
import com.media.notabadplayer.View.Lists.CreateListsFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Settings.SettingsFragment;

public class MainActivity extends AppCompatActivity implements BaseView {
    static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private AudioStorage _audioStorage;
    private MainPresenter _presenter;

    private BottomNavigationView _navigation;
    
    private BaseView _currentTab;
    
    private int _currentTabID = 0;
    private Map<Integer, CachedTab> _cachedTabs = new HashMap<>();
    
    private QuickPlayerFragment _quickPlayer;
    
    private AudioPlayerNoiseSuppression _noiseSuppression;
    
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final int currentTabID = _currentTabID;
            onTabItemSelected(item.getItemId());
            return currentTabID != _currentTabID;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Never restore this activity, instead, restart app
        if (savedInstanceState != null)
        {
            super.onCreate(null);
            Intent intent = new Intent(this, LaunchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        
        super.onCreate(null);
        
        // App theme
        AppThemeUtility.setTheme(this, GeneralStorage.getShared().getAppThemeValue());
        
        // Content
        setContentView(R.layout.activity_main);
        
        // Presenter, audio model
        _presenter = new MainPresenter();
        _presenter.setView(this);
        
        _audioStorage = new AudioStorage(this);
        _audioStorage.load();
        
        // Audio Player initialization
        if (!AudioPlayer.getShared().isInitialized())
        {
            AudioPlayer.getShared().initialize(getApplication(), _audioStorage);
        }
        
        // UI
        initUI();
        
        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        _noiseSuppression.start(this);
        
        // App launch track
        Uri path = getIntent().getParcelableExtra("launchTrackPath");
        
        boolean launchedWithInitialTrack = path != null;
        
        if (launchedWithInitialTrack)
        {
            startAppWithTrack(path);
        }
        else
        {
            restoreAudioPlayerState();
        }
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
        saveCurrentAudioState();
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
    
    private void initUI()
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
    
    private boolean isOnAnRootTab()
    {
        return getSupportFragmentManager().findFragmentById(R.id.mainLayout) == _currentTab;
    }
    
    private void selectAlbumsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select albums tab");

        cacheCurrentTab();

        // If cached, load from cache
        CachedTab cachedTab = _cachedTabs.get(R.id.navigation_albums);

        if (cachedTab != null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Load albums tab from cache");
            restoreTabFromCache(cachedTab, R.id.navigation_albums);
            return;
        }

        _currentTabID = R.id.navigation_albums;
        
        BasePresenter presenter = new AlbumsPresenter(_audioStorage);
        _currentTab = AlbumsFragment.newInstance(presenter);
        presenter.setView(_currentTab);
        
        refreshCurrentTab();
    }

    private void selectListsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select lists tab");

        cacheCurrentTab();

        // If cached, load from cache
        CachedTab cachedTab = _cachedTabs.get(R.id.navigation_lists);

        if (cachedTab != null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Load lists from cache");
            restoreTabFromCache(cachedTab, R.id.navigation_lists);
            return;
        }

        _currentTabID = R.id.navigation_lists;
        
        BasePresenter presenter = new ListsPresenter(_audioStorage);
        _currentTab = CreateListsFragment.newInstance(presenter);
        presenter.setView(_currentTab);

        refreshCurrentTab();
    }

    private void selectSearchTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select search tab");

        cacheCurrentTab();

        // If cached, load from cache
        CachedTab cachedTab = _cachedTabs.get(R.id.navigation_search);

        if (cachedTab != null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Load search tab from cache");
            restoreTabFromCache(cachedTab, R.id.navigation_search);
            return;
        }

        _currentTabID = R.id.navigation_search;
        
        BasePresenter presenter = new SearchPresenter(this, _audioStorage);
        _currentTab = SearchFragment.newInstance(presenter);
        presenter.setView(_currentTab);
        
        refreshCurrentTab();
    }

    private void selectSettingsTab()
    {
        Log.v(MainActivity.class.getCanonicalName(), "Select settings tab");

        cacheCurrentTab();

        // If cached, load from cache
        CachedTab cachedTab = _cachedTabs.get(R.id.navigation_settings);

        if (cachedTab != null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Load settings tab from cache");
            restoreTabFromCache(cachedTab, R.id.navigation_settings);
            return;
        }
        
        _currentTabID = R.id.navigation_settings;
        
        BasePresenter presenter = new SettingsPresenter( _audioStorage);
        _currentTab = SettingsFragment.newInstance(presenter, this);
        presenter.setView(_currentTab);
        
        refreshCurrentTab();
    }

    private void clearCurrentTabBackStack()
    {
        FragmentManager manager = getSupportFragmentManager();

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }
    }
    
    private void refreshCurrentTab()
    {
        clearCurrentTabBackStack();

        // Fragment - make sure the current tab is the in the main layout
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction().replace(R.id.mainLayout, (Fragment)_currentTab);
        transaction.commit();

        // Quick player is hidden for the settings screen
        updateQuickPlayerVisibility();
    }
    
    private void clearTabCache()
    {
        _cachedTabs.clear();
    }

    private void cacheCurrentTab()
    {
        switch (_currentTabID)
        {
            case R.id.navigation_albums:
                if (GeneralStorage.getShared().getCachingPolicy().cacheAlbumsTab())
                {
                    _cachedTabs.put(R.id.navigation_albums, CachedTab.create(_currentTab, getSupportFragmentManager()));
                }

                break;
            case R.id.navigation_lists:
                if (GeneralStorage.getShared().getCachingPolicy().cacheListsTab())
                {
                    _cachedTabs.put(R.id.navigation_lists, CachedTab.create(_currentTab, getSupportFragmentManager()));
                }

                break;
            case R.id.navigation_search:
                if (GeneralStorage.getShared().getCachingPolicy().cacheSearchTab())
                {
                    _cachedTabs.put(R.id.navigation_search, CachedTab.create(_currentTab, getSupportFragmentManager()));
                }

                break;
            case R.id.navigation_settings:
                if (GeneralStorage.getShared().getCachingPolicy().cacheSettingsTab())
                {
                    _cachedTabs.put(R.id.navigation_settings, CachedTab.create(_currentTab, getSupportFragmentManager()));
                }

                break;
        }
    }

    private void restoreTabFromCache(@NonNull CachedTab cachedTab, int tabID)
    {
        _currentTab = cachedTab.tab;
        _currentTabID = tabID;

        refreshCurrentTab();

        if (cachedTab.tabSubview != null)
        {
            Fragment f = (Fragment)cachedTab.tabSubview;

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(0, 0, 0, R.anim.hold);
            transaction.addToBackStack(cachedTab.tabSubviewName);
            transaction.replace(R.id.mainLayout, f, cachedTab.tabSubviewName);
            transaction.commit();
        }
    }

    private void onTabItemSelected(int itemID)
    {
        // If already selected, then try to go to the root tab
        if (_currentTabID == itemID)
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
            if (_currentTabID == R.id.navigation_settings)
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

    private void startAppWithTrack(@NonNull Uri path)
    {
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
        if (_currentTabID != R.id.navigation_settings)
        {
            _currentTab.openPlaylistScreen(_audioStorage, playlist);
        }
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums)
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
    public void searchQueryResults(@NonNull String searchQuery, @NonNull ArrayList<AudioTrack> songs, @Nullable String searchTip)
    {

    }

    @Override
    public void appSettingsReset()
    {
        Log.v(MainActivity.class.getSimpleName(), "App settings were reset");

        clearTabCache();
    }
    
    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {
        Log.v(MainActivity.class.getSimpleName(), "App theme changed to " + appTheme.name());

        clearTabCache();

        AppThemeUtility.setTheme(this, appTheme);
        
        _quickPlayer.appThemeChanged(appTheme);
    }
    
    @Override
    public void appTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {
        Log.v(MainActivity.class.getSimpleName(), "App track sorting changed.");

        clearTabCache();
        
        _quickPlayer.appTrackSortingChanged(trackSorting);
    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {
        Log.v(MainActivity.class.getSimpleName(), "App appearance changed.");

        clearTabCache();
        
        _quickPlayer.onShowVolumeBarSettingChange(value);
    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
}
