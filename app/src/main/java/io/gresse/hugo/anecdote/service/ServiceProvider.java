package io.gresse.hugo.anecdote.service;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gresse.hugo.anecdote.model.Website;

/**
 * Provide different services (that explain a lot)
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class ServiceProvider {

    protected List<Website>                 mWebsites;
    protected Map<Integer, AnecdoteService> mAnecdoteServices;
    protected WebsiteApiService mWebsiteApiService;

    public ServiceProvider(List<Website> websites) {
        mWebsites = websites;
        mAnecdoteServices = new HashMap<>();
        mWebsiteApiService = new WebsiteApiService();
    }

    public void register(Context context, Bus bus) {
        AnecdoteService service;

        for (Website website : mWebsites) {
            service = new AnecdoteService(website);
            bus.register(service);
            mAnecdoteServices.put(website.id, service);
        }

        bus.register(mWebsiteApiService);
    }

    public void unregister(Bus bus) {
        for(Map.Entry<Integer, AnecdoteService> entry : mAnecdoteServices.entrySet()) {
            bus.unregister(entry.getValue());
        }
        mAnecdoteServices.clear();
        bus.unregister(mWebsiteApiService);
    }

    public AnecdoteService getAnecdoteService(int websiteId){
        return mAnecdoteServices.get(websiteId);
    }

    public WebsiteApiService getWebsiteApiService() {
        return mWebsiteApiService;
    }
}
