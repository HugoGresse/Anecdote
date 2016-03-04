package io.gresse.hugo.anecdote.event;

/**
 * An anecdote related event
 *
 * Created by Hugo Gresse on 28/02/16.
 */
public abstract class AnecdoteEvent implements Event{

    public int websiteId;

    public AnecdoteEvent(int websiteId) {
        this.websiteId = websiteId;
    }
}
