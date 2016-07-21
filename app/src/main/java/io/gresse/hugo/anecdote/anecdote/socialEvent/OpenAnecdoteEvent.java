package io.gresse.hugo.anecdote.anecdote.socialEvent;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

/**
 * Launched to open the web page of the anecdote
 * <p/>
 * Created by Hugo Gresse on 20/07/16.
 */
public class OpenAnecdoteEvent extends SocialEvent {

    public final boolean  preloadOnly;

    public OpenAnecdoteEvent(String websiteName, Anecdote anecdote, boolean preloadOnly) {
        super(websiteName, anecdote);
        this.preloadOnly = preloadOnly;
    }
}
