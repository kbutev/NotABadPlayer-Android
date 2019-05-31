package com.media.notabadplayer.View.Player;

import java.util.ArrayList;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlayerNoiseSuppression;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Launch.LaunchActivity;
import com.media.notabadplayer.Presenter.PlayerPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AppThemeUtility;
import com.media.notabadplayer.Utilities.Serializing;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class PlayerActivity extends AppCompatActivity implements BaseView
{
    private BasePresenter _presenter;
    
    private BaseView _fragment;
    
    private AudioPlayerNoiseSuppression _noiseSuppression;
    
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

        // Audio model - retrieve from intent
        String intentData = getIntent().getStringExtra("playlist");
        AudioPlaylist playlist = (AudioPlaylist) Serializing.deserializeObject(intentData);

        if (playlist == null)
        {
            Log.v(PlayerActivity.class.getCanonicalName(), "Error: player cannot start with a null playlist.");

            finish();
            return;
        }
        
        // App theme
        AppThemeUtility.setTheme(this, GeneralStorage.getShared().getAppThemeValue());
        
        // Content
        setContentView(R.layout.activity_player);
        
        // UI
        initUI(playlist);
        
        // Noise suppression
        _noiseSuppression = new AudioPlayerNoiseSuppression();
        registerReceiver(_noiseSuppression, new IntentFilter(ACTION_AUDIO_BECOMING_NOISY));
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
            KeyBinds.getShared().evaluateInput(ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
            return true;
        }
        
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
        {
            KeyBinds.getShared().evaluateInput(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private void initUI(@NonNull AudioPlaylist playlist)
    {
        _presenter = new PlayerPresenter(playlist);
        _fragment = PlayerFragment.newInstance(_presenter);
        _presenter.setView(_fragment);
        
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.player, (Fragment)_fragment).commit();
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

    }

    @Override
    public void appThemeChanged(AppSettings.AppTheme appTheme)
    {

    }

    @Override
    public void appTrackSortingChanged(AppSettings.TrackSorting trackSorting)
    {

    }

    @Override
    public void onShowVolumeBarSettingChange(AppSettings.ShowVolumeBar value)
    {

    }

    @Override
    public void onPlayerErrorEncountered(@NonNull Exception error)
    {

    }
}
