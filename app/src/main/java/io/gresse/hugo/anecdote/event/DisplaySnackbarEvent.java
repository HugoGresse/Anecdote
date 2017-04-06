package io.gresse.hugo.anecdote.event;

import android.content.Intent;

/**
 * Event fired to display a toast
 *
 * Created by Hugo Gresse on 04/04/2017.
 */

public class DisplaySnackbarEvent {

    public String toastMessage;
    public String actionString;
    public Intent intentToRun;
    public int duration;

    public DisplaySnackbarEvent(String toastMessage, String actionString, Intent intentToRun, int duration) {
        this.toastMessage = toastMessage;
        this.actionString = actionString;
        this.intentToRun = intentToRun;
        this.duration = duration;
    }
}
