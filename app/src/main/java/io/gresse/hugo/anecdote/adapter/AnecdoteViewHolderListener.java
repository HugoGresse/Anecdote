package io.gresse.hugo.anecdote.adapter;

import android.view.View;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Listener for all {@link MixedContentAdapter}
 * <p/>
 * Created by Hugo Gresse on 17/02/16.
 */
public interface AnecdoteViewHolderListener {
    void onClick(Anecdote anecdote, View view);
    void onLongClick(Object object);
}
