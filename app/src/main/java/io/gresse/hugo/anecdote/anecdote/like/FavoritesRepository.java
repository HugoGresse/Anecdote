package io.gresse.hugo.anecdote.anecdote.like;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote_;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;

/**
 * Provide access to the list of favorited anecdote.
 * <p>
 * Created by Hugo Gresse on 15/06/2017.
 */
public final class FavoritesRepository {

    public static final  int    OFFSET_QUERY = 50;

    private static Website sWebsite;

    private final Box<Anecdote>   mAnecdoteBox;
    private final Query<Anecdote> mAnecdoteManyQuery;
    private final Query<Anecdote> mAnecdoteSingleQuery;

    public FavoritesRepository(BoxStore boxStore) {
        mAnecdoteBox = boxStore.boxFor(Anecdote.class);
        mAnecdoteManyQuery = mAnecdoteBox
                .query()
                .orderDesc(Anecdote_.favoritesTimestamp)
                .build();

        mAnecdoteSingleQuery = mAnecdoteBox
                .query()
                .equal(Anecdote_.text, "", QueryBuilder.StringOrder.CASE_SENSITIVE)
                .equal(Anecdote_.websitePageSlug, "", QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build();
    }


    /**
     * Return the favorited Anecdote for the given page, starting at page 0 with {@link #OFFSET_QUERY} items per page
     * max.
     * @param page the page to get favorited anecdote on
     * @return the lsit of favorited anecdote, may be empty
     */
    public List<Anecdote> getFavorites(int page) {
        return mAnecdoteManyQuery.find(OFFSET_QUERY * page, OFFSET_QUERY);
    }

    /**
     * Check if the given Anecdote is in favorites or not.
     */
    @Nullable
    public Anecdote isFavorite(Anecdote anecdote) {
        mAnecdoteSingleQuery.setParameter(Anecdote_.websitePageSlug, anecdote.websitePageSlug);

        if(!TextUtils.isEmpty(anecdote.text)){
            mAnecdoteSingleQuery.setParameter(Anecdote_.text, anecdote.text);
        }
        
        return mAnecdoteSingleQuery.findFirst();
    }

    /**
     * Save a given Anecdote to the local storage
     *
     * @param websiteNamePageSlug the name of the website and of the page
     * @param anecdote            the anecdote to save
     */
    public Anecdote addFavorite(String websiteNamePageSlug, Anecdote anecdote) {
        // We clone the given anecdote so we do'nt mutate it and the adapter can make the diff properly
        Anecdote modifiedAnecdote;
        try {
            modifiedAnecdote = anecdote.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            modifiedAnecdote = anecdote;
        }
        modifiedAnecdote.favoritesTimestamp = System.currentTimeMillis() / 1000;
        mAnecdoteBox.put(modifiedAnecdote);
        return modifiedAnecdote;
    }

    /**
     * Remove a given Anecdote to the local storage
     *
     * @param anecdote the anecdote to save
     */
    public Anecdote removeFavorite(Anecdote anecdote) {
        // Stored Anecdote don't have the same id after app restart.
        Anecdote toRemoveAnecdote = isFavorite(anecdote);
        if(toRemoveAnecdote != null){
            mAnecdoteBox.remove(toRemoveAnecdote);
        }
        // Clone the anecdote and change the timestamp to remove the favorite on it
        Anecdote modifiedAnecdote;
        try {
            modifiedAnecdote = anecdote.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            modifiedAnecdote = anecdote;
        }
        modifiedAnecdote.favoritesTimestamp = 0L;
        return modifiedAnecdote;
    }

    /**
     * Get the static Website for Favorites. This allow the app to use the same architecture for Favorites while having
     * this website as an "offline" website.
     *
     * @param context app context
     * @return the website for Favorites
     */
    public static Website getFavoritesWebsite(Context context) {
        if (sWebsite == null) {
            String name = context.getString(R.string.action_favoris);
            sWebsite = new Website(context.getString(R.string.action_favoris), Website.SOURCE_CALCULATED);

            WebsitePage websitePage = new WebsitePage();
            websitePage.name = name;
            websitePage.slug = Website.SOURCE_CALCULATED + "-" + "favoris";

            sWebsite.pages.add(websitePage);

            sWebsite.validateData();
        }
        return sWebsite;
    }
}