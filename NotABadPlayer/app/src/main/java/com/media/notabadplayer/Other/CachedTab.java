package com.media.notabadplayer.Other;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.media.notabadplayer.MVP.BasePresenter;
import com.media.notabadplayer.MVP.BaseRootView;
import com.media.notabadplayer.MVP.BaseView;

// Unites a view and a presenter together.
public class CachedTab {
    public final @NonNull BaseRootView tab;
    public final @NonNull BasePresenter presenter;
    public final @Nullable BaseView tabSubview;
    public final @Nullable String tabSubviewName;

    public CachedTab(@NonNull BaseRootView tab,
                     @NonNull BasePresenter presenter,
                     @Nullable BaseView tabSubview,
                     @Nullable String tabSubviewName)
    {
        this.tab = tab;
        this.presenter = presenter;
        this.tabSubview = tabSubview;
        this.tabSubviewName = tabSubviewName;
    }

    public static CachedTab create(@NonNull BaseRootView tab,
                                   @NonNull BasePresenter presenter,
                                   @NonNull FragmentManager manager)
    {
        BaseRootView tabSubview = null;
        String tabSubviewName = "";

        int entryCount = manager.getBackStackEntryCount();

        if (entryCount > 0)
        {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(0);

            Fragment fragment = manager.findFragmentByTag(entry.getName());

            if (fragment instanceof BaseRootView)
            {
                tabSubview = (BaseRootView)fragment;
                tabSubviewName = entry.getName();
            }
        }

        return new CachedTab(tab, presenter, tabSubview, tabSubviewName);
    }
}
