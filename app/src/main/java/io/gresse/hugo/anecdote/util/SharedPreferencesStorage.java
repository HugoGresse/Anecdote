package io.gresse.hugo.anecdote.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.model.Website;

/**
 * Utility class to store stuff in sharedPreferences
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class SharedPreferencesStorage {


    public static final String SP_KEY          = "io.gresse.hugo.anecdote";
    public static final String SP_KEY_WEBSITES = "contentProvider";

    /**
     * Get the list of user Website to be displayed in the app
     *
     * @param context app context
     * @return the lsit of website to be displayed
     */
    public static List<Website> getWebsites(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);

        String settingString = sharedPreferences.getString(
                SP_KEY_WEBSITES,
                generateDefaultWebsites());

        Type listType = new TypeToken<ArrayList<Website>>() {
        }.getType();

        return new Gson().fromJson(settingString, listType);
    }

    public static String generateDefaultWebsites() {
        List<Website> websites = new ArrayList<>();

        Website website = new Website(
                1,
                "Dans ton chat",
                "http://danstonchat.com/latest/",
                "div > div > div > p > a",
                ".html",
                25,
                false
        );

        website.contentItem.replaceMap.put("<span class=\"decoration\">", "<b>");
        website.contentItem.replaceMap.put("</span>", "</b>");
        website.urlItem.attribute = "href";

        websites.add(website);

        website = new Website(
                2,
                "Vie de merde",
                "http://m.viedemerde.fr/?page=",
                "ul.content > li",
                "",
                13,
                true
        );


        website.contentItem.selector = "p.text";
        website.urlItem.prefix = "http://m.viedemerde.fr/";
        website.urlItem.attribute = "id";
        website.urlItem.replaceMap.put("fml-", "");

        websites.add(website);
        return new Gson().toJson(websites);
    }

    /**
     * Update all saved websites config
     *
     * @param context  app context
     * @param websites list of websites to save
     */
    public static void saveWebsites(Context context, List<Website> websites) {
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString(SP_KEY_WEBSITES, new Gson().toJson(websites));
        sharedPreferencesEditor.apply();
    }

    /**
     * Save or add a unique website
     *
     * @param context app context
     * @param website website to save
     */
    public static void saveWebsite(Context context, Website website) {
        List<Website> websites = getWebsites(context);

        for (int i = 0; i < websites.size(); i++) {
            if (websites.get(i).equals(website)) {
                websites.set(i, website);
                saveWebsites(context, websites);
                return;
            }
        }

        websites.add(website);
        saveWebsites(context, websites);
    }


}
