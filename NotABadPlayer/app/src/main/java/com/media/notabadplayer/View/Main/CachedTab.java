package com.media.notabadplayer.View.Main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.media.notabadplayer.Presenter.BasePresenter;
import com.media.notabadplayer.View.BaseView;

public class CachedTab {
    public final @NonNull BaseView tab;
    public final @NonNull BasePresenter presenter;
    public final @Nullable BaseView tabSubview;
    public final @Nullable String tabSubviewName;

    public CachedTab(@NonNull BaseView tab,
                     @NonNull BasePresenter presenter,
                     @Nullable BaseView tabSubview,
                     @Nullable String tabSubviewName)
    {
        this.tab = tab;
        this.presenter = presenter;
        this.tabSubview = tabSubview;
        this.tabSubviewName = tabSubviewName;
    }

    public static CachedTab create(@NonNull BaseView tab,
                                   @NonNull BasePresenter presenter,
                                   @NonNull FragmentManager manager)
    {
        BaseView tabSubview = null;
        String tabSubviewName = "";

        int entryCount = manager.getBackStackEntryCount();

        if (entryCount > 0)
        {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(0);

            Fragment fragment = manager.findFragmentByTag(entry.getName());

            if (fragment instanceof BaseView)
            {
                tabSubview = (BaseView)fragment;
                tabSubviewName = entry.getName();
            }
        }

        return new CachedTab(tab, presenter, tabSubview, tabSubviewName);
    }
}
