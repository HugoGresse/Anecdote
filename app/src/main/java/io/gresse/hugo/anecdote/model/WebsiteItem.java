package io.gresse.hugo.anecdote.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent an iterative item from a Website, like a content or url.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class WebsiteItem {

    public static final int TYPE_STRING     = 0;
    public static final int TYPE_URL        = 1;
    public static final int TYPE_PAGINATION = 2;
    public static final int TYPE_IMAGE      = 3;
    public static final int TYPE_VIDEO      = 4;

    /**
     * The type of data among:
     * - {@link #TYPE_STRING}
     * - {@link #TYPE_URL}
     * - {@link #TYPE_PAGINATION}
     * - {@link #TYPE_IMAGE}
     * - {@link #TYPE_VIDEO}
     */
    public int type;

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
     * The suffix to add to the item
     */
    @Nullable
    public String suffix;

    /**
     * A map of replacement to be done after fetching. The key is the string to replace and the value is the value to
     * replace the key from.
     */
    public Map<String, String> replaceMap;

    /**
     * If the current websiteItem fail to be geted from given data, try to fallback to this
     */
    @Nullable
    public WebsiteItem fallbackItem;

    public WebsiteItem() {
        suffix = null;
        prefix = null;
        attribute = null;
        selector = null;
        replaceMap = new HashMap<>();
    }

    /**
     * Same as {@link #getData(Element, Element)} but return the value directly
     *
     * @param element Element to get data from
     * @return the formated data from the element
     */
    public String getData(Element element) {
        return getData(element, null);
    }

    /**
     * Get and format the data we want from the JSOUP Element.
     *
     * @param element     element to search data in
     * @param tempElement a tempElement to storage the wanted data
     */
    @SuppressWarnings("ParameterCanBeLocal")
    public String getData(Element element, @Nullable Element tempElement) {
        String data;
        if (!TextUtils.isEmpty(prefix)) {
            data = prefix;
        } else {
            data = "";
        }

        if (TextUtils.isEmpty(selector)) {
            tempElement = element;
        } else try {
            tempElement = element.select(selector).get(0);
        } catch (IndexOutOfBoundsException exception) {
            // No item, we try the fallback if any
            if(fallbackItem != null){
                return fallbackItem.getData(element, tempElement);
            }
            return data;
        }

        if (tempElement != null) {
            if (TextUtils.isEmpty(attribute)) {
                data += tempElement.html();
            } else {
                data += tempElement.attr(attribute);
            }
        }

        if (!TextUtils.isEmpty(suffix)) {
            data += suffix;
        }

        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            data = data.replaceAll(entry.getKey(), entry.getValue());
        }

        return data;
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
