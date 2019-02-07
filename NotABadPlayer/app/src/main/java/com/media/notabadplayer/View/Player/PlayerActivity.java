package com.media.notabadplayer.View.Player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Controls.KeyBinds;
import com.media.notabadplayer.Presenter.Player.PlayerPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements BaseView
{
    private BasePresenter _presenter;
    
    private BaseView _fragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Content
        setContentView(R.layout.activity_player);
        
        // UI
        initUI();
        
        // Audio model - retrieve from intent
        ArrayList<String> tracksString = getIntent().getStringArrayListExtra("tracks");
        ArrayList<AudioTrack> tracks = new ArrayList<>();
        
        for (int e = 0; e < tracksString.size(); e++)
        {
            tracks.add(AudioTrack.createFromString(tracksString.get(e)));
        }
        
        String playingTrackString = getIntent().getStringExtra("playingTrack");
        AudioPlaylist playlist = new AudioPlaylist(tracks, AudioTrack.createFromString(playingTrackString));
        
        _presenter = new PlayerPresenter(_fragment, getApplication(), playlist);
        _fragment.setPresenter(_presenter);
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        // Transition animation
        overridePendingTransition(R.anim.slide_up, R.anim.hold);
    }

    @Override
    public void finish()
    {
        super.finish();

        // Transition animation
        overridePendingTransition(0, R.anim.slide_down);
    }
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
        {
            KeyBinds.getShared().respondToInput(ApplicationInput.PLAYER_VOLUME_UP_BUTTON);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
        {
            KeyBinds.getShared().respondToInput(ApplicationInput.PLAYER_VOLUME_DOWN_BUTTON);
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
    public void setPresenter(BasePresenter presenter)
    {
        
    }
    
    @Override
    public void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover)
    {

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
