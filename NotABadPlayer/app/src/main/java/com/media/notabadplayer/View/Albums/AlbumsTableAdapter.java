package com.media.notabadplayer.View.Albums;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.AlbumInfo;
import com.media.notabadplayer.R;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

class AlbumsTableAdapter extends BaseAdapter
{
    private Context _context;
    private ArrayList<AlbumInfo> _data;
    
    public AlbumsTableAdapter(@NonNull Context context, ArrayList<AlbumInfo> albums)
    {
        this._context = context;
        this._data = albums;
    }

    public int getCount()
    {
        return _data.size();
    }

    public Object getItem(int position)
    {
        return _data.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Init
        if (convertView == null)
        {
            LayoutInflater.from(_context).inflate(R.layout.item_table_album, parent, false);
        }
        
        // Item
        AlbumInfo item = (AlbumInfo) getItem(position);

        View listItem = LayoutInflater.from(_context).inflate(R.layout.item_table_album, parent, false);
        
        String dataTitle = item.albumTitle;
        String dataCover = item.albumCover;
        
        ImageView cover = checkNotNull((ImageView)listItem.findViewById(R.id.cover), "Base adapter is expecting a valid image view");
        
        if (dataCover != null)
        {
            cover.setImageURI(Uri.parse(dataCover));
        }
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view");
        
        if (dataTitle == null || dataTitle.length() == 0)
        {
            title.setText("Unknown");
        }
        else
        {
            title.setText(dataTitle);
        }
        
        return listItem;
    }
}
