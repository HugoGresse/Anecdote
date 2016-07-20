package io.gresse.hugo.anecdote.event;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Launched to copy the anecdote content
 * <p/>
 * Created by Hugo Gresse on 20/07/16.
 */
public class CopyAnecdoteEvent extends SocialEvent {

    public CopyAnecdoteEvent(String websiteName, Anecdote anecdote) {
        super(websiteName, anecdote);
    }
}
