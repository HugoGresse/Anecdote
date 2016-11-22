package io.gresse.hugo.anecdote.tracking;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;

/**
 * Main entry point for tracking application activity. Sub flavors are responsable to implement
 * {@link EventSenderInterface} with a class named "EventSender" and send the proper information to each services they
 * manage.
 * <p/>
 * Created by Hugo Gresse on 25/04/16.
 */
public class EventTracker {

    public static final String CONTENT_TYPE_ANECDOTE = "Anecdote";
    public static final String CONTENT_TYPE_APP = "App";
    public static final String TRACKING_WEBSITE_NAME = "Website name";

    private static EventSenderInterface sEvent;

    public EventTracker(Context context) {
        if (!isEventEnable()) return;

        sEvent = new EventSender(context);
    }

    /**
     * Return true if event reporting is enable, checking the BuildConfig
     *
     * @return true if enable, false otherweise
     */
    public static boolean isEventEnable() {
        return !Configuration.DEBUG;
    }

    /**
     * Called by activities onStart
     */
    public static void onStart(Activity activity){
        if (!isEventEnable()) return;

        sEvent.onStart(activity);
    }

    /**
     * Called by activties onStopre
     */
    public static void onStop(){
        if (!isEventEnable()) return;

        sEvent.onStop();
    }
    /**
     * Track fragment view, should be called in onResume
     *
     * @param fragment   the fragment name to track
     * @param screenName the additional name if any
     * @param subName    sub view name
     */
    public static void trackFragmentView(Fragment fragment, @Nullable String screenName, @Nullable String subName) {
        if (!isEventEnable()) return;

        String name;

        if (TextUtils.isEmpty(screenName)) {
            name = fragment.getClass().getSimpleName();
        } else {
            name = screenName;
        }

        if (TextUtils.isEmpty(name)) {
            name = "ERROR";
        }

        String detail = subName;
        if (TextUtils.isEmpty(detail)) {
            detail = "Fragment";
        }

        sEvent.sendView(name, detail);
    }

    /**
     * Track an undetermined error
     *
     * @param key   the key error
     * @param value the error detail
     */
    public static void trackError(String key, String value) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Error", key, value);
    }

    /**
     * Track when a website configuration is edited
     *
     * @param websiteName website name
     * @param isSave      if the edit is saved or not
     */
    public static void trackWebsiteEdit(String websiteName, boolean isSave) {
        if (!isEventEnable()) return;

        String mode;
        if (isSave) {
            mode = "save";
        } else {
            mode = "open";
        }

        sEvent.sendEvent("Website edit",
                "mode", mode,
                TRACKING_WEBSITE_NAME, websiteName);
    }

    /**
     * Track when a website is deleted/removed from the nagivationView
     *
     * @param websiteName website name
     */
    public static void trackWebsiteDelete(String websiteName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Website delete", TRACKING_WEBSITE_NAME, websiteName);
    }

    /**
     * Track when a website is set as the default/first website
     *
     * @param websiteName website name
     */
    public static void trackWebsiteDefault(String websiteName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Website delete", "Website set default", websiteName);
    }

    /**
     * Track when all websites are restored for new ones
     */
    public static void trackWebsitesRestored() {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Website restored");
    }

    /**
     * Track when a custom website is added
     */
    public static void trackCustomWebsiteAdded() {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Websites custom added");
    }

    /**
     * Track the copy action on an anecdote
     *
     * @param event the copy event
     */
    public static void trackAnecdoteCopy(CopyAnecdoteEvent event) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Anecdote copied",
                TRACKING_WEBSITE_NAME, event.websiteName,
                "Type", event.type);
    }

    /**
     * Track when the anecdote is shared
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteShare(String websiteName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Anecdote shared",
                TRACKING_WEBSITE_NAME, websiteName);
    }

    /**
     * Track when the anecdote details is open
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteDetails(String websiteName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Anecdote details",
                TRACKING_WEBSITE_NAME, websiteName);
    }

    /**
     * Track when the anecdote is opened with the "Read more" or open in browser button
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteReadMore(String websiteName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Anecdote read more",
                TRACKING_WEBSITE_NAME, websiteName);
    }

    /**
     * Track the click on third parties library
     *
     * @param thirdPartiesName the name of the third parties
     */
    public static void trackThirdPartiesClick(String thirdPartiesName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Third-parties click",
                "Third-parties", thirdPartiesName);
    }

    /**
     * Track the new value assigned to a setting
     *
     * @param name  the setting key
     * @param value the setting new value
     */
    public static void trackSettingChange(String name, String value) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Setting " + name + " changed",
                "value", value);
    }

    /**
     * Track when to data has been retrieved from this website, indicating possibly an issue with the selectors
     *
     * @param websiteName the website name
     */
    public static void trackWebsiteWrongConfiguration(String websiteName) {
        if (!isEventEnable()) return;

        sEvent.sendEvent("Website wrong configuration",
                TRACKING_WEBSITE_NAME, websiteName);
    }
}
