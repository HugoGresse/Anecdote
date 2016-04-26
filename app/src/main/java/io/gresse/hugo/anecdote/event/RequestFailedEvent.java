package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

/**
 * When a request failed
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class RequestFailedEvent extends AnecdoteEvent {

    public String    message;
    @Nullable
    public Exception exception;

    public int pageNumber;

    public RequestFailedEvent(int websiteId, String message, @Nullable  Exception exception, int pageNumber) {
        super(websiteId);
        this.message = message;
        this.exception = exception;
        this.pageNumber = pageNumber;
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
