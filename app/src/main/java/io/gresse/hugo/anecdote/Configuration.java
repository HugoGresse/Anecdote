package io.gresse.hugo.anecdote;

/**
 * Store global application config
 * <p/>
 * Created by Hugo Gresse on 09/03/16.
 */
public class Configuration {

    public static final boolean DEBUG       = BuildConfig.DEBUG;
    public static final String  API_VERSION = "1";
    public static final String  API_URL     = "https://anecdote-api.firebaseio.com/v" + API_VERSION + "/websites.json";

}
