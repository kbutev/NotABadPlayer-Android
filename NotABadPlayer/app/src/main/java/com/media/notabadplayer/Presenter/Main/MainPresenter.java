package com.media.notabadplayer.Presenter.Main;

import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class MainPresenter implements BasePresenter {
    private BaseView _view;
    
    public MainPresenter(BaseView view) 
    {
        _view = view;
    }
    
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

    }

    @Override
    public void onSearchQuery(String searchValue)
    {

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
