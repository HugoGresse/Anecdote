package io.gresse.hugo.anecdote.api.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a unique text provider such as VDM or DTC to be used to get and parse data from the website.
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class Website {

    public static final String SOURCE_LOCAL  = "local";
    public static final String SOURCE_REMOTE = "remote";
    public static final String SOURCE_CALCULATED = "calc";

    public int    version;
    public String slug;
    public String name;
    public int    color;
    public int    like;
    public String source;
    public String userAgent;

    public List<WebsitePage> pages;

    public Website() {
        this(null, SOURCE_LOCAL);
    }

    public Website(String name, String source) {
        this.pages = new ArrayList<>();
        this.source = source;
        this.name = name;
    }

    /**
     * Validate this object by preventing any crash when using it
     */
    public void validateData() {
        if (TextUtils.isEmpty(name)) {
            name = Long.toHexString(Double.doubleToLongBits(Math.random()));
        }

        for(WebsitePage websitePage : pages){
            websitePage.validate();
        }

        if (TextUtils.isEmpty(slug)) {
            if (source.equals(SOURCE_REMOTE)) {
                slug = "api-" + name;
            } else if (source.equals(SOURCE_CALCULATED)) {
                slug = "calc-" + name;
            } else {
                slug = "local-" + name;
            }
        }
    }

    /**
     * Check if the version missmatch between the two object
     *
     * @param website the new website
     * @return true if up to date, false otherweise
     */
    public boolean isUpToDate(Website website) {
        return website.version <= version;
    }

    /**
     * Check if the user can edit this website manually or not
     *
     * @return true if editable
     */
    public boolean isEditable() {
        return source.equals(SOURCE_LOCAL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Website website = (Website) o;

        if (!slug.equals(website.slug)) return false;
        return source.equals(website.source);

    }

    @Override
    public int hashCode() {
        int result = slug.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Website{" +
                "version=" + version +
                ", slug='" + slug + '\'' +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", like=" + like +
                ", source='" + source + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", pages=" + pages +
                '}';
    }
}
