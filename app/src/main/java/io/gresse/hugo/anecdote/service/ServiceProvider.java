package io.gresse.hugo.anecdote.service;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

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
    protected WebsiteApiService             mWebsiteApiService;

    public ServiceProvider() {
        mAnecdoteServices = new HashMap<>();
        mWebsiteApiService = new WebsiteApiService();
    }

    public void createAnecdoteService(List<Website> websites) {
        mWebsites = websites;

        for (Website website : mWebsites) {
            mAnecdoteServices.put(website.id, new AnecdoteService(website));
        }
    }

    public void register(Context context, EventBus eventBus) {
        for (Map.Entry<Integer, AnecdoteService> entry : mAnecdoteServices.entrySet()) {
            eventBus.register(entry.getValue());
        }
        eventBus.register(mWebsiteApiService);
    }

    public void unregister(EventBus eventBus) {
        for (Map.Entry<Integer, AnecdoteService> entry : mAnecdoteServices.entrySet()) {
            eventBus.unregister(entry.getValue());
        }
        mAnecdoteServices.clear();
        eventBus.unregister(mWebsiteApiService);
    }

    public AnecdoteService getAnecdoteService(int websiteId) {
        return mAnecdoteServices.get(websiteId);
    }

    public WebsiteApiService getWebsiteApiService() {
        return mWebsiteApiService;
    }
}
