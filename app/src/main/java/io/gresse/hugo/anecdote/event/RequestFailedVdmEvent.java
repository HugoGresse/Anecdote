package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

/**
 * A VDM failed event
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class RequestFailedVdmEvent extends RequestFailedEvent {
    public RequestFailedVdmEvent(String message, @Nullable Exception exception, int pageNumber) {
        super(message, exception, pageNumber);
    }
}
