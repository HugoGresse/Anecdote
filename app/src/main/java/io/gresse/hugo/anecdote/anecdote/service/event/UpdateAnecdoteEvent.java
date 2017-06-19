package io.gresse.hugo.anecdote.anecdote.service.event;

/**
 * When a given item has been updated, like setted a favorite.
 *
 * Created by Hugo Gresse on 17/06/2017.
 */

public class UpdateAnecdoteEvent extends AnecdoteEvent {

    public int itemChanged;

    public UpdateAnecdoteEvent(String websitePageSlug, int itemChange) {
        super(websitePageSlug);
        this.itemChanged = itemChange;
    }
}
