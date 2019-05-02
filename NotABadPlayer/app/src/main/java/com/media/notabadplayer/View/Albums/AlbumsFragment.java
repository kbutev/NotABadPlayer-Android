package com.media.notabadplayer.View.Albums;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Audio.AudioTrackSource;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.Playlist.PlaylistPresenter;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;
import com.media.notabadplayer.View.Playlist.PlaylistFragment;

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
    
    public static @NonNull AlbumsFragment newInstance()
    {
        return new AlbumsFragment();
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
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        _presenter.start();
    }

    @Override
    public void onStart()
    {
        super.onStart();

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
    public void onResume()
    {
        super.onResume();

        enableInteraction();
    }

    @Override
    public void onPause()
    {
        _tableState = _table.onSaveInstanceState();

        super.onPause();

        disableInteraction();
    }
    
    private void initUI()
    {
        _table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!_table.isClickable())
                {
                    return;
                }

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
    public void enableInteraction()
    {
        _table.setClickable(true);
    }

    @Override
    public void disableInteraction()
    {
        _table.setClickable(false);
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioAlbum album)
    {
        FragmentActivity a = getActivity();

        if (a == null)
        {
            return;
        }

        FragmentManager manager = a.getSupportFragmentManager();
        int backStackCount = manager.getBackStackEntryCount();
        
        String newEntryName = album.albumTitle;
        String lastEntryName = backStackCount > 0 ? manager.getBackStackEntryAt(backStackCount-1).getName() : "";

        // Do nothing, if the last entry name is equal to the new entry name
        if (lastEntryName != null && lastEntryName.equals(newEntryName))
        {
            return;
        }

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }
        
        PlaylistFragment f = PlaylistFragment.newInstance();
        f.setPresenter(new PlaylistPresenter(f, album));

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(0, R.anim.fade_in, 0, R.anim.hold);
        transaction.replace(R.id.mainLayout, f);
        transaction.addToBackStack(newEntryName).commit();
    }

    @Override
    public void openPlaylistScreen(@NonNull AudioPlaylist playlist)
    {
        FragmentActivity a = getActivity();

        if (a == null)
        {
            return;
        }

        FragmentManager manager = a.getSupportFragmentManager();
        int backStackCount = manager.getBackStackEntryCount();

        String newEntryName = playlist.getName();
        String lastEntryName = backStackCount > 0 ? manager.getBackStackEntryAt(backStackCount-1).getName() : "";

        // Do nothing, if the last entry name is equal to the new entry name
        if (lastEntryName != null && lastEntryName.equals(newEntryName))
        {
            return;
        }

        while (manager.getBackStackEntryCount() > 0)
        {
            manager.popBackStackImmediate();
        }
        
        PlaylistFragment f = PlaylistFragment.newInstance();
        f.setPresenter(new PlaylistPresenter(f, playlist));

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(0, R.anim.fade_in, 0, R.anim.hold);
        transaction.replace(R.id.mainLayout, f);
        transaction.addToBackStack(newEntryName).commit();
    }
    
    @Override
    public void onMediaAlbumsLoad(@NonNull ArrayList<AudioAlbum> albums)
    {
        Context context = getContext();

        if (context == null)
        {
            return;
        }

        _tableAdapter = new AlbumsTableAdapter(context, albums, _tableSideSelector);
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