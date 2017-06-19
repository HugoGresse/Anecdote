package io.gresse.hugo.anecdote.anecdote.service;

import android.support.annotation.NonNull;
import android.util.Log;

import javax.inject.Inject;

import io.gresse.hugo.anecdote.anecdote.like.FavoritesRepository;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.service.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.event.Event;

/**
 * Manage offline anecdote stored on the user device for later offline access like the favorites.
 * <p>
 * Created by Hugo Gresse on 16/06/2017.
 */
public class OfflineAnecdoteService extends AnecdoteService {

    private static final String TAG = OfflineAnecdoteService.class.getSimpleName();

    @Inject
    public FavoritesRepository mFavoritesRepository;


    public OfflineAnecdoteService(Website website, WebsitePage websitePage) {
        super(website, websitePage);
    }

    @Override
    public void downloadLatest(@NonNull Event event, int pageNumber) {
        Log.d(TAG, "downloadLatest page #" + pageNumber);
        mAnecdotes.addAll(mFavoritesRepository.getFavorites(pageNumber));
        postOnUiThread(new OnAnecdoteLoadedEvent(mWebsitePage.slug, mAnecdotes.size()));
    }

    @Override
    void onFavoriteChange(boolean settedAsFavorite, Anecdote anecdote) {
        if (!settedAsFavorite) {
            mAnecdotes.remove(anecdote);
        }
    }
}
