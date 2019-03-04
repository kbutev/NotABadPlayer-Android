package com.media.notabadplayer.View.Main;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.Albums.AlbumsPresenter;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.Presenter.Search.SearchPresenter;
import com.media.notabadplayer.Presenter.Settings.SettingsPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.QuickPlayerFragment;
import com.media.notabadplayer.View.Playlists.PlaylistsFragment;
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
        
        setContentView(R.layout.activity_main);
        
        _presenter = new MainPresenter(this);
        
        _audioInfo = new AudioInfo(this);
        
        initUI();
        
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        
        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        registerReceiver(_noiseSuppression, new IntentFilter(ACTION_AUDIO_BECOMING_NOISY));
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
        _cachedTabs.put(R.id.navigation_albums, _currentTab);
        refreshCurrentTab();
    }

    private void selectPlaylistsTab()
    {
        _currentTabID = R.id.navigation_playlists;
        _currentTab = PlaylistsFragment.newInstance();
        _cachedTabs.put(R.id.navigation_playlists, _currentTab);
        refreshCurrentTab();
    }

    private void selectSearchTab()
    {
        _currentTabID = R.id.navigation_search;
        _currentTab = SearchFragment.newInstance();
        _currentTab.setPresenter(new SearchPresenter(_currentTab, _audioInfo));
        _cachedTabs.put(R.id.navigation_search, _currentTab);
        refreshCurrentTab();
    }

    private void selectSettingsTab()
    {
        _currentTabID = R.id.navigation_settings;
        _currentTab = SettingsFragment.newInstance();
        _currentTab.setPresenter(new SettingsPresenter(_currentTab, this));
        _cachedTabs.put(R.id.navigation_settings, _currentTab);
        refreshCurrentTab();
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
                return;
            case R.id.navigation_playlists:
                selectPlaylistsTab();
                return;
            case R.id.navigation_search:
                selectSearchTab();
                return;
            case R.id.navigation_settings:
                selectSettingsTab();
                return;
        }
        
        return;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainLayout);

            if (fragment != _currentTab)
            {
                super.onBackPressed();
                return true;
            }
        }

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
}
