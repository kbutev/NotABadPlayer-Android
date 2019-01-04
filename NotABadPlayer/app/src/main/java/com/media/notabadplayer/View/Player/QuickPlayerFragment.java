package com.media.notabadplayer.View.Player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.media.notabadplayer.Audio.AudioPlayerObserver;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class QuickPlayerFragment extends Fragment implements BaseView, AudioPlayerObserver {
    public QuickPlayerFragment()
    {

    }

    public static QuickPlayerFragment newInstance()
    {
        return new QuickPlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quick_player, container, false);

        // Setup UI

        return root;
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

    @Override
    public void onPlayerPlay(AudioTrack current)
    {

    }
  
    @Override
    public void onPlayerFinish()
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
