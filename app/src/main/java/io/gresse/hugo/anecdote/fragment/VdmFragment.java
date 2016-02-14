package io.gresse.hugo.anecdote.fragment;

import com.squareup.otto.Subscribe;

import io.gresse.hugo.anecdote.MainActivity;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteVdmEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedVdmEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.RequestFailedVdmEvent;
import io.gresse.hugo.anecdote.service.AnecdoteService;

/**
 * Display VDM quotes
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class VdmFragment extends AnecdoteFragment {

    /***************************
     * Implement super abstract methods
     ***************************/

    @Override
    protected AnecdoteService getService() {
        return ((MainActivity) getActivity()).getVdmService();
    }

    @Override
    protected void loadNewAnecdotes(int start) {
        BusProvider.getInstance().post(new LoadNewAnecdoteVdmEvent(start));
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailedEvent(RequestFailedEvent event) {
        if(!(event instanceof RequestFailedVdmEvent)) return;
        afterRequestFinished(false);
    }

    @Subscribe
    public void  onAnecdoteReceived(OnAnecdoteLoadedEvent event){
        if(!(event instanceof OnAnecdoteLoadedVdmEvent)) return;
        afterRequestFinished(true);
    }
}
