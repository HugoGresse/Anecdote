package io.gresse.hugo.anecdote.event;

/**
 * An event when we need more anecdote
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class LoadNewAnecdoteEvent extends AnecdoteEvent {

    public int start;
    public int count;

    public LoadNewAnecdoteEvent(int websiteId, int start) {
        this(websiteId, start, 0);
    }

    public LoadNewAnecdoteEvent(int websiteId, int start, int count) {
        super(websiteId);
        this.start = start;
        this.count = count;
    }
}
