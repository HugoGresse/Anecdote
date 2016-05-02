package io.gresse.hugo.anecdote.event;

/**
 * Toggle the fullscreen visibility
 * Created by Hugo Gresse on 24/04/16.
 */
public class ChangeFullscreenEvent implements Event {

    public boolean toFullscreen;

    public ChangeFullscreenEvent(boolean toFullscreen) {
        this.toFullscreen = toFullscreen;
    }
}
