package com.media.notabadplayer.Utilities;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

public class LooperService {
    public static int LOOPER_INTERVAL_MSEC = 100;

    private static LooperService singleton;

    private ClientsCollection _clients = new ClientsCollection();

    private Handler _handler;
    private Runnable _runnable;

    private LooperService()
    {
        _handler = new Handler(Looper.getMainLooper());
        _runnable = new Runnable() {
            @Override
            public void run() {
                loop();
                loopAgain();
            }
        };

        loopAgain();
    }

    synchronized public static LooperService getShared()
    {
        if (singleton == null)
        {
            singleton = new LooperService();
        }

        return singleton;
    }

    public void subscribe(final LooperClient client)
    {
        _clients.subscribe(client);
    }

    public void unsubscribe(final LooperClient client)
    {
        _clients.unsubscribe(client);
    }

    private void loop()
    {
        _clients.loop();
    }

    private void loopAgain()
    {
        _handler.postDelayed(_runnable, LOOPER_INTERVAL_MSEC);
    }

    class ClientsCollection {
        private ArrayList<LooperClient> _clients = new ArrayList<>();
        private final Object mutex = new Object();

        void subscribe(final LooperClient client)
        {
            synchronized (mutex)
            {
                if (!_clients.contains(client))
                {
                    _clients.add(client);
                }
            }
        }

        void unsubscribe(final LooperClient client)
        {
            synchronized (mutex)
            {
                _clients.remove(client);
            }
        }

        void loop()
        {
            synchronized (mutex)
            {
                for (LooperClient client : _clients)
                {
                    client.loop();
                }
            }
        }
    }
}
