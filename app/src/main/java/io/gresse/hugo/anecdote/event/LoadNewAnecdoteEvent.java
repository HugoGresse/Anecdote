package io.gresse.hugo.anecdote.event;

/**
 * An event when we need more anecdote
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class LoadNewAnecdoteEvent {

    public int start;
    public int count;

    public LoadNewAnecdoteEvent(int start, int count) {
        this.start = start;
        this.count = count;
    }
}
