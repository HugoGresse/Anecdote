package io.gresse.hugo.anecdote.anecdote.service.event;

import io.gresse.hugo.anecdote.event.Event;

/**
 * An anecdote related event
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public abstract class AnecdoteEvent implements Event {

    public String websitePageSlug;

    public AnecdoteEvent(String websitePageSlug) {
        this.websitePageSlug = websitePageSlug;
    }
}
