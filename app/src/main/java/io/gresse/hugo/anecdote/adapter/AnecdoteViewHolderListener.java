package io.gresse.hugo.anecdote.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import io.gresse.hugo.anecdote.model.Anecdote;

/**
 * Listener for all {@link MixedContentAdapter}
 * <p/>
 * Created by Hugo Gresse on 17/02/16.
 */
public interface AnecdoteViewHolderListener {

    int ACTION_COPY                    = 1;
    int ACTION_SHARE                   = 2;
    int ACTION_OPEN_IN_BROWSER_PRELOAD = 3;
    int ACTION_OPEN_IN_BROWSER         = 4;
    int ACTION_FULLSCREEN              = 5;

    /**
     * When a item received click, follow it with the correct information
     *
     * @param anecdote the anecdote which was clicked
     * @param view     the view clicked
     * @param action   the action to do, like {@link #ACTION_COPY}
     */
    void onClick(Anecdote anecdote, @Nullable View view, int action);

    void onLongClick(Object object);
}
