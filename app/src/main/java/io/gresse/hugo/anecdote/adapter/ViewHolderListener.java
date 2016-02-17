package io.gresse.hugo.anecdote.adapter;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Listener for all viewHolder available callbacks
 * <p/>
 * Created by Hugo Gresse on 17/02/16.
 */
public interface ViewHolderListener {
    void onLongClick(Anecdote anecdote);
}
