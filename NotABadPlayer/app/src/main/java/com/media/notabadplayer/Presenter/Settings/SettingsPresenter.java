package com.media.notabadplayer.Presenter.Settings;

import android.content.Context;
import android.support.annotation.NonNull;

import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.Controls.ApplicationInput;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class SettingsPresenter implements BasePresenter 
{
    @NonNull private BaseView _view;
    @NonNull private Context _context;

    public SettingsPresenter(@NonNull BaseView view, @NonNull Context context)
    {
        _view = view;
        _context = context;
    }
    
    @Override
    public void start() {
        
    }

    @Override
    public void onAlbumClick(int index) {

    }

    @Override
    public void onAlbumsItemClick(int index) {

    }

    @Override
    public void onSearchResultClick(int index) {

    }

    @Override
    public void onSearchQuery(String searchValue) {

    }

    @Override
    public void onAppThemeChange() {

    }

    @Override
    public void onKeybindSelected(ApplicationAction action, ApplicationInput input) 
    {
        GeneralStorage.getShared().saveSettingsAction(_context, input, action);
    }
}
