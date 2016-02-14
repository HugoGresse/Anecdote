package io.gresse.hugo.anecdote.fragment;

import android.os.Bundle;
import android.view.View;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mAnecdoteService.getAnecdotes().isEmpty()){
            BusProvider.getInstance().post(new LoadNewAnecdoteDtcEvent(0));
        }
    }

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
        mIsLoadingNewItems = false;
    }

    @Subscribe
    public void  onAnecdoteReceived(OnAnecdoteLoadedEvent event){
        if(!(event instanceof OnAnecdoteLoadedDtcEvent)) return;
        mIsLoadingNewItems = false;
        mAdapter.setData(mAnecdoteService.getAnecdotes());
    }
}
