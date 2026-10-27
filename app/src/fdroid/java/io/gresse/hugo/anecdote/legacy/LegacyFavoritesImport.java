package io.gresse.hugo.anecdote.legacy;

import android.content.Context;

import io.gresse.hugo.anecdote.anecdote.like.FavoritesDao;

/**
 * F-Droid builds never shipped the ObjectBox favorites store, so there is nothing to import.
 */
public final class LegacyFavoritesImport {

    private LegacyFavoritesImport() {
    }

    public static void importInto(Context context, FavoritesDao favoritesDao) {
    }
}
