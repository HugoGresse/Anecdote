package io.gresse.hugo.anecdote.event;

/**
 * Requested when we need ot change the Toolbar title
 *
 * Created by Hugo Gresse on 14/02/16.
 */
public class ChangeTitleEvent implements Event {

    public String title;
    public String className;

    public ChangeTitleEvent(String title, String className) {
        this.title = title;
        this.className = className;
    }
}
