package io.gresse.hugo.anecdote.event;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Launched to share an anecdote
 *
 * Created by Hugo Gresse on 20/07/16.
 */
public class ShareAnecdoteEvent extends SocialEvent {

    public ShareAnecdoteEvent(String websiteName, Anecdote anecdote) {
        super(websiteName, anecdote);
    }

}
