package com.media.notabadplayer.View.Albums;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import static com.google.common.base.Preconditions.checkNotNull;

import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.Other.GridSideIndexingView;

class AlbumsTableAdapter extends BaseAdapter implements SectionIndexer
{
    private final @NonNull Context _context;
    private final @NonNull List<AudioAlbum> _albums;
    private final @NonNull GridSideIndexingView _sideSelector;

    private final @NonNull AlbumsImageProcesses _imageSetterProcesses = new AlbumsImageProcesses();
    
    private Drawable _artCoverNoneDrawable = null;
    
    public AlbumsTableAdapter(@NonNull Context context, @NonNull List<AudioAlbum> albums, @NonNull GridSideIndexingView sideSelector)
    {
        this._context = context;
        this._albums = new ArrayList<>(albums);
        this._sideSelector = sideSelector;
    }
    
    public int getCount()
    {
        return _albums.size();
    }

    public Object getItem(int position)
    {
        return _albums.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_album, parent, false);
        }
        
        View listItem = convertView;
        
        // Item
        AudioAlbum item = (AudioAlbum) getItem(position);
        String dataTitle = item.albumTitle;
        String dataCover = item.albumCover;

        if (!dataCover.isEmpty())
        {
            AlbumsImageProcess p = new AlbumsImageProcess(_context, listItem, dataCover, _imageSetterProcesses);
            _imageSetterProcesses.add(p);
            p.start();
        }
        else
        {
            _imageSetterProcesses.removeProcessForView(listItem);
            
            if (_artCoverNoneDrawable == null)
            {
                _artCoverNoneDrawable = parent.getResources().getDrawable(R.drawable.cover_art_none);
            }
            
            ImageView cover = checkNotNull((ImageView)listItem.findViewById(R.id.cover), "Base adapter is expecting a valid image view");
            cover.setImageDrawable(_artCoverNoneDrawable);
        }
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view");
        
        if (dataTitle.length() == 0)
        {
            title.setText(R.string.albums_title_unknown);
        }
        else
        {
            title.setText(dataTitle);
        }
        
        return listItem;
    }
    
    @Override
    public Object[] getSections() 
    {
        ArrayList<Character> alphabet = _sideSelector.getAlphabet();
        
        String[] chars = new String[alphabet.size()];
        
        for (int i = 0; i < alphabet.size(); i++)
        {
            chars[i] = String.valueOf(alphabet.get(i));
        }
        
        return chars;
    }
    
    @Override
    public int getPositionForSection(int selectedIndex)
    {
        ArrayList<Character> alphabet = _sideSelector.getAlphabet();

        // Check for invalid index
        if (selectedIndex >= alphabet.size()) {
            return 0;
        }

        // Select the first album whose title first char matches the selected index char
        int position = 0;
        Character selectedCharacter = alphabet.get(selectedIndex);
        
        for (int e = 0; e < _albums.size(); e++)
        {
            if (_albums.get(e).albumTitle.charAt(0) == selectedCharacter)
            {
                position = e;
                break;
            }
        }
        
        return position;
    }
    
    @Override
    public int getSectionForPosition(int position) 
    {
        return 0;
    }

    private class AlbumsImageProcesses implements AlbumsImageProcessDelegate {
        ArrayList<AlbumsImageProcess> currentRunningProcesses = new ArrayList<>();

        private void add(@NonNull AlbumsImageProcess process)
        {
            AlbumsImageProcess alreadyRunning = findRunningProcessWithSameTarget(process);

            if (alreadyRunning != null)
            {
                remove(alreadyRunning);
            }
            
            currentRunningProcesses.add(process);
        }

        private void remove(@NonNull AlbumsImageProcess process)
        {
            process.stop();

            currentRunningProcesses.remove(process);
        }
        
        private void removeProcessForView(@NonNull View view)
        {
            for (int e = 0; e < currentRunningProcesses.size(); e++)
            {
                if (currentRunningProcesses.get(e).getTarget() == view)
                {
                    currentRunningProcesses.get(e).stop();
                    currentRunningProcesses.remove(e);
                    break;
                }
            }
        }

        private AlbumsImageProcess findRunningProcessWithSameTarget(@NonNull AlbumsImageProcess process)
        {
            for (AlbumsImageProcess p : currentRunningProcesses)
            {
                if (p.isEqualTo(process))
                {
                    return p;
                }
            }

            return null;
        }
        
        @Override
        public void onProcessFinish()
        {
            // Cleanup - remove non running process
            for (int e = 0; e < currentRunningProcesses.size(); e++)
            {
                if (!currentRunningProcesses.get(e).isRunning())
                {
                    currentRunningProcesses.remove(e);
                    break;
                }
            }
        }
    }
}
