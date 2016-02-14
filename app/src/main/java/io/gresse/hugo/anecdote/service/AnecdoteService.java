package io.gresse.hugo.anecdote.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.model.dtc.Anecdote;
import okhttp3.OkHttpClient;

/**
 * A base service for all that will load anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class AnecdoteService {

    protected Context mContext;
    protected OkHttpClient mOkHttpClient;
    protected static List<Anecdote> mAnecdotes;
    protected boolean mEnd = false;

    public AnecdoteService(Context context) {
        mContext = context;

        mOkHttpClient = new OkHttpClient();
        mAnecdotes = new ArrayList<>();
    }

    /**
     * Return the list of anecdotes already loaded by the service
     *
     * @return list of anecdote
     */
    public List<Anecdote> getAnecdotes(){
        return mAnecdotes;
    }

    /**
     * Remvoe all anecdotes
     */
    public void cleanAnecdotes(){
        mAnecdotes.clear();
    }

    /**
     * Post an Event ot UI Thread
     *
     * @param event the event to post on Bus
     */
    protected void postOnUiThread(final Event event){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                BusProvider.getInstance().post(event);
            }
        });
    }
}
