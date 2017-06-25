package io.gresse.hugo.anecdote.anecdote.service.event;

/**
 * A base received something event
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class OnAnecdoteLoadedEvent extends AnecdoteEvent {

    public int numberOfItemLoaded;

    public OnAnecdoteLoadedEvent(String websitePageSlug, int numberOfItemLoaded) {
        super(websitePageSlug);
        this.numberOfItemLoaded = numberOfItemLoaded;
    }
}
