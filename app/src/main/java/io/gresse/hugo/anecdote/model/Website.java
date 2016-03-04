package io.gresse.hugo.anecdote.model;

import android.text.TextUtils;

/**
 * Represent a unique content provider such as VDM or DTC to be used to get and parse data from the website.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class Website {

    // The website id should never be altered
    public int         id;
    public String      name;
    public String      url;
    public String      selector;
    public String      urlSuffix;
    public int         itemPerPage;
    public boolean     isFirstPageZero;
    public int         color;
    public int         like;
    public WebsiteItem contentItem;
    public WebsiteItem urlItem;

    public Website() {
        this.contentItem = new WebsiteItem();
        this.urlItem = new WebsiteItem();
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
    public void validateData(){
        if(TextUtils.isEmpty(name)){
            name = "";
        }
        if(TextUtils.isEmpty(url)){
            url = "";
        }
        if(TextUtils.isEmpty(selector)){
            selector = "";
        }
        if(TextUtils.isEmpty(urlSuffix)){
            urlSuffix = "";
        }
        if(itemPerPage <= 0){
            itemPerPage = 1;
        }
        if(contentItem == null){
            contentItem = new WebsiteItem();
        }
        if(urlItem == null){
            urlItem = new WebsiteItem();
        }
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

    @Override
    public String toString() {
        return "Website{" +
                "id=" + id +
                "\n, name='" + name + "'" +
                "\n, url='" + url +  "'" +
                "\n, selector='" + selector +  "'" +
                "\n, urlSuffix='" + urlSuffix +  "'" +
                "\n, itemPerPage=" + itemPerPage +
                "\n, isFirstPageZero=" + isFirstPageZero +
                "\n, color=" + color +
                "\n, like=" + like +
                "\n, contentItem=" + contentItem +
                "\n, urlItem=" + urlItem +
                '}';
    }
}
