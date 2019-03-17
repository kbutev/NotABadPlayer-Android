package com.media.notabadplayer.View.Lists;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.Presenter.Main.MainPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class ListsFragment extends Fragment implements BaseView {
    private MainPresenter _presenter;
    
    private Button _createPlaylistButton;
    private Button _deletePlaylistButton;
    
    public ListsFragment()
    {

    }

    public static ListsFragment newInstance()
    {
        return new ListsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlists, container, false);
        
        _createPlaylistButton = root.findViewWithTag(R.id.createPlaylistButton);
        _deletePlaylistButton = root.findViewWithTag(R.id.deletePlaylistButton);
        
        // Setup UI
        initUI();
        
        return root;
    }

    private void initUI()
    {
        _createPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
        
        _deletePlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {

    }

    @Override
    public void openAlbumScreen(@NonNull AudioAlbum album) {
        
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioPlaylist playlist) {

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
