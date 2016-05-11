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
    public Integer websiteId;

    public ChangeTitleEvent(@Nullable String title, @Nullable String className) {
        this.title = title;
        this.className = className;
    }

    /**
     * If it's a website related event
     *
     * @param websiteId the website displayed
     */
    public ChangeTitleEvent(@Nullable Integer websiteId) {
        this.websiteId = websiteId;
    }
}
