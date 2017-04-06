package io.gresse.hugo.anecdote.anecdote.social;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.view.CustomImageView;

/**
 * Save a file and ask to share it
 *
 * Created by Hugo Gresse on 22/03/2017.
 */

public class SaveAndShareAnecdoteEvent extends SocialEvent {

    final CustomImageView customImageView;

    public SaveAndShareAnecdoteEvent(String websiteName, Anecdote anecdote, CustomImageView customImageView) {
        super(websiteName, anecdote);
        this.customImageView = customImageView;
    }

}
