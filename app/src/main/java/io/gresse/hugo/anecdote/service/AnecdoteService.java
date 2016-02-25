package io.gresse.hugo.anecdote.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.event.network.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.util.NetworkConnectivityListener;
import okhttp3.OkHttpClient;

/**
 * A base service for all that will load anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class AnecdoteService {

    protected Context        mContext;
    protected OkHttpClient   mOkHttpClient;
    protected List<Anecdote> mAnecdotes;
    protected List<Event>    mFailEvents;
    protected boolean mEnd = false;

    public AnecdoteService(Context context) {
        mContext = context;

        mOkHttpClient = new OkHttpClient();
        mAnecdotes = new ArrayList<>();
        mFailEvents = new ArrayList<>();
    }

    /**
     * Return the list of anecdotes already loaded by the service
     *
     * @return list of anecdote
     */
    public List<Anecdote> getAnecdotes() {
        return mAnecdotes;
    }

    /**
     * Remvoe all anecdotes
     */
    public void cleanAnecdotes() {
        mAnecdotes.clear();
    }

    /**
     * Called by child service
     *
     * @param connectivityEvent an event fired when the network connectivity change
     */
    public void onConnectivityChangeListener(NetworkConnectivityChangeEvent connectivityEvent) {
        if(connectivityEvent.state != NetworkConnectivityListener.State.CONNECTED){
            return;
        }

        if(!mFailEvents.isEmpty()){
            for(Event event : mFailEvents){
                BusProvider.getInstance().post(event);
            }
            mFailEvents.clear();
        }
    }

    /**
     * Download the given page and parse it
     *
     * @param event      original event
     * @param pageNumber the page to download
     */
    public abstract void downloadLatest(@NonNull Event event, int pageNumber);

    /**
     * Post an Event ot UI Thread
     *
     * @param event the event to post on Bus
     */
    protected void postOnUiThread(final Event event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getInstance().post(event);
            }
        });
    }
}
