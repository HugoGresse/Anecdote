package io.gresse.hugo.anecdote.service;

import android.content.Context;

import com.squareup.otto.Bus;

/**
 * Provide different services
 *
 * Created by Hugo Gresse on 14/02/16.
 */
public class ServiceProvider {



    protected DtcService mDtcService;

    public void register(Context context, Bus bus){
        mDtcService = new DtcService(context);

        bus.register(mDtcService);
    }

    public AnecdoteService getDtcService(){
        return mDtcService;
    }

}
