package io.gresse.hugo.anecdote.event;

/**
 * We need more DTC quotes!
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class LoadNewAnecdoteDtcEvent extends LoadNewAnecdoteEvent {

    public LoadNewAnecdoteDtcEvent(int start) {
        super(start, 0);
    }

}
