package io.gresse.hugo.anecdote.model;

import org.jsoup.Jsoup;

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

    /**
     * Return the content without html
     *
     * @return plain text content
     */
    public String getPlainTextContent() {
        return Jsoup.parse(content.replace("<br>", "#lb#")).text().replace("#lb#", System.getProperty("line.separator"));
    }

    @Override
    public String toString() {
        return "DtcQuote{" +
                "content='" + content + '\'' +
                ", permalink='" + permalink + '\'' +
                '}';
    }
}
