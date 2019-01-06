package com.media.notabadplayer.Presenter.Main;

import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class MainPresenter implements BasePresenter {
    private BaseView _view;
    
    public MainPresenter(BaseView view) {
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
}
