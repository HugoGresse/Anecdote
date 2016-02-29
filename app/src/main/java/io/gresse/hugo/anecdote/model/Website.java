package io.gresse.hugo.anecdote.model;

/**
 * Represent a unique content provider such as VDM or DTC to be used to get and parse data from the website.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class Website {

    public String      name;
    public String      pageUrl;
    public String      itemSelector;
    public String      pageSuffix;
    public WebsiteItem contentItem;
    public WebsiteItem urlItem;
    public int         itemPerPage;
    public boolean     isFirstPageZero;
    public int         color;

    // Advanced settings


    public Website() {
    }

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
        this.contentItem = new WebsiteItem();
        this.urlItem = new WebsiteItem();
    }
}
