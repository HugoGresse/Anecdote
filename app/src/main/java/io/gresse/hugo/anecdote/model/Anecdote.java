package io.gresse.hugo.anecdote.model;

/**
 * A single DTC quote
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class Anecdote {

    public String content;
    public String permalink;

    public Anecdote(String content, String permalink) {
        this.content = content;
        this.permalink = permalink;
    }

    @Override
    public String toString() {
        return "DtcQuote{" +
                "content='" + content + '\'' +
                ", permalink='" + permalink + '\'' +
                '}';
    }
}
