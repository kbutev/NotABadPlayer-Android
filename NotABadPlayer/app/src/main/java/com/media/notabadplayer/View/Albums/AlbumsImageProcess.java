package com.media.notabadplayer.View.Albums;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import static com.google.common.base.Preconditions.checkNotNull;

import com.media.notabadplayer.Audio.Model.AudioArtCover;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.ArtImageFetcher;

public class AlbumsImageProcess {
    private @NonNull Context _context;
    private @NonNull final View _target;
    private @NonNull final ImageView _cover;
    private @NonNull final AudioArtCover _artCover;
    private @Nullable final AlbumsImageProcessDelegate _delegate;

    private @NonNull final ArtImageFetcher _fetcher;
    
    private @NonNull final Runnable _runnable;
    
    private @Nullable Bitmap _bitmapResult;
    
    private Thread _thread;
    
    private @NonNull final Object _runningLock = new Object();
    private boolean _running = false;

    AlbumsImageProcess(@NonNull Context context, 
                       @NonNull View target, 
                       @NonNull AudioArtCover artCover,
                       @Nullable AlbumsImageProcessDelegate delegate) 
    {
        this._context = context;
        this._target = target;
        this._artCover = artCover;
        this._cover = checkNotNull((ImageView) target.findViewById(R.id.cover), "Base adapter is expecting a valid image view");
        this._delegate = delegate;
        this._fetcher = new ArtImageFetcher(_context);

        this._runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    _bitmapResult = _fetcher.fetch(_artCover);
                    finish();
                } 
                catch (Exception e)
                {
                    finish();
                }
            }
        };
    }
    
    public boolean isRunning()
    {
        synchronized (_runningLock)
        {
            return _running;
        }
    }
    
    public boolean isEqualTo(@NonNull AlbumsImageProcess process) 
    {
        return this._target == process._target;
    }

    public View getTarget()
    {
        return this._target;
    }
    
    public void start()
    {
        if (_thread != null)
        {
            throw new IllegalStateException("Calling start() twice on a AlbumsImageProcess");
        }
        
        _thread = new Thread(_runnable);

        _running = true;
        
        _thread.start();
    }
    
    public void stop()
    {
        if (isRunning())
        {
            synchronized (_runningLock)
            {
                _running = false;
                _thread.interrupt();
            }
        }
    }
    
    private void finish()
    {
        final AlbumsImageProcess process = this;
        
        Runnable updateOnMain = new Runnable() {
            @Override
            public void run() {
                if (process.isRunning())
                {
                    if (_bitmapResult != null)
                    {
                        _cover.setImageBitmap(_bitmapResult);
                    }
                }

                // Alert delegate
                if (_delegate != null)
                {
                    _delegate.onProcessFinish();
                }
            }
        };
        
        new Handler(Looper.getMainLooper()).post(updateOnMain);
    }
}