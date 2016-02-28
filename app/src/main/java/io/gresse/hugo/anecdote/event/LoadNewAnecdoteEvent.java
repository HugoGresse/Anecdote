package io.gresse.hugo.anecdote.event;

/**
 * An event when we need more anecdote
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class LoadNewAnecdoteEvent extends AnecdoteEvent {

    public int start;
    public int count;

    public LoadNewAnecdoteEvent(String websiteName, int start) {
        this(websiteName, start, 0);
    }

    public LoadNewAnecdoteEvent(String websiteName, int start, int count) {
        super(websiteName);
        this.start = start;
        this.count = count;
    }
}
