package com.media.notabadplayer.Utilities;

import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;

// A unique collection of adapter views.
public class InternalAdapterViews {
    final ArrayList<InternalAdapterView> _views = new ArrayList<>();

    public Iterator<InternalAdapterView> iterator()
    {
        return _views.iterator();
    }

    public @NonNull InternalAdapterView add(@NonNull View view)
    {
        for (InternalAdapterView v : _views) {
            if (v.view == view) {
                return v;
            }
        }

        InternalAdapterView v = new InternalAdapterView(view);
        _views.add(v);
        return v;
    }
}
