package io.gresse.hugo.anecdote;

import android.app.Application;
import android.content.Context;

import io.gresse.hugo.anecdote.anecdote.like.AnecdoteDatabase;
import io.gresse.hugo.anecdote.anecdote.like.FavoritesDao;
import io.gresse.hugo.anecdote.anecdote.like.FavoritesRepository;
import io.gresse.hugo.anecdote.anecdote.social.SocialService;
import io.gresse.hugo.anecdote.legacy.LegacyFavoritesImport;
import io.gresse.hugo.anecdote.util.chrome.ChromeCustomTabsManager;

/**
 * Main application, also acting as the app-wide dependency container.
 *
 * Created by Hugo Gresse on 15/04/2017.
 */
public class AnecdoteApplication extends Application {

    private FavoritesRepository mFavoritesRepository;
    private ServiceProvider     mServiceProvider;

    public static AnecdoteApplication from(Context context) {
        return (AnecdoteApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FavoritesDao favoritesDao = AnecdoteDatabase.create(this).favoritesDao();
        mFavoritesRepository = new FavoritesRepository(favoritesDao);
        new Thread(() -> LegacyFavoritesImport.importInto(this, favoritesDao), "legacy-favorites-import").start();
        SocialService socialService = new SocialService(this, new ChromeCustomTabsManager(this));
        mServiceProvider = new ServiceProvider(socialService, mFavoritesRepository);
    }

    public FavoritesRepository getFavoritesRepository() {
        return mFavoritesRepository;
    }

    public ServiceProvider getServiceProvider() {
        return mServiceProvider;
    }
}
