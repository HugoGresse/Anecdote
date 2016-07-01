package io.gresse.hugo.anecdote.event;

/**
 * An event when we need more anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class LoadNewAnecdoteEvent extends AnecdoteEvent {

    public int page;

    public LoadNewAnecdoteEvent(String websitePageSlug, int page) {
        super(websitePageSlug);
        this.page = page;
    }
}
