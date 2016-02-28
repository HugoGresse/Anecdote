package io.gresse.hugo.anecdote.service;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gresse.hugo.anecdote.model.Website;

/**
 * Provide different services
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class ServiceProvider {

    protected List<Website>                mWebsites;
    protected Map<String, AnecdoteService> mServices;

    public ServiceProvider(List<Website> websites) {
        mWebsites = websites;
        mServices = new HashMap<>();
    }

    public void register(Context context, Bus bus) {
        AnecdoteService service;

        for (Website website : mWebsites) {
            service = new AnecdoteService(context, website);
            bus.register(service);
            mServices.put(website.name, service);
        }
    }

    public void unregister(Bus bus) {
        for(Map.Entry<String, AnecdoteService> entry : mServices.entrySet()) {
            bus.unregister(entry.getValue());
        }
        mServices.clear();
    }

    public AnecdoteService getAnecdoteService(String serviceName){
        return mServices.get(serviceName);
    }

}
