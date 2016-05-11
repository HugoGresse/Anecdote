package io.gresse.hugo.anecdote.event;

/**
 * An event when we need more anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class LoadNewAnecdoteEvent extends AnecdoteEvent {

    public int page;

    public LoadNewAnecdoteEvent(int websiteId, int page) {
        super(websiteId);
        this.page = page;
    }
}
