package io.gresse.hugo.anecdote.service;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gresse.hugo.anecdote.model.api.Website;
import io.gresse.hugo.anecdote.model.api.WebsitePage;

/**
 * Provide different services (that explain a lot)
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class ServiceProvider {

    protected List<Website>                mWebsites;
    protected Map<String, AnecdoteService> mAnecdoteServices;
    protected WebsiteApiService            mWebsiteApiService;

    public ServiceProvider() {
        mAnecdoteServices = new HashMap<>();
        mWebsiteApiService = new WebsiteApiService();
    }

    public void createAnecdoteService(List<Website> websites) {
        mWebsites = websites;

        for (Website website : mWebsites) {
            for (WebsitePage websitePage : website.pages) {
                mAnecdoteServices.put(websitePage.slug, new AnecdoteService(website, websitePage));
            }
        }
    }

    public void register(EventBus eventBus) {
        for (Map.Entry<String, AnecdoteService> entry : mAnecdoteServices.entrySet()) {
            eventBus.register(entry.getValue());
        }
        eventBus.register(mWebsiteApiService);
    }

    public void unregister(EventBus eventBus) {
        for (Map.Entry<String, AnecdoteService> entry : mAnecdoteServices.entrySet()) {
            eventBus.unregister(entry.getValue());
        }
        mAnecdoteServices.clear();
        eventBus.unregister(mWebsiteApiService);
    }

    public AnecdoteService getAnecdoteService(String websitePageSlug) {
        return mAnecdoteServices.get(websitePageSlug);
    }

    public WebsiteApiService getWebsiteApiService() {
        return mWebsiteApiService;
    }
}
