package io.gresse.hugo.anecdote.anecdote.like;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

@Dao
public interface FavoritesDao {

    @Query("SELECT * FROM Anecdote ORDER BY favoritesTimestamp DESC LIMIT :limit OFFSET :offset")
    List<Anecdote> getFavorites(int offset, int limit);

    @Nullable
    @Query("SELECT * FROM Anecdote WHERE websitePageSlug IS :websitePageSlug AND text IS :text LIMIT 1")
    Anecdote find(String websitePageSlug, String text);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Anecdote anecdote);

    @Insert
    void insertAll(List<Anecdote> anecdotes);

    @Delete
    void delete(Anecdote anecdote);
}
