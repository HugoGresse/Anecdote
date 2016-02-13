package io.gresse.hugo.anecdote.event;

/**
 * A base received something event
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class OnAnecdoteLoadedEvent {

    public int numberOfItemLoaded;

    public OnAnecdoteLoadedEvent(int numberOfItemLoaded) {
        this.numberOfItemLoaded = numberOfItemLoaded;
    }
}
