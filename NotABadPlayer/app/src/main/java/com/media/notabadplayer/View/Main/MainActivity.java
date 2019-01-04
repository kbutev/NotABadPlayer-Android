package com.media.notabadplayer.View.Main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.Presenter.Albums.AlbumsPresenter;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.Albums.AlbumsFragment;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Player.QuickPlayerFragment;
import com.media.notabadplayer.View.Playlists.PlaylistsFragment;
import com.media.notabadplayer.View.Search.SearchFragment;
import com.media.notabadplayer.View.Settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BaseView {
    private MediaInfo _mediaInfo;
    private MainPresenter _presenter;
    
    private BaseView _currentTab;
    
    private BaseView _quickPlayer;
    
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_albums:
                    _currentTab = new AlbumsFragment().newInstance();
                    _currentTab.setPresenter(new AlbumsPresenter(_currentTab, _mediaInfo));
                    refreshCurrentTab();
                    return true;
                case R.id.navigation_playlists:
                    _currentTab = new PlaylistsFragment().newInstance();
                    refreshCurrentTab();
                    return true;
                case R.id.navigation_search:
                    _currentTab = new SearchFragment().newInstance();
                    refreshCurrentTab();
                    return true;
                case R.id.navigation_settings:
                    _currentTab = new SettingsFragment().newInstance();
                    refreshCurrentTab();
                    return true;
            }
            return false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _mediaInfo = new MediaInfo(this);
        
        initUI();
        
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    
    private void initUI()
    {
        _presenter = new MainPresenter(this);
        _currentTab = AlbumsFragment.newInstance();
        _currentTab.setPresenter(new AlbumsPresenter(_currentTab, _mediaInfo));
        refreshCurrentTab();
        _presenter.start();
        
        _quickPlayer = QuickPlayerFragment.newInstance();
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.quickPlayer, (Fragment)_quickPlayer).commit();
    }
    
    private void refreshCurrentTab()
    {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.tabLayout, (Fragment)_currentTab).commit();
    }
    
    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.tabLayout);
        
        if (fragment != _currentTab)
        {
            super.onBackPressed();
        }
    }
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        
    }

    @Override
    public void openAlbumScreen(com.media.notabadplayer.Audio.MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover) {
        
    }

    @Override
    public void onMediaAlbumsLoad(ArrayList<com.media.notabadplayer.Audio.AlbumInfo> albums)
    {

    }

    @Override
    public void onAlbumSongsLoad(ArrayList<com.media.notabadplayer.Audio.AudioTrack> songs)
    {

    }

    @Override
    public void openPlayerScreen()
    {

    }

    @Override
    public void openPlayerScreen(com.media.notabadplayer.Audio.AudioTrack track)
    {

    }
}
