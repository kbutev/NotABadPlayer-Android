package com.media.notabadplayer.View.Settings;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.media.notabadplayer.Controls.ApplicationAction;
import com.media.notabadplayer.R;

public class SettingsKeybindListAdapter extends BaseAdapter
{
    private final Context _context;

    public SettingsKeybindListAdapter(@NonNull Context context)
    {
        this._context = context;
    }
    
    public static int getCountForAction(ApplicationAction action)
    {
        for (int e = 0; e < ApplicationAction.values().length; e++)
        {
            if (ApplicationAction.values()[e] == action)
            {
                return e;
            }
        }
        
        return 0;
    }

    public int getCount()
    {
        return ApplicationAction.values().length;
    }

    public Object getItem(int position)
    {
        return ApplicationAction.values()[position].name();
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Item update
        ApplicationAction item = ApplicationAction.values()[position];
        
        View listItem = LayoutInflater.from(_context).inflate(R.layout.item_settings_option, parent, false);
        
        TextView title = listItem.findViewById(R.id.title);
        title.setText(item.name().replaceAll("_", " "));
        
        return listItem;
    }
}
