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

    public String              name;
    public String              pageUrl;
    public String              detailUrl;
    public String              contentSelector;
    public String              pageSuffix;
    public int                 itemPerPage;
    public boolean             isFirstPageZero;

    // Advanced settings
    /**
     * If supplied (not null or empty), will query for this selector after selecting the parent using
     * {@link #contentSelector}
     */
    @Nullable
    public String              urlSelector;
    /**
     * This list of replacement that will be done on content before creating an Anecdote
     */
    public Map<String, String> replaceMap;

    public Website(String name,
                   String pageUrl,
                   String detailUrl,
                   String contentSelector,
                   String pageSuffix,
                   int itemPerPage,
                   boolean isFirstPageZero) {
        this.name = name;
        this.pageUrl = pageUrl;
        this.detailUrl = detailUrl;
        this.contentSelector = contentSelector;
        this.pageSuffix = pageSuffix;
        this.itemPerPage = itemPerPage;
        this.isFirstPageZero = isFirstPageZero;
        this.replaceMap = new HashMap<>();
    }
}
