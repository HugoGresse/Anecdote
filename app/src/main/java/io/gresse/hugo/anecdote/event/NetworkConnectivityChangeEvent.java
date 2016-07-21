package io.gresse.hugo.anecdote.event;

import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;

/**
 * When event connection change
 *
 * Created by Hugo Gresse on 25/02/16.
 */
public class NetworkConnectivityChangeEvent implements Event {

    public NetworkConnectivityListener.State state;

    public NetworkConnectivityChangeEvent(NetworkConnectivityListener.State state) {
        this.state = state;
    }
}
