package io.gresse.hugo.anecdote.event;

/**
 * Request a specific permission
 *
 * Created by Hugo Gresse on 29/06/2017.
 */

public class RequestPermissionEvent implements Event {

    public String permission;
    public Event callbackEvent;

    public RequestPermissionEvent(String permission, Event callbackEvent) {
        this.permission = permission;
        this.callbackEvent = callbackEvent;
    }
}
