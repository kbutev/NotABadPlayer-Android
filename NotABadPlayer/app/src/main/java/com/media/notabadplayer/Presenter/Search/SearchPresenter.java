package com.media.notabadplayer.Presenter.Search;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.Constants.AppSettings;
import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

import java.util.ArrayList;

public class SearchPresenter implements BasePresenter
{
    @NonNull private BaseView _view;
    @NonNull private Context _context;
    @NonNull private AudioInfo _audioInfo;
    private ArrayList<AudioTrack> _searchResults = new ArrayList<>();
    
    public SearchPresenter(@NonNull BaseView view, @NonNull Context context, @NonNull AudioInfo audioInfo)
    {
        _view = view;
        _context = context;
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
    public void onPlayerButtonClick(ApplicationInput input)
    {

    }

    @Override
    public void onOpenPlaylistButtonClick()
    {

    }

    @Override
    public void onPlayOrderButtonClick()
    {

    }
    
    @Override
    public void onSearchResultClick(int index)
    {
        if (index < 0 || index >= _searchResults.size())
        {
            Log.v(SearchPresenter.class.getCanonicalName(), "Error: invalid clicked search index");
            return;
        }
        
        String playlistName = _context.getResources().getString(R.string.playlist_name_search_results);
        AudioPlaylist playlist = new AudioPlaylist(playlistName, _searchResults, _searchResults.get(index));
        
        _view.openPlayerScreen(playlist);
    }
    
    @Override
    public void onSearchQuery(@NonNull String searchValue)
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
    public void onAppSettingsReset() 
    {

    }

    @Override
    public void onAppThemeChange(AppSettings.AppTheme themeValue)
    {
        
    }
    
    @Override
    public void onAppSortingChange(AppSettings.AlbumSorting albumSorting, AppSettings.TrackSorting trackSorting)
    {
        
    }

    @Override
    public void onAppAppearanceChange(AppSettings.ShowStars showStars, AppSettings.ShowVolumeBar showVolumeBar)
    {

    }
    
    @Override
    public void onKeybindChange(com.media.notabadplayer.Controls.ApplicationAction action, com.media.notabadplayer.Controls.ApplicationInput input)
    {
        
    }
}
