package io.gresse.hugo.anecdote.util.chrome;

import android.content.ComponentName;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;

import java.lang.ref.WeakReference;

/**
 * Connection to Chrome custom tabs
 *
 * Created by Hugo Gresse on 06/05/16.
 */
public class ChromeCustomTabsConnection extends CustomTabsServiceConnection {

    // A weak reference to the ServiceConnectionCallback to avoid leaking it.
    private WeakReference<ChromeCustomTabsConnectionCallback> mConnectionCallback;

    public ChromeCustomTabsConnection(ChromeCustomTabsConnectionCallback connectionCallback) {
        mConnectionCallback = new WeakReference<>(connectionCallback);
    }

    @Override
    public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
        ChromeCustomTabsConnectionCallback connectionCallback = mConnectionCallback.get();
        if (connectionCallback != null) connectionCallback.onServiceConnected(client);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        ChromeCustomTabsConnectionCallback connectionCallback = mConnectionCallback.get();
        if (connectionCallback != null) connectionCallback.onServiceDisconnected();
    }
}
