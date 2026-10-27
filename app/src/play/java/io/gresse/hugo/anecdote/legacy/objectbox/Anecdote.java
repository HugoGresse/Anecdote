package io.gresse.hugo.anecdote.legacy.objectbox;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Favorites as stored by ObjectBox up to version 1.3.0. Only read once to import them into Room: keep the class name,
 * fields and indexes in sync with objectbox-models/default.json.
 */
@Entity
public class Anecdote {

    @Id
    public long id;
    @Index
    public String type;
    public String text;
    @Index
    public String permalink;
    public String media;
    @Index
    public String websitePageSlug;
    public long favoritesTimestamp;

    public io.gresse.hugo.anecdote.anecdote.model.Anecdote toFavorite() {
        io.gresse.hugo.anecdote.anecdote.model.Anecdote favorite =
                new io.gresse.hugo.anecdote.anecdote.model.Anecdote(type, text, permalink, media);
        favorite.websitePageSlug = websitePageSlug;
        favorite.favoritesTimestamp = favoritesTimestamp;
        return favorite;
    }
}
