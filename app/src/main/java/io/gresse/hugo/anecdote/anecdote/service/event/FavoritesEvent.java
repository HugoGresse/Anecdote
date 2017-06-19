package io.gresse.hugo.anecdote.anecdote.service.event;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

/**
 * Set a given anecodte as favorite, or remove it ¯\_(ツ)_/¯
 *
 * Created by Hugo Gresse on 17/06/2017.
 */

public class FavoritesEvent extends AnecdoteEvent {

    public Anecdote anecdote;
    public boolean setAsFavorite = true;

    public FavoritesEvent(String websitePageSlug, Anecdote anecdote, boolean setAsFavorite) {
        super(websitePageSlug);
        this.anecdote = anecdote;
        this.setAsFavorite = setAsFavorite;
    }
}
