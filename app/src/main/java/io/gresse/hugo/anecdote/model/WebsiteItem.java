package io.gresse.hugo.anecdote.model;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent an iterative item from a Website, like a content or url.
 *
 * Created by Hugo Gresse on 28/02/16.
 */
public class WebsiteItem {

    /**
     * The DOM selector to fetch the item
     */
    @Nullable
    public String selector;

    /**
     * The special attribute to get, default is the html content
     */
    @Nullable
    public String attribute;

    /**
     * The prefix to add to the item
     */
    @Nullable
    public String prefix;

    /**
     * The suffit to add to the item
     */
    @Nullable
    public String suffix;

    /**
     * A map of replacement to be done after fetching. The key is the string to replace and the value is the value to
     * replace the key from.
     */
    public Map<String, String> replaceMap;

    public WebsiteItem() {
        suffix = null;
        prefix = null;
        attribute = null;
        selector = null;
        replaceMap = new HashMap<>();
    }

    @Override
    public String toString() {
        return "WebsiteItem{" +
                "selector='" + selector + '\'' +
                ", attribute='" + attribute + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", replaceMap=" + replaceMap +
                '}';
    }
}
