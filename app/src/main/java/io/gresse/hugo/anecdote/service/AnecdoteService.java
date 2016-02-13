package io.gresse.hugo.anecdote.service;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.model.dtc.Anecdote;
import okhttp3.OkHttpClient;

/**
 * A base service for all that will load anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public abstract class AnecdoteService {

    protected Activity mActivity;
    protected OkHttpClient mOkHttpClient;
    protected List<Anecdote> mAnecdotes;
    protected boolean mEnd = false;

    public AnecdoteService(Activity activity) {
        mActivity = activity;

        mOkHttpClient = new OkHttpClient();
        mAnecdotes = new ArrayList<>();
    }

    public List<Anecdote> getAnecdotes(){
        return mAnecdotes;
    }
}
