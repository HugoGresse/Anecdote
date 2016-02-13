package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

/**
 * When a request failed
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class RequestFailedEvent {

    public String message;
    @Nullable
    public Exception exception;

    public RequestFailedEvent(String message, @Nullable Exception exception) {
        this.message = message;
        this.exception = exception;
    }


}
