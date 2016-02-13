package io.gresse.hugo.anecdote;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

/**
 * Created by Hugo Gresse on 13/02/16.
 */
public class Utils {

    /**
     * Preserve linebreaks with Jsoup
     * @param html enter html
     * @return html with correct linebreak
     */
    public static String br2nl(String html) {
        if (html == null) return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

}
