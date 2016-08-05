package io.gresse.hugo.anecdote.anecdote.social;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

/**
 * Launched to copy the anecdote content
 * <p/>
 * Created by Hugo Gresse on 20/07/16.
 */
public class CopyAnecdoteEvent extends SocialEvent {

    public static final String TYPE_ANECDOTE = "Anecdote";
    public static final String TYPE_MEDIA    = "Media";

    public final String type;
    public final String shareString;

    public CopyAnecdoteEvent(String websiteName, Anecdote anecdote, String type, String shareString) {
        super(websiteName, anecdote);
        this.type = type;
        this.shareString = shareString;
    }
}
