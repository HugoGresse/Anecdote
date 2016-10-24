package io.gresse.hugo.anecdote.anecdote.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.Jsoup;

import io.gresse.hugo.anecdote.R;

/**
 * A single anecdote, composed of:
 * - url
 * - text
 * - (Optional) image or video link
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class Anecdote {

    @Nullable
    public String type;
    public String text;
    @Nullable
    public String permalink;
    @Nullable
    public String media;

    public Anecdote(String text, String permalink) {
        this(MediaType.TEXT, text, permalink, null);
    }

    public Anecdote(@Nullable String type, String text, @Nullable String permalink, @Nullable String media) {
        this.type = type;
        this.text = text;
        this.permalink = permalink;
        this.media = media;
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
