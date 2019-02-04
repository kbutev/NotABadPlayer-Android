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

import com.media.notabadplayer.Audio.AlbumInfo;
import com.media.notabadplayer.Audio.MediaInfo;
import com.media.notabadplayer.Presenter.Albums.AlbumPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements BaseView
{
    private BasePresenter _presenter;
    
    private GridView _table;
    private Parcelable _tableState;
    
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
        
        // Setup UI
        _table = root.findViewById(R.id.primaryArea);
        _table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                _presenter.onAlbumClick(position);
            }
        });
        
        return root;
    }
    
    @Override
    public void setPresenter(BasePresenter presenter)
    {
        _presenter = presenter;
    }

    @Override
    public void openAlbumScreen(MediaInfo mediaInfo, String albumID, String albumArtist, String albumTitle, String albumCover) 
    {
        AlbumFragment f = AlbumFragment.newInstance();
        f.setPresenter(new AlbumPresenter(f, mediaInfo, new AlbumInfo(albumID, albumArtist, albumTitle, albumCover)));
        
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, f).addToBackStack(AlbumsFragment.class.getCanonicalName()).commit();
    }
    
    @Override
    public void onMediaAlbumsLoad(ArrayList<com.media.notabadplayer.Audio.AlbumInfo> albums)
    {
        _table.setAdapter(new AlbumsTableAdapter(getContext(), albums));
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