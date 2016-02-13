package io.gresse.hugo.anecdote.event;

import com.squareup.otto.Bus;

/**
 * Siple singleton to return the static Bus
 * <p/>
 * Created by Hugo on 06/07/2015.
 */
public class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }

}
