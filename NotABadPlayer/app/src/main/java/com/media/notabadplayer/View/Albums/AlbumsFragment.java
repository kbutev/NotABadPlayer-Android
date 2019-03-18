package com.media.notabadplayer.View.Albums;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.Albums.AlbumPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    
    private GridView _table;
    private AlbumsTableAdapter _tableAdapter;
    private Parcelable _tableState;
    private GridSideIndexingView _tableSideSelector;
    
    public AlbumsFragment()
    {
        
    }
    
    public static AlbumsFragment newInstance()
    {
        return new AlbumsFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume()
    { 
        super.onResume();
        
        _presenter.start();
        
        if (_tableAdapter != null)
        {
            _table.setAdapter(_tableAdapter);
        }
        
        if (_tableState != null)
        {
            _table.onRestoreInstanceState(_tableState);
        }
    }

    @Override
    public void onPause() 
    {
        _tableState = _table.onSaveInstanceState();
        super.onPause();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        
        _table = root.findViewById(R.id.primaryAreaGrid);
        _tableSideSelector = root.findViewById(R.id.sideIndexing);
        
        initUI();
        
        return root;
    }
    
    private void initUI()
    {
        _table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                _presenter.onAlbumClick(position);
            }
        });
    }
    
    @Override
    public void setPresenter(@NonNull BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(@NonNull AudioAlbum album) 
    {
        AlbumFragment f = AlbumFragment.newInstance();
        f.setPresenter(new AlbumPresenter(f, album));
        
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, f).addToBackStack(AlbumsFragment.class.getCanonicalName()).commit();
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioPlaylist playlist)
    {

    }
    
    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums)
    {
        _tableAdapter = new AlbumsTableAdapter(getContext(), albums, _tableSideSelector);
        _table.setAdapter(_tableAdapter);
        
        ArrayList<String> titles = new ArrayList<>();
        
        for (int e = 0; e < albums.size(); e++)
        {
            titles.add(albums.get(e).albumTitle);
        }
        
        _tableSideSelector.updateAlphabet(titles);
        _tableSideSelector.setGridView(_table);
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
        _tableAdapter.sortAlbums(albumSorting);
        _table.invalidateViews();
        
        if (_tableState != null)
        {
            _table.onRestoreInstanceState(_tableState);
        }
    }

    @Override
    public void appAppearanceChanged(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }
}