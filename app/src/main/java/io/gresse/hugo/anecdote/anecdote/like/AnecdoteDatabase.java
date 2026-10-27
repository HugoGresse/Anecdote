package io.gresse.hugo.anecdote.anecdote.like;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

@Database(entities = {Anecdote.class}, version = 1)
public abstract class AnecdoteDatabase extends RoomDatabase {

    private static final String NAME = "anecdote.db";

    public abstract FavoritesDao favoritesDao();

    public static AnecdoteDatabase create(Context context) {
        // Favorites are read and written synchronously from EventBus subscribers on the main thread, as they were
        // with ObjectBox. The table only holds the user favorites, so these queries stay small.
        return Room.databaseBuilder(context, AnecdoteDatabase.class, NAME)
                .allowMainThreadQueries()
                .build();
    }
}
