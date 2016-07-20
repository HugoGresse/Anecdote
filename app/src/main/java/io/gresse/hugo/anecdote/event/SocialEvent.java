package io.gresse.hugo.anecdote.event;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Event related to social stuff
 *
 * Created by Hugo Gresse on 20/07/16.
 */
public class SocialEvent implements Event {

    public final Anecdote anecdote;
    public final String websiteName;

    public SocialEvent(String websiteName, Anecdote anecdote) {
        this.anecdote = anecdote;
        this.websiteName = websiteName;
    }
}
