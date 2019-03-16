package com.media.notabadplayer.View.Main;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
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
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.PlayerActivity;
import com.media.notabadplayer.View.Player.QuickPlayerFragment;
import com.media.notabadplayer.View.Lists.ListsFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

public class MainActivity extends AppCompatActivity implements BaseView {
    static final int DEFAULT_SELECTED_TAB_ID = R.id.navigation_albums;
    
    private AudioInfo _audioInfo;
    private MainPresenter _presenter;
    
    private BaseView _currentTab;
    
    private int _currentTabID;
    private Map<Integer, BaseView> _cachedTabs = new HashMap<>();
    
    private BaseView _quickPlayer;
    
    private AudioPlayerNoiseSuppression _noiseSuppression;
    
    private boolean _launchedWithInitialTrack;
    
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
        super.onCreate(savedInstanceState);
        
        // Content
        setContentView(R.layout.activity_main);
        
        // Presenter, audio model
        _presenter = new MainPresenter(this);
        
        _audioInfo = new AudioInfo(this);
        
        // UI
        initUI();
        
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        
        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        registerReceiver(_noiseSuppression, new IntentFilter(ACTION_AUDIO_BECOMING_NOISY));
        
        // App launch track
        Uri path = getIntent().getParcelableExtra("launchTrackPath");
        _launchedWithInitialTrack = path != null;
        
        if (_launchedWithInitialTrack)
        {
            startAppWithTrack(path);
        }
    }
    
    @Override
    protected void onDestroy()
    {
        unregisterReceiver(_noiseSuppression);
        super.onDestroy();
    }
    
    private void initUI()
    {
        // Select default tab
        onTabItemSelected(DEFAULT_SELECTED_TAB_ID);
        
        // Start
        _presenter.start();
        
        _quickPlayer = QuickPlayerFragment.newInstance();
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.quickPlayer, (Fragment)_quickPlayer).commit();
    }
    
    private void selectAlbumsTab()
    {
        _currentTabID = R.id.navigation_albums;
        _currentTab = AlbumsFragment.newInstance();
        _currentTab.setPresenter(new AlbumsPresenter(_currentTab, _audioInfo));
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicyFlagForAlbumsTab(this))
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }

    private void selectListsTab()
    {
        _currentTabID = R.id.navigation_lists;
        _currentTab = ListsFragment.newInstance();
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicyFlagForListsTab(this))
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }

    private void selectSearchTab()
    {
        _currentTabID = R.id.navigation_search;
        _currentTab = SearchFragment.newInstance();
        _currentTab.setPresenter(new SearchPresenter(_currentTab, _audioInfo));
        refreshCurrentTab();
        
        if (GeneralStorage.getShared().getCachingPolicyFlagForSearchTab(this))
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
        
        if (GeneralStorage.getShared().getCachingPolicyFlagForSettingsTab(this))
        {
            _cachedTabs.put(R.id.navigation_albums, _currentTab);
        }
    }
    
    private void refreshCurrentTab()
    {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, (Fragment)_currentTab).commit();
    }
    
    private void onTabItemSelected(int itemID)
    {
        // If already selected, do nothing
        if (_currentTabID == itemID)
        {
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

    private void startAppWithTrack(Uri path)
    {
        AudioTrack track = _audioInfo.findTrackByPath(path);
        AudioPlaylist playlist = new AudioPlaylist(track);
        
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("playlist", Serializing.serializeObject(AudioPlayer.getShared().getPlaylist()));
        startActivity(intent);
        
        // Transition animation
        overridePendingTransition(0, 0);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
        {
            KeyBinds.getShared().evaluateInput(this, ApplicationInput.QUICK_PLAYER_VOLUME_UP_BUTTON);
            return true;
        }
        
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
        {
            KeyBinds.getShared().evaluateInput(this, ApplicationInput.QUICK_PLAYER_VOLUME_DOWN_BUTTON);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onBackPressed()
    {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainLayout);
        
        // Not on any of the first tabs? Go back
        if (currentFragment != _currentTab)
        {
            super.onBackPressed();
            return;
        }
        
        // Currently on any of the first tabs? Send to background
        moveTaskToBack(true);
    }
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        
    }

    @Override
    public void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover) {
        
    }

    @Override
    public void onMediaAlbumsLoad(ArrayList<AudioAlbum> albums)
    {

    }

    @Override
    public void onAlbumSongsLoad(ArrayList<AudioTrack> songs)
    {

    }
    
    @Override
    public void openPlayerScreen(AudioPlaylist playlist)
    {

    }

    @Override
    public void searchQueryResults(String searchQuery, ArrayList<AudioTrack> songs)
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
}
