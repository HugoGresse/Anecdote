package io.gresse.hugo.anecdote;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.anecdote.social.SocialService;
import io.gresse.hugo.anecdote.api.WebsiteApiService;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;

/**
 * Provide different services (that explain a lot)
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
@Singleton
public class ServiceProvider {

    protected List<Website>                mWebsites;
    protected Map<String, AnecdoteService> mAnecdoteServices;
    protected WebsiteApiService            mWebsiteApiService;
    @Inject
    protected SocialService                mSocialService;

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
        eventBus.register(mSocialService);
        mSocialService.register();
    }

    public void unregister(EventBus eventBus) {
        for (Map.Entry<String, AnecdoteService> entry : mAnecdoteServices.entrySet()) {
            eventBus.unregister(entry.getValue());
        }
        mAnecdoteServices.clear();
        eventBus.unregister(mWebsiteApiService);
        mSocialService.unregister();
        eventBus.unregister(mSocialService);
    }

    public AnecdoteService getAnecdoteService(String websitePageSlug) {
        return mAnecdoteServices.get(websitePageSlug);
    }

    public WebsiteApiService getWebsiteApiService() {
        return mWebsiteApiService;
    }

    public SocialService getSocialService() {
        return mSocialService;
    }
}
