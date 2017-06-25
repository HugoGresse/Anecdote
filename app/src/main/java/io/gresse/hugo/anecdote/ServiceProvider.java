package io.gresse.hugo.anecdote;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.gresse.hugo.anecdote.anecdote.like.FavoritesRepository;
import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.anecdote.service.OfflineAnecdoteService;
import io.gresse.hugo.anecdote.anecdote.service.OnlineAnecdoteService;
import io.gresse.hugo.anecdote.anecdote.social.SocialService;
import io.gresse.hugo.anecdote.api.WebsiteApiService;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import toothpick.Scope;
import toothpick.Toothpick;

/**
 * Provide different services (that explain a lot)
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
@Singleton
public class ServiceProvider {

    private   Map<String, AnecdoteService> mAnecdoteServices;
    private   WebsiteApiService            mWebsiteApiService;
    @Inject
    protected SocialService                mSocialService;

    ServiceProvider() {
        mAnecdoteServices = new HashMap<>();
        mWebsiteApiService = new WebsiteApiService();
    }

    public void createAnecdoteService(Context context, List<Website> websites) {
        Scope scope = Toothpick.openScope(context.getApplicationContext());

        OnlineAnecdoteService onlineAnecdoteService;

        for (Website website : websites) {
            for (WebsitePage websitePage : website.pages) {
                onlineAnecdoteService = new OnlineAnecdoteService(website, websitePage);
                Toothpick.inject(onlineAnecdoteService, scope);
                mAnecdoteServices.put(websitePage.slug, onlineAnecdoteService);
            }
        }

        // Add Favorites service
        Website favoritesWebsite = FavoritesRepository.getFavoritesWebsite(context);
        WebsitePage favoritesWebsitePage = favoritesWebsite.pages.get(0);
        OfflineAnecdoteService offlineAnecdoteService = new OfflineAnecdoteService(
                favoritesWebsite, favoritesWebsitePage);
        Toothpick.inject(offlineAnecdoteService, scope);
        mAnecdoteServices.put(favoritesWebsitePage.slug, offlineAnecdoteService);
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
