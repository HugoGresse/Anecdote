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
    protected VdmService mVdmService;

    public void register(Context context, Bus bus){
        mDtcService = new DtcService(context);
        mVdmService = new VdmService(context);

        bus.register(mDtcService);
        bus.register(mVdmService);
    }

    public AnecdoteService getDtcService(){
        return mDtcService;
    }

    public AnecdoteService getVdmService(){
        return mVdmService;
    }

}
