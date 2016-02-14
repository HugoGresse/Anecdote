package io.gresse.hugo.anecdote;

import android.os.Build;

/**
 * Generals utils
 *
 * Created by Hugo Gresse on 13/02/16.
 */
public class Utils {

    /**
     * Return the user agent sent on all request. It's not default Android user agent as we don't really want that
     * websites see the app trafic (for now at least).
     *
     * Be only replace the device by the correct one.
     *
     * @return the user agent
     */
    public static String getUserAgent(){
        return "Mozilla/5.0 (Linux; Android 5.1; " + Build.MODEL +
                ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.95 Mobile Safari/537.36";
    }

}
