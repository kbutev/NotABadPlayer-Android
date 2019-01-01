package com.media.notabadplayer.View.Player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.media.notabadplayer.Audio.AlbumInfo;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.MediaInfo;
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
        setContentView(R.layout.activity_player);

        initUI();
        
        String trackFilePath = getIntent().getStringExtra("track.filePath");
        String trackTitle = getIntent().getStringExtra("track.title");
        String trackTrackNum = getIntent().getStringExtra("track.trackNum");
        String trackDur = getIntent().getStringExtra("track.duration");
        
        AudioTrack track = new AudioTrack(trackFilePath, trackTitle, trackTrackNum, trackDur);
        
        _presenter = new PlayerPresenter(_fragment, getApplicationContext(), track);
        _fragment.setPresenter(_presenter);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        _presenter.start();
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
    public void openAlbumScreen(MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover)
    {

    }
    
    @Override
    public void onMediaAlbumsLoad(ArrayList<AlbumInfo> albums)
    {

    }
    
    @Override
    public void onAlbumSongsLoad(ArrayList<AudioTrack> songs)
    {

    }

    @Override
    public void openPlayer()
    {

    }

    @Override
    public void startPlayer(com.media.notabadplayer.Audio.AudioTrack track)
    {

    }
    
    @Override
    public void onPlayerPlay(AudioTrack current) 
    {

    }
    
    @Override
    public void onPlayerStop()
    {

    }
    
    @Override
    public void onPlayerPause()
    {

    }
    
    @Override
    public void onPlayerResume()
    {

    }
    
    @Override
    public void onPlayerVolumeChanged() 
    {

    }
}
