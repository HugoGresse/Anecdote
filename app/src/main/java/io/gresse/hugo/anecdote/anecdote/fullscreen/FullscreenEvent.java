package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.support.v4.app.Fragment;
import android.view.View;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.event.Event;

/**
 * A click on an anecdote rich text request to open the text in fullscreen
 * <p/>
 * Created by Hugo Gresse on 21/04/16.
 */
public class FullscreenEvent implements Event {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;

    public int      type;
    public String   websiteName;
    public Fragment currentFragment;
    public View     transitionView;
    public String   transitionName;
    public Anecdote anecdote;

    public FullscreenEvent(int type,
                           String websiteName,
                           Fragment currentFragment,
                           View transitionView,
                           String transitionName,
                           Anecdote anecdote) {
        this.type = type;
        this.websiteName = websiteName;
        this.currentFragment = currentFragment;
        this.transitionView = transitionView;
        this.transitionName = transitionName;
        this.anecdote = anecdote;
    }
}
