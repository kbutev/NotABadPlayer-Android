package com.media.notabadplayer.View.Player;

import java.util.ArrayList;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.Player.PlayerPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AppThemeSetter;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

public class PlayerActivity extends AppCompatActivity implements BaseView
{
    private BasePresenter _presenter;
    
    private BaseView _fragment;
    
    private AudioPlayerNoiseSuppression _noiseSuppression;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // App theme
        AppThemeSetter.setTheme(this, GeneralStorage.getShared().getAppThemeValue(this));
        
        // Content
        setContentView(R.layout.activity_player);
        
        // UI
        initUI();
        
        // Audio model - retrieve from intent
        String intentData = getIntent().getStringExtra("playlist");
        AudioPlaylist playlist = (AudioPlaylist) Serializing.deserializeObject(intentData);
        
        // Presenter
        _presenter = new PlayerPresenter(_fragment, playlist);
        _fragment.setPresenter(_presenter);
        
        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        registerReceiver(_noiseSuppression, new IntentFilter(ACTION_AUDIO_BECOMING_NOISY));
    }
    
    @Override
    protected void onDestroy()
    {
        unregisterReceiver(_noiseSuppression);
        super.onDestroy();
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        // Transition animation
        overridePendingTransition(R.anim.player_slide_up, R.anim.hold);
    }

    @Override
    public void finish()
    {
        super.finish();

        // Transition animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            View view = findViewById(android.R.id.content);
            
            if (view != null)
            {
                Drawable background = view.getBackground();

                if (background instanceof ColorDrawable)
                {
                    window.setStatusBarColor(((ColorDrawable) background).getColor());
                } 
            }
        }
        
        overridePendingTransition(0, R.anim.player_slide_down);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
        {
            KeyBinds.getShared().evaluateInput(this, ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
            return true;
        }
        
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
        {
            KeyBinds.getShared().evaluateInput(this, ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private void initUI()
    {
        _fragment = PlayerFragment.newInstance();
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.player, (Fragment)_fragment).commit();
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        
    }
    
    @Override
    public void openPlaylistScreen(@NonNull AudioAlbum album)
    {

    }

    @Override
    public void openPlaylistScreen(@NonNull AudioPlaylist playlist)
    {

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
    public void onPlaylistLoad(@NonNull AudioPlaylist playlist, boolean sortTracks)
    {

    }
    
    @Override
    public void openPlayerScreen(@NonNull AudioPlaylist playlist)
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

    }

    @Override
    public void appSortingChanged(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }
}
