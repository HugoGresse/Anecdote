package io.gresse.hugo.anecdote.fragment;

import android.util.Log;

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
public class DtcFragment extends QuoteFragment {

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
        Log.d("dtcfrag", "onRequestFailedEvent");
        if(!(event instanceof RequestFailedDtcEvent)) return;
        mIsLoadingNewItems = false;
    }

    @Subscribe
    public void  onAnecdoteReceived(OnAnecdoteLoadedEvent event){
        if(!(event instanceof OnAnecdoteLoadedDtcEvent)) return;


        Log.d("dtcfrag", "onAnecdoteReceived 2");
        mAdapter.setData(mAnecdoteService.getAnecdotes());
    }
}
