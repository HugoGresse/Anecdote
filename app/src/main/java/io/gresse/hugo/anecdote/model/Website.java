package io.gresse.hugo.anecdote.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;

/**
 * Represent a unique content provider such as VDM or DTC to be used to get and parse data from the website.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class Website {

    public static final String SOURCE_LOCAL  = "local";
    public static final String SOURCE_REMOTE = "remote";

    public static final int TYPE_NONE = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    // The website id should never be altered
    public int         id;
    public int         version;
    public String      name;
    public String      url;
    public String      selector;
    public String      urlSuffix;
    public int         itemPerPage;
    public boolean     isFirstPageZero;
    public int         color;
    public int         like;
    public String      source;
    public WebsiteItem contentItem;
    public WebsiteItem urlItem;
    @Nullable
    public WebsiteItem additionalMixedContentItem;

    /**
     * If a paginationItem is not null, so the first page to get if the root website (like http://9gag.com) and the
     * other page url are getted from the last page launch using this paginationItem.
     *
     * PaginationItem value if not getter after the first elements selections but at the same level.
     */
    @Nullable
    public WebsiteItem paginationItem;

    public Website() {
        this.contentItem = new WebsiteItem();
        this.urlItem = new WebsiteItem();
        this.source = SOURCE_LOCAL;
    }

    public Website(int id,
                   String name,
                   String url,
                   String selector,
                   String urlSuffix,
                   int itemPerPage,
                   boolean isFirstPageZero) {
        this();
        this.id = id;
        this.name = name;
        this.url = url;
        this.selector = selector;
        this.urlSuffix = urlSuffix;
        this.itemPerPage = itemPerPage;
        this.isFirstPageZero = isFirstPageZero;
    }

    /**
     * Validate this object by preventing any crash when using it
     */
    public void validateData() {
        if (TextUtils.isEmpty(name)) {
            name = "";
        }
        if (TextUtils.isEmpty(url)) {
            url = "";
        }
        if (TextUtils.isEmpty(selector)) {
            selector = "";
        }
        if (TextUtils.isEmpty(urlSuffix)) {
            urlSuffix = "";
        }
        if (itemPerPage <= 0) {
            itemPerPage = 1;
        }
        if (contentItem == null) {
            contentItem = new WebsiteItem();
        }
        if (urlItem == null) {
            urlItem = new WebsiteItem();
        }
    }

    /**
     * Get the page url from the given page number.
     *
     * @param pageNumber the page to get the url from
     * @param paginationMap the paginationMap is any, to try to get the url from
     * @return the url that represent the page
     */
    public String getPageUrl(int pageNumber, @Nullable Map<Integer, String> paginationMap){
        if(paginationItem == null){
            return url +
                    ((isFirstPageZero) ? pageNumber - 1 : pageNumber) +
                    urlSuffix;
        } else if(pageNumber != 0 && paginationMap != null && paginationMap.containsKey(pageNumber)) {
            return paginationMap.get(pageNumber);
        } else {
            return url;
        }
    }

    /**
     * Check if has additional mixed content
     *
     * @return true if has additional mixed (image or video) content
     */
    public boolean hasAdditionalContent(){
        return additionalMixedContentItem != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Website website = (Website) o;

        return id == website.id && source.equals(website.source);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Website{" +
                "id=" + id +
                "\n, name='" + name + "'" +
                "\n, url='" + url + "'" +
                "\n, selector='" + selector + "'" +
                "\n, urlSuffix='" + urlSuffix + "'" +
                "\n, itemPerPage=" + itemPerPage +
                "\n, isFirstPageZero=" + isFirstPageZero +
                "\n, color=" + color +
                "\n, like=" + like +
                "\n, contentItem=" + contentItem +
                "\n, urlItem=" + urlItem +
                '}';
    }
}
