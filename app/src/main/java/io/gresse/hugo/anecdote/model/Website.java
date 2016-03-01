package io.gresse.hugo.anecdote.model;

/**
 * Represent a unique content provider such as VDM or DTC to be used to get and parse data from the website.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class Website {

    // The website id should never be altered
    public int         id;
    public String      name;
    public String      pageUrl;
    public String      itemSelector;
    public String      pageSuffix;
    public WebsiteItem contentItem;
    public WebsiteItem urlItem;
    public int         itemPerPage;
    public boolean     isFirstPageZero;
    public int         color;

    public Website() {
        this.contentItem = new WebsiteItem();
        this.urlItem = new WebsiteItem();
    }

    public Website(int id,
                   String name,
                   String pageUrl,
                   String itemSelector,
                   String pageSuffix,
                   int itemPerPage,
                   boolean isFirstPageZero) {
        this();
        this.id = id;
        this.name = name;
        this.pageUrl = pageUrl;
        this.itemSelector = itemSelector;
        this.pageSuffix = pageSuffix;
        this.itemPerPage = itemPerPage;
        this.isFirstPageZero = isFirstPageZero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Website website = (Website) o;

        return id == website.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
