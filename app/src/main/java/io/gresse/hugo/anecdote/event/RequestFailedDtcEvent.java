package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

/**
 * A DTC failed event
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class RequestFailedDtcEvent extends RequestFailedEvent {
    public RequestFailedDtcEvent(String message, @Nullable Exception exception) {
        super(message, exception);
    }
}
