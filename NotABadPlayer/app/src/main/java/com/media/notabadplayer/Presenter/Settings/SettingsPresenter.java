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
    @NonNull private BaseView _applicationRootView;
    @NonNull private Context _context;

    public SettingsPresenter(@NonNull BaseView view, @NonNull BaseView applicationRootView, @NonNull Context context)
    {
        _view = view;
        _applicationRootView = applicationRootView;
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
    public void onAppThemeChange(int themeValue) {
        if (themeValue == GeneralStorage.getShared().getAppThemeValue(_context))
        {
            return;
        }
        
        GeneralStorage.getShared().saveAppThemeValue(_context, themeValue);
        
        _view.appThemeChanged();
        _applicationRootView.appThemeChanged();
    }
    
    @Override
    public void onAppSortingChange(int value)
    {
        _view.appSortingChanged();
        _applicationRootView.appSortingChanged();
    }
    
    @Override
    public void onKeybindSelected(ApplicationAction action, ApplicationInput input) 
    {
        GeneralStorage.getShared().saveSettingsAction(_context, input, action);
    }
}
