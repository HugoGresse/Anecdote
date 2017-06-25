package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;

import io.gresse.hugo.anecdote.util.TitledFragment;

/**
 * Requested when we need ot change the Toolbar title
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class ChangeTitleEvent implements Event {

    public String  title;
    @Nullable
    public String  additionalTitle;
    public boolean spinnerEnable;

    public ChangeTitleEvent(String title) {
        this.title = title;
        this.spinnerEnable = false;
    }

    public ChangeTitleEvent(TitledFragment fragment, String additionalSlug, boolean spinnerEnable) {
        this.title = fragment.getTitle();
        this.additionalTitle = additionalSlug;
        this.spinnerEnable = spinnerEnable;
    }
}
