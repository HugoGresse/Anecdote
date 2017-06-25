package io.gresse.hugo.anecdote.anecdote.service;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.gresse.hugo.anecdote.anecdote.like.FavoritesRepository;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.service.event.FavoritesEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.UpdateAnecdoteEvent;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.event.Event;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;

/**
 * Base for the service that supply anecdote content, either from offline or online.
 *
 * Created by Hugo Gresse on 15/06/2017.
 */

public abstract class AnecdoteService {

    protected String               mServiceName;
    protected Website              mWebsite;
    protected WebsitePage          mWebsitePage;
    protected List<Anecdote>       mAnecdotes;

    @Inject
    protected FavoritesRepository mFavoritesRepository;

    public AnecdoteService(Website website, WebsitePage websitePage) {
        mWebsite = website;
        mWebsitePage = websitePage;
        mServiceName = mWebsite.name.replaceAll("\\s", "") + websitePage.name + this.getClass().getSimpleName();

        mAnecdotes = new ArrayList<>();
    }

    /**
     * Get the Website object
     *
     * @return website object
     */
    public Website getWebsite() {
        return mWebsite;
    }
    /**
     * Get the Website page object
     *
     * @return website page object
     */
    public WebsitePage getWebsitePage() {
        return mWebsitePage;
    }

    /**
     * @return the full website page name which looks like eg. “9GAG Latest"
     */
    public String getPageFullName(){
        return mWebsite.name + " " + mWebsitePage.name;
    }

    /**
     * Return the list of anecdotes already loaded by the service
     *
     * @return list of anecdote
     */
    public List<Anecdote> getAnecdotes() {
        return mAnecdotes;
    }

    /**
     * Remvoe all anecdotes
     */
    public void cleanAnecdotes() {
        mAnecdotes.clear();
    }
    /**
     * Download the given page and parse it
     *
     * @param event      original event
     * @param pageNumber the page to download
     */
    protected abstract void downloadLatest(@NonNull final Event event, final int pageNumber);


    protected abstract void onFavoriteChange(boolean settedAsFavorite, Anecdote anecdote);

    /**
     * Post an Event ot UI Thread
     *
     * @param event the event to post on Bus
     */
    protected void postOnUiThread(final Event event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (event instanceof OnAnecdoteLoadedEvent || event instanceof RequestFailedEvent) {
                    EventBus.getDefault().postSticky(event);
                } else {
                    EventBus.getDefault().post(event);
                }
            }
        });
    }

    @Subscribe
    public void loadNextAnecdoteEvent(LoadNewAnecdoteEvent event) {
        if (!event.websitePageSlug.equals(mWebsitePage.slug)) return;
        int pageNumber = event.page;
        downloadLatest(event, pageNumber);
    }

    @Subscribe
    public void setFavoriteAnecdote(FavoritesEvent event){
        int i;
        Anecdote anecdote;
        for (i = 0; i < mAnecdotes.size(); i++) {
            anecdote = mAnecdotes.get(i);
            if(anecdote.equals(event.anecdote)){
                if(event.setAsFavorite){
                    onFavoriteChange(true, mFavoritesRepository.addFavorite(mWebsite.name + " " + mWebsitePage.name, anecdote));
                } else {
                    onFavoriteChange(false, mFavoritesRepository.removeFavorite(anecdote));
                }
                break;
            }
        }

        if (!event.websitePageSlug.equals(mWebsitePage.slug)) return;

        postOnUiThread(new UpdateAnecdoteEvent(mWebsitePage.slug, i));
    }

}
