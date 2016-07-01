package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

/**
 * Requested when we need ot change the Toolbar title
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class ChangeTitleEvent implements Event {

    @Nullable
    public String  title;
    @Nullable
    public String  className;
    @Nullable
    public String websiteSlug;

    public ChangeTitleEvent(@Nullable String title, @Nullable String className) {
        this.title = title;
        this.className = className;
    }

    /**
     * If it's a website (parent) related event
     *
     * @param websiteSlug the website (parent) displayed
     */
    public ChangeTitleEvent(@Nullable String websiteSlug) {
        this.websiteSlug = websiteSlug;
    }
}
