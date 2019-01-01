package com.media.notabadplayer.View.Search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements BaseView
{
    private MainPresenter _presenter;

    public SearchFragment()
    {

    }

    public static SearchFragment newInstance()
    {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

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