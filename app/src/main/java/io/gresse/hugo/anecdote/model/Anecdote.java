package io.gresse.hugo.anecdote.model;

import android.support.annotation.Nullable;

import org.jsoup.Jsoup;

/**
 * A single anecdote, composed of:
 * - url
 * - text
 * - (Optional) image or video link
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class Anecdote {

    public String type;
    public String text;
    @Nullable
    public String permalink;
    @Nullable
    public String media;

    public Anecdote(String text, String permalink) {
        this(MediaType.TEXT, text, permalink, null);
    }

    public Anecdote(String type, String text, @Nullable String permalink, @Nullable String media) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Anecdote anecdote = (Anecdote) o;

        return text.equals(anecdote.text) && (permalink != null ?
                permalink.equals(anecdote.permalink) : anecdote.permalink == null);

    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + (permalink != null ? permalink.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Anecdote text='" + text + "'\', permalink='" + permalink + '\'';
    }
}
