package com.media.notabadplayer.View.Main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Launch.LaunchActivity;
import com.media.notabadplayer.Presenter.Player.QuickPlayerPresenter;
import com.media.notabadplayer.Presenter.Playlist.PlaylistPresenter;
import com.media.notabadplayer.Storage.AudioStorage;
import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.Albums.AlbumsPresenter;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.Presenter.Search.SearchPresenter;
import com.media.notabadplayer.Presenter.Settings.SettingsPresenter;
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
import com.media.notabadplayer.View.Playlist.PlaylistFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BaseView {
    static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private AudioStorage _audioInfo;
    private MainPresenter _presenter;

    BottomNavigationView _navigation;
    
    private BaseView _currentTab;
    
    private int _currentTabID = 0;
    private Map<Integer, BaseView> _cachedTabs = new HashMap<>();
    
    private BaseView _quickPlayer;
    
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
        _presenter = new MainPresenter(this);
        
        _audioInfo = new AudioStorage(this);
        _audioInfo.load();
        
        // Audio Player initialization
        if (!AudioPlayer.getShared().isInitialized())
        {
            AudioPlayer.getShared().initialize(getApplication(), _audioInfo);
        }
        
        // UI
        initUI();
        
        _navigation = findViewById(R.id.navigation);
        _navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        
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
    
    private void initUI()
    {
        // Select default tab
        onTabItemSelected(DEFAULT_SELECTED_TAB_ID);
        
        // Create quick player and it's presenter
        QuickPlayerFragment quickPlayer = QuickPlayerFragment.newInstance();
        _quickPlayer = quickPlayer;
        _quickPlayer.setPresenter(new QuickPlayerPresenter(_quickPlayer, this));
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.quickPlayer, (Fragment)_quickPlayer).commit();
        
        // Start presenter
        _presenter.start();
    }
    
    private boolean isOnAnRootTab()
    {
        return getSupportFragmentManager().findFragmentById(R.id.mainLayout) == _currentTab;
    }
    
    private void selectAlbumsTab()
    {
        _currentTabID = R.id.navigation_albums;
        _currentTab = AlbumsFragment.newInstance();
        _currentTab.setPresenter(new AlbumsPresenter(_currentTab, _audioInfo));
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicy().cacheAlbumsTab())
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }

    private void selectListsTab()
    {
        _currentTabID = R.id.navigation_lists;
        _currentTab = CreateListsFragment.newInstance();
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicy().cacheListsTab())
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }

    private void selectSearchTab()
    {
        _currentTabID = R.id.navigation_search;
        _currentTab = SearchFragment.newInstance();
        _currentTab.setPresenter(new SearchPresenter(_currentTab, this, _audioInfo));
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicy().cacheSearchTab())
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }

    private void selectSettingsTab()
    {
        _currentTabID = R.id.navigation_settings;
        _currentTab = SettingsFragment.newInstance();
        _currentTab.setPresenter(new SettingsPresenter(_currentTab, this, this));
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicy().cacheSettingsTab())
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }
    
    private void refreshCurrentTab()
    {
        FragmentManager manager = getSupportFragmentManager();

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }

        FragmentTransaction transaction = manager.beginTransaction().replace(R.id.mainLayout, (Fragment)_currentTab);
        transaction.commit();
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

        // If cached, load from cache
        BaseView cachedView = _cachedTabs.get(itemID);

        if (cachedView != null)
        {
            _currentTab = cachedView;
            _currentTabID = itemID;
            refreshCurrentTab();
            return;
        }
        
        // If not cached, load from scratch
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

    private void startAppWithTrack(@NonNull Uri path)
    {
        AudioTrack track = _audioInfo.findTrackByPath(path);

        if (track == null)
        {
            Log.v(MainActivity.class.getCanonicalName(), "Error: cannot start app with desired track: " + path.toString());
            return;
        }

        AudioPlaylist playlist = track.source.getSourcePlaylist(_audioInfo, track);
        
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
    
    private void restoreAudioPlayerState()
    {
        GeneralStorage.getShared().restorePlayerState();
        GeneralStorage.getShared().restorePlayerPlayHistoryState(getApplication());
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
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        
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
    public void openPlaylistScreen(@NonNull AudioAlbum album)
    {
        // When not on the settings tab, let the view handle the operation
        if (_currentTabID != R.id.navigation_settings)
        {
            _currentTab.openPlaylistScreen(album);
            return;
        }
        // ... or when on the settings tab, navigate to the albums tab, and then open the playlist fragment

        View view = _navigation.findViewById(R.id.navigation_albums);
        view.performClick();

        FragmentManager manager = getSupportFragmentManager();
        int backStackCount = manager.getBackStackEntryCount();
        
        String newEntryName = album.albumTitle;
        String lastEntryName = backStackCount > 0 ? manager.getBackStackEntryAt(backStackCount-1).getName() : "";

        // Do nothing, if the last entry name is equal to the new entry name
        if (lastEntryName != null && lastEntryName.equals(newEntryName))
        {
            return;
        }

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }

        PlaylistFragment f = PlaylistFragment.newInstance();
        PlaylistPresenter presenter = new PlaylistPresenter(f, album);
        f.setPresenter(presenter);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(0, R.anim.fade_in, 0, R.anim.hold);
        transaction.replace(R.id.mainLayout, f);
        transaction.addToBackStack(newEntryName).commit();
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioPlaylist playlist)
    {
        // When not on the settings tab, let the view handle the operation
        if (_currentTabID != R.id.navigation_settings)
        {
            _currentTab.openPlaylistScreen(playlist);
            return;
        }
        // ... or when on the settings tab, navigate to the albums tab, and then open the playlist fragment
        
        View view = _navigation.findViewById(R.id.navigation_albums);
        view.performClick();

        FragmentManager manager = getSupportFragmentManager();
        int backStackCount = manager.getBackStackEntryCount();
        
        String newEntryName = playlist.getName();
        String lastEntryName = backStackCount > 0 ? manager.getBackStackEntryAt(backStackCount-1).getName() : "";

        // Do nothing, if the last entry name is equal to the new entry name
        if (lastEntryName != null && lastEntryName.equals(newEntryName))
        {
            return;
        }

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }

        PlaylistFragment f = PlaylistFragment.newInstance();
        PlaylistPresenter presenter = new PlaylistPresenter(f, playlist);
        f.setPresenter(presenter);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(0, R.anim.fade_in, 0, R.anim.hold);
        transaction.replace(R.id.mainLayout, f);
        transaction.addToBackStack(newEntryName).commit();
    }

    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums)
    {

    }

    @Override
    public void onAlbumSongsLoad(@NonNull ArrayList<AudioTrack> songs)
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
    public void searchQueryResults(@NonNull String searchQuery, @NonNull ArrayList<AudioTrack> songs)
    {

    }

    @Override
    public void appSettingsReset()
    {

    }
    
    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {
        Log.v(MainActivity.class.getSimpleName(), "App theme changed to " + appTheme.name());

        AppThemeUtility.setTheme(this, appTheme);
        
        _currentTab.appThemeChanged(appTheme);
        _quickPlayer.appThemeChanged(appTheme);
    }
    
    @Override
    public void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {
        Log.v(MainActivity.class.getSimpleName(), "App sorting changed.");
        
        _currentTab.appSortingChanged(albumSorting, trackSorting);
        _quickPlayer.appSortingChanged(albumSorting, trackSorting);
    }

    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {
        Log.v(MainActivity.class.getSimpleName(), "App appearance changed.");
        
        _currentTab.appAppearanceChanged(showStars, showVolumeBar);
        _quickPlayer.appAppearanceChanged(showStars, showVolumeBar);
    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
}
