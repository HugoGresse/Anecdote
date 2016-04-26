package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

/**
 * When a request failed
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class RequestFailedEvent implements Event {

    public String    message;
    @Nullable
    public Exception exception;
    public Event     originalEvent;

    public RequestFailedEvent(Event originalEvent,
                              String message,
                              @Nullable Exception exception) {
        this.originalEvent = originalEvent;
        this.message = message;
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "RequestFailedEvent{" +
                "message='" + message + '\'' +
                ", exception=" + exception +
                ", pageNumber=" + pageNumber +
                '}';
    }
}
