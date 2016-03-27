package io.gresse.hugo.anecdote.model;

import android.support.annotation.Nullable;

import org.jsoup.Jsoup;

/**
 * A single anecdote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class Anecdote {

    public String      content;
    public String      permalink;
    @Nullable
    public RichContent mixedContent;

    public Anecdote(String content, String permalink) {
        this(content, permalink, null);
    }

    public Anecdote(String content, String permalink, @Nullable RichContent mixedContent) {
        this.content = content;
        this.permalink = permalink;
        this.mixedContent = mixedContent;
    }

    /**
     * Return the content without html
     *
     * @return plain text content
     */
    public String getPlainTextContent() {
        return Jsoup.parse(content.replace("<br>", "#lb#")).text().replace("#lb#", System.getProperty("line.separator"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Anecdote anecdote = (Anecdote) o;

        return content.equals(anecdote.content) && (permalink != null ?
                permalink.equals(anecdote.permalink) : anecdote.permalink == null);

    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + (permalink != null ? permalink.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Anecdote content='" + content + "'\', permalink='"+ permalink + '\'';
    }
}
