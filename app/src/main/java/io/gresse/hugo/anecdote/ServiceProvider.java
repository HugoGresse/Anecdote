package io.gresse.hugo.anecdote;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gresse.hugo.anecdote.anecdote.like.FavoritesRepository;
import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.anecdote.service.OfflineAnecdoteService;
import io.gresse.hugo.anecdote.anecdote.service.OnlineAnecdoteService;
import io.gresse.hugo.anecdote.anecdote.social.SocialService;
import io.gresse.hugo.anecdote.api.WebsiteApiService;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;

/**
 * Provide different services (that explain a lot)
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class ServiceProvider {

    private final Map<String, AnecdoteService> mAnecdoteServices;
    private final WebsiteApiService            mWebsiteApiService;
    private final SocialService                mSocialService;
    private final FavoritesRepository          mFavoritesRepository;

    ServiceProvider(SocialService socialService, FavoritesRepository favoritesRepository) {
        mAnecdoteServices = new HashMap<>();
        mWebsiteApiService = new WebsiteApiService();
        mSocialService = socialService;
        mFavoritesRepository = favoritesRepository;
    }

    public void createAnecdoteService(Context context, List<Website> websites) {
        for (Website website : websites) {
            for (WebsitePage websitePage : website.pages) {
                mAnecdoteServices.put(
                        websitePage.slug,
                        new OnlineAnecdoteService(website, websitePage, mFavoritesRepository));
            }
        }

        // Add Favorites service
        Website favoritesWebsite = FavoritesRepository.getFavoritesWebsite(context);
        WebsitePage favoritesWebsitePage = favoritesWebsite.pages.get(0);
        mAnecdoteServices.put(
                favoritesWebsitePage.slug,
                new OfflineAnecdoteService(favoritesWebsite, favoritesWebsitePage, mFavoritesRepository));
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
}
