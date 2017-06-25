package io.gresse.hugo.anecdote;

/**
 * Store global application config
 * <p/>
 * Created by Hugo Gresse on 09/03/16.
 */
public class Configuration {

    public static final boolean DEBUG                     = BuildConfig.DEBUG;
    public static final String  API_VERSION               = "2";
    public static final String  API_URL                   = "https://anecdote-api.firebaseio.com/v" + API_VERSION + "/websites.json";
    public static final String  DOWNLOAD_FOLDER           = "Anecdote";
    public static final int     IMAGE_SAVE_TOAST_DURATION = 5000;
    public static final String  DONATION_LINK             = "https://paypal.me/HugoGresse/5";

}
