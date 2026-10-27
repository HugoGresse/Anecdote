package io.gresse.hugo.anecdote.anecdote.model;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.Jsoup;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import io.gresse.hugo.anecdote.R;

/**
 * A single anecdote, composed of:
 * - url
 * - text
 * - (Optional) image or video link
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
@Entity(indices = {
        @Index("type"),
        @Index("permalink"),
        @Index({"websitePageSlug", "text"})
})
public class Anecdote implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @Nullable
    public String type;
    public String text;
    @Nullable
    public String permalink;
    @Nullable
    public String media;
    public String websitePageSlug;
    public long favoritesTimestamp; // Favorites date, if not favorite: 0L

    @Ignore
    public Anecdote(String text, String permalink) {
        this(MediaType.TEXT, text, permalink, null);
    }

    @Ignore
    public Anecdote(@Nullable String type, String text, @Nullable String permalink, @Nullable String media) {
        this.type = type;
        this.text = text;
        this.permalink = permalink;
        this.media = media;
    }

    public Anecdote() {
    }

    /**
     * Return the text without html
     *
     * @return plain text text
     */
    public String getPlainTextContent() {
        return Jsoup.parse(text.replace("<br>", "#lb#")).text().replace("#lb#", System.getProperty("line.separator"));
    }

    /**
     * Get the share string of the
     * @param context app context
     * @return the shareable string description of this anecdote
     */
    public String getShareString(Context context){
        String copyString = getPlainTextContent();

        if(!TextUtils.isEmpty(permalink)){
            copyString += " " + permalink;
        }

        copyString += " " + context.getString(R.string.app_share_credits);

        return copyString;
    }

    /**
     * Return if the anecdote has been favorited by the user, in the current session (meaning it is not stored).
     */
    public boolean isFavorite(){
        return favoritesTimestamp != 0L;
    }

    @Override
    public Anecdote clone() throws CloneNotSupportedException {
        Object o = null;
        try {
            o = super.clone();
        } catch(CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
        return (Anecdote) o;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Anecdote anecdote = (Anecdote) o;

        if (type != null ? !type.equals(anecdote.type) : anecdote.type != null) return false;
        if (text != null ? !text.equals(anecdote.text) : anecdote.text != null) return false;
        //noinspection SimplifiableIfStatement
        if (permalink != null ? !permalink.equals(anecdote.permalink) : anecdote.permalink != null) return false;
        return media != null ? media.equals(anecdote.media) : anecdote.media == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (permalink != null ? permalink.hashCode() : 0);
        result = 31 * result + (media != null ? media.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Anecdote text='" + text + "'\', permalink='" + permalink + '\'';
    }
}
