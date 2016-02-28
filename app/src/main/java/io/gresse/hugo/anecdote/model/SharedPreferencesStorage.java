package io.gresse.hugo.anecdote.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to store stuff in sharedPreferences
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class SharedPreferencesStorage {


    public static final String SP_KEY                 = "io.gresse.hugo.anecdote";
    public static final String SP_KEY_CONTENTPROVIDER = "contentProvider";

    /**
     * Get the list of user Website to be displayed in the app
     *
     * @param context app context
     * @return the lsit of website to be displayed
     */
    public static List<Website> getContentProvider(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);

        String settingString = sharedPreferences.getString(
                SP_KEY_CONTENTPROVIDER,
                generateDefaultContentProvider());

        Type listType = new TypeToken<ArrayList<Website>>() {
        }.getType();

        return new Gson().fromJson(settingString, listType);
    }

    public static String generateDefaultContentProvider(){
        List<Website> websites = new ArrayList<>();

        Website website = new Website(
                "Dans ton chat",
                "http://danstonchat.com/latest/",
                "http://danstonchat.com/latest/",
                "div > div > div > p > a",
                ".html",
                25,
                false
        );
        website.replaceMap.put("<span class=\"decoration\">", "<b>");
        website.replaceMap.put("</span>", "</b>");

        websites.add(website);

        website = new Website(
                "Vie de merde",
                "http://m.viedemerde.fr/?page=",
                "http://m.viedemerde.fr/",
                "ul.content > li",
                "",
                13,
                true
        );

        websites.add(website);
        return new Gson().toJson(websites);
    }

}
