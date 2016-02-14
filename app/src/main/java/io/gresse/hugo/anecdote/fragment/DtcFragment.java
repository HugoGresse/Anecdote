package io.gresse.hugo.anecdote.fragment;

import com.squareup.otto.Subscribe;

import io.gresse.hugo.anecdote.MainActivity;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteDtcEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedDtcEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.event.RequestFailedDtcEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.service.AnecdoteService;

/**
 * Display DTC quotes
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class DtcFragment extends AnecdoteFragment {

    /***************************
     * Implement super abstract methods
     ***************************/

    @Override
    protected AnecdoteService getService() {
        return ((MainActivity) getActivity()).getDtcService();
    }

    @Override
    protected void loadNewAnecdotes(int start) {
        BusProvider.getInstance().post(new LoadNewAnecdoteDtcEvent(start));
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onRequestFailedEvent(RequestFailedEvent event) {
        if(!(event instanceof RequestFailedDtcEvent)) return;
        afterRequestFinished(false);
    }

    @Subscribe
    public void  onAnecdoteReceived(OnAnecdoteLoadedEvent event){
        if(!(event instanceof OnAnecdoteLoadedDtcEvent)) return;
        afterRequestFinished(true);
    }
}
