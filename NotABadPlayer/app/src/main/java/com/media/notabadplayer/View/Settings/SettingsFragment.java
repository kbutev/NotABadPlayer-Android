package com.media.notabadplayer.View.Settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.media.notabadplayer.Audio.MediaAlbum;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SettingsFragment extends Fragment implements BaseView
{
    private MainPresenter _presenter;

    public SettingsFragment()
    {

    }

    public static SettingsFragment newInstance()
    {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Setup UI

        return root;
    }

    @Override
    public void setPresenter(BasePresenter presenter)
    {

    }

    @Override
    public void openAlbumScreen(MediaInfo mediaInfo, String albumID, String albumArtist, String albumTitle, String albumCover) {
        
    }

    @Override
    public void onMediaAlbumsLoad(ArrayList<MediaAlbum> albums)
    {

    }

    @Override
    public void onAlbumSongsLoad(ArrayList<com.media.notabadplayer.Audio.MediaTrack> songs)
    {

    }
    
    @Override
    public void openPlayerScreen(com.media.notabadplayer.Audio.MediaPlayerPlaylist playlist)
    {

    }
}