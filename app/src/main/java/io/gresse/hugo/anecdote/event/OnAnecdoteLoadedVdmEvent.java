package io.gresse.hugo.anecdote.event;

/**
 * When new VDM has been downloaded
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class OnAnecdoteLoadedVdmEvent extends OnAnecdoteLoadedEvent {

    public int page;

    public OnAnecdoteLoadedVdmEvent(int numberOfItemLoaded, int page) {
        super(numberOfItemLoaded);
        this.page = page;
    }
}
