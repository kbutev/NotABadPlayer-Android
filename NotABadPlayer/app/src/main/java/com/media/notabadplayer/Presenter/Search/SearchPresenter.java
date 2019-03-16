package com.media.notabadplayer.Presenter.Search;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SearchPresenter implements BasePresenter
{
    @NonNull private BaseView _view;
    @NonNull private AudioInfo _audioInfo;
    private ArrayList<AudioTrack> _searchResults = new ArrayList<>();
    
    public SearchPresenter(@NonNull BaseView view, @NonNull AudioInfo audioInfo)
    {
        _view = view;
        _audioInfo = audioInfo;
    }
    
    @Override
    public void start() 
    {
        
    }
    
    @Override
    public void onAlbumClick(int index) 
    {

    }
    
    @Override
    public void onAlbumsItemClick(int index) 
    {

    }
    
    @Override
    public void onSearchResultClick(int index)
    {
        AudioPlaylist playlist = new AudioPlaylist(_searchResults.get(index));
        
        _view.openPlayerScreen(playlist);
    }
    
    @Override
    public void onSearchQuery(String searchValue)
    {
        if (searchValue.isEmpty())
        {
            return;
        }
        
        final String searchQuery = searchValue.toLowerCase();
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                _searchResults = _audioInfo.searchForTracks(searchQuery);
                
                Handler mainHandler = new Handler(Looper.getMainLooper());
                
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        _view.searchQueryResults(searchQuery, _searchResults);
                    }
                };
                
                mainHandler.post(myRunnable);
            }
        });
        
        thread.start();
    }

    @Override
    public void onAppThemeChange(int themeValue)
    {
        
    }
    
    @Override
    public void onAppSortingChange(int value)
    {
        
    }
    
    @Override
    public void onKeybindSelected(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {
        
    }
}
