package com.media.notabadplayer.View.Search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.Audio.Model.AudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.UIAnimations;

public class SearchListAdapter extends BaseAdapter
{
    private Context _context;
    private List<AudioTrack> _tracks;

    private HashSet<View> _listViews = new HashSet<>();

    private View _currentlySelectedView = null;
    private int _currentlySelectedViewListIndex = -1;

    public SearchListAdapter(@NonNull Context context, List<AudioTrack> tracks)
    {
        this._context = context;
        this._tracks = tracks;
    }
    
    public int getCount()
    {
        return _tracks.size();
    }

    public Object getItem(int position)
    {
        return _tracks.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) 
    {
        // Album track item
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_search_result, parent, false);
        }

        View listItem = convertView;

        // Views set update
        _listViews.add(convertView);
        
        // Item update
        AudioTrack item = (AudioTrack) getItem(position);
        
        ImageView cover = listItem.findViewById(R.id.albumCover);
        
        if (!item.artCover.isEmpty())
        {
            cover.setImageURI(Uri.parse(Uri.decode(item.artCover)));
        }
        else
        {
            cover.setImageDrawable(parent.getResources().getDrawable(R.drawable.cover_art_none));
        }
        
        TextView title = listItem.findViewById(R.id.title);
        title.setText(item.title);

        TextView albumTitle = listItem.findViewById(R.id.albumTitle);
        albumTitle.setText(item.albumTitle);

        TextView duration = listItem.findViewById(R.id.duration);
        duration.setText(item.duration);

        // Select playing track
        boolean isPlayingTrack = false;

        AudioPlaylist playlist = Player.getShared().getPlaylist();

        if (playlist != null)
        {
            AudioTrack track = playlist.getPlayingTrack();

            if (track.equals(item))
            {
                isPlayingTrack = true;
            }
        }

        Resources resources = parent.getResources();

        if (!isPlayingTrack)
        {
            listItem.setBackgroundColor(resources.getColor(R.color.transparent));
        }
        else
        {
            _currentlySelectedView = listItem;
            _currentlySelectedViewListIndex = position;
            _currentlySelectedView.setBackgroundColor(resources.getColor(R.color.currentlyPlayingTrack));
        }
        
        return listItem;
    }

    public boolean isItemSelectedForTrack(@NonNull AudioTrack track)
    {
        int position = -1;

        for (int e = 0; e < _tracks.size(); e++)
        {
            AudioTrack listTrack = _tracks.get(e);

            if (listTrack.equals(track))
            {
                position = e;
                break;
            }
        }

        if (position == -1)
        {
            return false;
        }

        return position == _currentlySelectedViewListIndex;
    }

    public void selectItem(@NonNull View view, int position)
    {
        deselectCurrentItem();

        _currentlySelectedView = view;
        _currentlySelectedViewListIndex = position;

        _currentlySelectedView.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
        UIAnimations.getShared().listItemAnimations.animateTap(_context, _currentlySelectedView);
    }

    public void deselectCurrentItem()
    {
        UIAnimations.getShared().listItemAnimations.endAll();

        Iterator<View> iterator = _listViews.iterator();

        while (iterator.hasNext())
        {
            View child = iterator.next();
            child.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
        }
    }
}
