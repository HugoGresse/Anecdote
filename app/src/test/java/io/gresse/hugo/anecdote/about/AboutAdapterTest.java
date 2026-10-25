package io.gresse.hugo.anecdote.about;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AboutAdapterTest {

    @Test
    public void urlStartIndex_findsTrailingUrlEvenWhenItContainsHttp() {
        String text = "OkHttp by Square https://square.github.io/okhttp/";

        assertEquals(text.indexOf("https"), AboutAdapter.urlStartIndex(text));
    }

    @Test
    public void urlStartIndex_supportsPlainHttp() {
        String text = "Jsoup by Jonathan Hedley http://jsoup.org/";

        assertEquals(text.indexOf("http://"), AboutAdapter.urlStartIndex(text));
    }

    @Test
    public void urlStartIndex_returnsMinusOneWithoutTrailingUrl() {
        assertEquals(-1, AboutAdapter.urlStartIndex("OkHttp by Square"));
    }
}
