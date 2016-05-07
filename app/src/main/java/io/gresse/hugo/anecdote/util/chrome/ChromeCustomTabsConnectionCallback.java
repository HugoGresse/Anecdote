package io.gresse.hugo.anecdote.util.chrome;

import android.support.customtabs.CustomTabsClient;

/**
 * Callbacks when connection/disconnection Chrome custom tabs service.
 * <p/>
 * Created by Hugo Gresse on 06/05/16.
 */
public interface ChromeCustomTabsConnectionCallback {

    /**
     * Called when the service is connected.
     *
     * @param client a CustomTabsClient
     */
    void onServiceConnected(CustomTabsClient client);

    /**
     * Called when the service is disconnected.
     */
    void onServiceDisconnected();
}
