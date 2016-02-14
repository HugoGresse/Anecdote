package io.gresse.hugo.anecdote.event;

/**
 * We need more VDM quotes!
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class LoadNewAnecdoteVdmEvent extends LoadNewAnecdoteEvent {

    public LoadNewAnecdoteVdmEvent(int start) {
        super(start, 0);
    }

}
