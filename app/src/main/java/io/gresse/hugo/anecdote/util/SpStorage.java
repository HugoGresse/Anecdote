package io.gresse.hugo.anecdote.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.gresse.hugo.anecdote.model.Website;

/**
 * Utility class to store stuff in sharedPreferences
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class SpStorage {


    public static final String SP_KEY             = "io.gresse.hugo.anecdote";
    public static final String SP_KEY_FIRSTLAUNCH = "firstLaunch";
    public static final String SP_KEY_WEBSITES    = "websites";


    /**
     * Check if it's the first application launch or not
     *
     * @param context app context
     * @return true if first launch, false otherwise
     */
    public static boolean isFirstLaunch(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SP_KEY_FIRSTLAUNCH, true);
    }

    /**
     * Set first launch pref or the app
     *
     * @param context       app context
     * @param isFirstLaunch true if the app is in first launch, false otherweise
     */
    public static void setFirstLaunch(Context context, boolean isFirstLaunch) {
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean(SP_KEY_FIRSTLAUNCH, isFirstLaunch);
        sharedPreferencesEditor.apply();
    }

    /**
     * Get the list of user Website to be displayed in the app
     *
     * @param context app context
     * @return the list of website to be displayed
     */
    public static List<Website> getWebsites(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);

        String settingString = sharedPreferences.getString(SP_KEY_WEBSITES, "");

        if (TextUtils.isEmpty(settingString)) {
            return new ArrayList<>();
        }

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
        for(Website website : websites){
            website.validateData();
        }
        sharedPreferencesEditor.putString(SP_KEY_WEBSITES, new Gson().toJson(websites));
        sharedPreferencesEditor.apply();
    }

    /**
     * Save or add a unique website. If we create a new website, we need to be sure his id is not already on another
     * website.
     *
     * @param context app context
     * @param website website to save
     */
    public static void saveWebsite(Context context, Website website) {
        List<Website> websites = getWebsites(context);

        website.validateData();

        int maxId = 1;
        Website currentWebsite;
        for (int i = 0; i < websites.size(); i++) {
            currentWebsite = websites.get(i);
            if (currentWebsite.equals(website)) {
                websites.set(i, website);
                saveWebsites(context, websites);
                return;
            }
            if (currentWebsite.id >= maxId) {
                maxId = currentWebsite.id + 1;
            }
        }

        website.id = maxId;
        websites.add(website);
        saveWebsites(context, websites);
    }

    /**
     * Delete/remove given website for preferences
     *
     * @param context app context
     * @param website website to remove
     */
    public static void deleteWebsite(Context context, Website website) {
        List<Website> websites = getWebsites(context);

        for (Website websiteTemp : websites) {
            if (websiteTemp.equals(website)) {
                websites.remove(website);
                break;
            }
        }
        saveWebsites(context, websites);
    }

    /**
     * Set given website as default/first website on the list
     *
     * @param context app context
     * @param website website to set default on
     */
    public static void setDefaultWebsite(Context context, Website website) {
        List<Website> websites = getWebsites(context);

        int websiteToSwap = -1;
        for (int i = 0; i < websites.size(); i++) {
            if (websites.get(i).equals(website)) {
                websiteToSwap = i;
                break;
            }
        }
        Collections.swap(websites, 0, websiteToSwap);
        saveWebsites(context, websites);
    }


}
