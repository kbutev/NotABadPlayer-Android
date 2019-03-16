package com.media.notabadplayer.View.Albums;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Presenter.Albums.AlbumPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    
    private GridView _table;
    private ListAdapter _tableAdapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        
        _table = root.findViewById(R.id.primaryArea);
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
    public void setPresenter(BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(AudioInfo audioInfo, String albumID, String albumArtist, String albumTitle, String albumCover) 
    {
        AlbumFragment f = AlbumFragment.newInstance();
        f.setPresenter(new AlbumPresenter(f, audioInfo, new AudioAlbum(albumID, albumArtist, albumTitle, albumCover)));
        
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, f).addToBackStack(AlbumsFragment.class.getCanonicalName()).commit();
    }
    
    @Override
    public void onMediaAlbumsLoad(ArrayList<AudioAlbum> albums)
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

    @Override
    public void appThemeChanged()
    {

    }

    @Override
    public void appSortingChanged()
    {

    }
}