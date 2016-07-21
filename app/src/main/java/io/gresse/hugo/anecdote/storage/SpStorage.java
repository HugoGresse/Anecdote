package io.gresse.hugo.anecdote.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.api.model.Content;
import io.gresse.hugo.anecdote.api.model.ContentItem;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;

/**
 * Utility class to store stuff in sharedPreferences
 * <p/>
 * Created by Hugo Gresse on 28/02/16.
 */
public class SpStorage {

    private static final String TAG = SpStorage.class.getSimpleName();

    public static final String SP_KEY                   = "io.gresse.hugo.anecdote.1";
    public static final String SP_KEY_VERSION           = "version";
    public static final String SP_KEY_FIRSTLAUNCH       = "firstLaunch";
    public static final String SP_KEY_WEBSITES          = "websites";
    public static final String SP_KEY_WEBSITE_REMOTE_NB = "websitesRemoteNumber";

    /**
     * Return the version code number
     * @param context app context
     * @param defaultVersion the default value if not any already saved
     * @return the current version
     */
    public static int getVersion(Context context, int defaultVersion) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(SP_KEY_VERSION, 0);
    }

    /**
     * Set the current sharedpref versions, matching app versionCode
     *
     * @param context app context
     * @param version the version to switch to
     */
    public static void setVersion(Context context, int version) {
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putInt(SP_KEY_VERSION, version);
        sharedPreferencesEditor.apply();
    }

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
     * Get the raw json string for the websites key
     * @param context app context
     * @return websites json
     */
    public static String getWebsitesString(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SP_KEY_WEBSITES, "");
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

    /**
     * Update all saved websites config, override alreayd saved ones
     *
     * @param context  app context
     * @param websites list of websites to save
     */
    public static void saveWebsites(Context context, List<Website> websites) {
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE).edit();
        for (Website website : websites) {
            website.validateData();
            for(WebsitePage websitePage : website.pages){
                websitePage.content.reorderItems();
            }
        }
        sharedPreferencesEditor.putString(SP_KEY_WEBSITES, new Gson().toJson(websites));
        sharedPreferencesEditor.apply();
    }

    /**
     * Save or add a unique website.
     *
     * @param context app context
     * @param website website to save
     */
    public static void saveWebsite(Context context, Website website) {
        List<Website> websites = getWebsites(context);

        website.validateData();
        for(WebsitePage websitePage : website.pages){
            websitePage.content.reorderItems();
        }

        Website currentWebsite;
        for (int i = 0; i < websites.size(); i++) {
            currentWebsite = websites.get(i);
            if (currentWebsite.equals(website)) {
                websites.set(i, website);
                saveWebsites(context, websites);
                return;
            }
        }

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

    /**
     * Get the number of websites last time checked
     * @param context app context
     * @return number of websites
     */
    public static int getSavedRemoteWebsiteNumber(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(SP_KEY_WEBSITE_REMOTE_NB, 0);
    }

    /**
     * Set saved remite website number
     *
     * @param context app context
     * @param number the number of remote website last time checked
     */
    public static void setSavedRemoteWebsiteNumber(Context context, int number){
        SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putInt(SP_KEY_WEBSITE_REMOTE_NB, number);
        sharedPreferencesEditor.apply();
    }

    /**
     * Migrate the current saved data to new formats
     *
     * @param context app context
     */
    public static boolean migrate(Context context) {
        boolean openWebsiteChooserReturn = false;
        switch (getVersion(context, 12)){
            case 0:
                List<Website> websites = getWebsites(context);
                /**
                 * v0.4.0 migration (type in WebsiteItem)
                 *
                 * the first/previous version is considered as 0 as version was implemented starting app v0.4.0
                 */
                if (websites != null) {
                    for (Website website : websites) {
                        SpStorage.deleteWebsite(context, website);
                    }
                }
                setVersion(context, 5); // 5 = 0.4.0
                Log.i(TAG, "Migrating from 0 > 5 done");
                break;
            case 5:
                /**
                 * v1.0.0 migration : major model change
                 */
                List<Website> migratedLocalWebsites = new ArrayList<>();

                // 1. Load all stored websites
                String websitesString = SpStorage.getWebsitesString(context);

                // 2. Clear all stored websites
                SharedPreferences.Editor sharedPreferencesEditor = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE).edit();
                sharedPreferencesEditor.putString(SP_KEY_WEBSITES, "[]");
                sharedPreferencesEditor.apply();

                // 3. Migrate local websites ONLY
                if(!TextUtils.isEmpty(websitesString)){
                    try {
                        JSONArray jsonObj = new JSONArray(websitesString);
                        
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject object = jsonObj.getJSONObject(i);

                            String source = object.getString("source");
                            if(TextUtils.isEmpty(source) || "remote".equals(source)){
                                // Skip remote websites. The user will need to reselect it
                                continue;
                            }

                            Website website = new Website();
                            WebsitePage websitePage = new WebsitePage();
                            websitePage.content = new Content();
                            websitePage.content.items.add(new ContentItem(MediaType.TEXT, 1));

                            website.color = object.getInt("color");
                            websitePage.isFirstPageZero = object.getBoolean("isFirstPageZero");
                            websitePage.isSinglePage = object.getBoolean("isSinglePage");
                            website.like = object.getInt("like");
                            website.name = object.getString("name");
                            websitePage.selector = object.getString("selector");
                            websitePage.name = website.name;
                            website.source = "local";
                            websitePage.url = object.getString("url");
                            websitePage.urlSuffix = object.getString("urlSuffix");

                            website.pages.add(websitePage);
                            website.validateData();

                            migratedLocalWebsites.add(website);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // 4. Save back local websites
                SpStorage.saveWebsites(context, migratedLocalWebsites);

                // 5. Change return value to help user reselect websites
                openWebsiteChooserReturn = true;

                // 6. Done, set new version
                setVersion(context, 12);
                // As the user will need to reselect websites, say to him why
                Toast.makeText(context, context.getString(R.string.app_migration_1_0_0), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Migrating from 5 > 12 done");
                break;
            default:
                // Nothing
                break;
        }
        return openWebsiteChooserReturn;
    }

}
