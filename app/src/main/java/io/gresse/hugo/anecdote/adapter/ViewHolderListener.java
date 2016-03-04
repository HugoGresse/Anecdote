package io.gresse.hugo.anecdote.adapter;

/**
 * Listener for all viewHolder available callbacks
 * <p/>
 * Created by Hugo Gresse on 17/02/16.
 */
public interface ViewHolderListener {
    void onClick(Object object);
    void onLongClick(Object object);
}
