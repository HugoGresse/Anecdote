package io.gresse.hugo.anecdote.event;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * A click on an anecdote rich content request to open the content in fullscreen
 * <p/>
 * Created by Hugo Gresse on 21/04/16.
 */
public class FullscreenEvent implements Event {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;

    public int      type;
    public Fragment currentFragment;
    public View     transitionView;
    public String   transitionName;
    public String   contentUrl;

    public FullscreenEvent(int type, Fragment currentFragment, View transitionView, String transitionName, @Nullable String contentUrl) {
        this.type = type;
        this.currentFragment = currentFragment;
        this.transitionView = transitionView;
        this.transitionName = transitionName;
        this.contentUrl = contentUrl;
    }
}
