package io.gresse.hugo.anecdote.event;

/**
 * A base received something event
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class OnAnecdoteLoadedEvent extends AnecdoteEvent {

    public int numberOfItemLoaded;
    public int page;

    public OnAnecdoteLoadedEvent(int websiteId, int numberOfItemLoaded, int page) {
        super(websiteId);
        this.numberOfItemLoaded = numberOfItemLoaded;
        this.page = page;
    }
}
