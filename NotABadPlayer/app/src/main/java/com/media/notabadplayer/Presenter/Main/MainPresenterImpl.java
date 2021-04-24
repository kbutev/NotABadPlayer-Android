package com.media.notabadplayer.Presenter.Main;

import androidx.annotation.NonNull;
import android.util.Log;

import com.media.notabadplayer.Constants.AppState;
import com.media.notabadplayer.MVP.BaseRootView;

public class MainPresenterImpl implements MainPresenter {
    private BaseRootView _view;
    
    public MainPresenterImpl()
    {
        
    }

    // MainPresenter

    @Override
    public void start()
    {
        if (_view == null)
        {
            throw new IllegalStateException("MainPresenter: view has not been set");
        }

        Log.v(MainPresenterImpl.class.getCanonicalName(), "Start.");
    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public void setView(@NonNull BaseRootView view)
    {
        if (_view != null)
        {
            throw new IllegalStateException("MainPresenter: view has already been set");
        }

        _view = view;
    }

    @Override
    public void onAppStateChange(AppState state)
    {

    }
}
