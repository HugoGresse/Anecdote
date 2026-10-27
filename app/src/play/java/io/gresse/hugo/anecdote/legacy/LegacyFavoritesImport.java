package io.gresse.hugo.anecdote.legacy;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.anecdote.like.FavoritesDao;
import io.gresse.hugo.anecdote.legacy.objectbox.Anecdote;
import io.gresse.hugo.anecdote.legacy.objectbox.MyObjectBox;
import io.objectbox.BoxStore;
import io.objectbox.BoxStoreBuilder;

/**
 * Copy the favorites saved by ObjectBox (Play Store builds up to 1.3.0) into Room, then delete the ObjectBox store so
 * the import only ever runs once.
 */
public final class LegacyFavoritesImport {

    private static final String TAG = LegacyFavoritesImport.class.getSimpleName();

    private LegacyFavoritesImport() {
    }

    public static void importInto(Context context, FavoritesDao favoritesDao) {
        File objectBoxDirectory = new File(new File(context.getFilesDir(), BoxStoreBuilder.DEFAULT_NAME), BoxStoreBuilder.DEFAULT_NAME);
        if (!objectBoxDirectory.exists()) {
            return;
        }

        try {
            BoxStore boxStore = MyObjectBox.builder().androidContext(context).build();
            List<io.gresse.hugo.anecdote.anecdote.model.Anecdote> favorites = new ArrayList<>();
            for (Anecdote legacyAnecdote : boxStore.boxFor(Anecdote.class).getAll()) {
                favorites.add(legacyAnecdote.toFavorite());
            }
            favoritesDao.insertAll(favorites);
            boxStore.close();
            boxStore.deleteAllFiles();
            Log.i(TAG, "Imported " + favorites.size() + " favorites from ObjectBox");
        } catch (RuntimeException e) {
            Log.w(TAG, "Unable to import ObjectBox favorites, will retry on next start", e);
        }
    }
}
