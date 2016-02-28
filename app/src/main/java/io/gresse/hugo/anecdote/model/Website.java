package io.gresse.hugo.anecdote.model;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent a unique content provider such as VDM or DTC to be used to get and parse data from the website.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class Website {

    public String  name;
    public String  pageUrl;
    public String  itemSelector;
    public String  pageSuffix;
    public int     itemPerPage;
    public boolean isFirstPageZero;

    // Advanced settings

    /**
     * If supplied (not null or empty), will query for this selector after selecting the parent using
     * {@link #itemSelector} to get the final content element
     */
    @Nullable
    public String contentSelector;

    /**
     * The attribute to get content from, default is null so it take the inner text content to get it
     */
    @Nullable
    public String contentAttribute;

    /**
     * The detail url prefix to create the detail link from
     */
    @Nullable
    public String urlPrefix;

    /**
     * If supplied (not null or empty), will query for this selector after selecting the parent using
     * {@link #itemSelector} to get the final url content
     */
    @Nullable
    public String urlSelector;

    /**
     * The attribute to get url from, default is null to it will text to inner text content
     */
    @Nullable
    public String urlAttribute;

    /**
     * This list of replacement that will be done on content before creating an Anecdote
     */
    public Map<String, String> replaceContentMap;

    /**
     * This list of replacement that will be done on url before creating an Anecdote
     */
    public Map<String, String> replaceUrlMap;

    public Website(String name,
                   String pageUrl,
                   String itemSelector,
                   String pageSuffix,
                   int itemPerPage,
                   boolean isFirstPageZero) {
        this.name = name;
        this.pageUrl = pageUrl;
        this.itemSelector = itemSelector;
        this.pageSuffix = pageSuffix;
        this.itemPerPage = itemPerPage;
        this.isFirstPageZero = isFirstPageZero;
        this.replaceContentMap = new HashMap<>();
        this.replaceUrlMap = new HashMap<>();
    }
}
