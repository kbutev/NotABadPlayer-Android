package com.media.notabadplayer.View.Albums;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import static com.google.common.base.Preconditions.checkNotNull;

import com.media.notabadplayer.R;

public class AlbumsImageProcess {
    private @NonNull Context _context;
    private @NonNull final View _target;
    private @NonNull final ImageView _cover;
    private @NonNull final String _dataCover;
    private @Nullable final AlbumsImageProcessDelegate _delegate;
    
    private @NonNull final Runnable _runnable;
    
    private @Nullable Bitmap _bitmapResult;
    
    private Thread _thread;
    
    private @NonNull final Object _runningLock = new Object();
    private boolean _running = false;

    AlbumsImageProcess(@NonNull Context context, 
                       @NonNull View target, 
                       @NonNull String dataCover, 
                       @Nullable AlbumsImageProcessDelegate delegate) 
    {
        this._context = context;
        this._target = target;
        this._dataCover = "file://" + dataCover;
        this._cover = checkNotNull((ImageView) target.findViewById(R.id.cover), "Base adapter is expecting a valid image view");
        this._delegate = delegate;
        
        this._runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Uri uri = Uri.parse(Uri.decode(_dataCover));
                    _bitmapResult = MediaStore.Images.Media.getBitmap(_context.getContentResolver(), uri);
                    finish(true);
                } 
                catch (Exception e)
                {
                    finish(false);
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
    
    private void finish(boolean success)
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