package io.gresse.hugo.anecdote.event;

/**
 * When new DTC has been downloaded
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class OnAnecdoteLoadedDtcEvent extends OnAnecdoteLoadedEvent {

    public int page;

    public OnAnecdoteLoadedDtcEvent(int numberOfItemLoaded, int page) {
        super(numberOfItemLoaded);
        this.page = page;
    }
}
