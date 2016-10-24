package io.gresse.hugo.anecdote.util;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;

/**
 * Event related utils
 * <p/>
 * Created by Hugo Gresse on 25/04/16.
 */
public class EventUtils {

    public static final String CONTENT_TYPE_ANECDOTE = "Anecdote";
    public static final String CONTENT_TYPE_APP = "App";


    /**
     * Return true if event reporting is enable, checking the BuildConfig
     *
     * @return true if enable, false otherweise
     */
    public static boolean isEventEnable() {
        return !Configuration.DEBUG;
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

        ContentViewEvent contentViewEvent = new ContentViewEvent();

        contentViewEvent.putContentName(name);

        if (TextUtils.isEmpty(subName)) {
            contentViewEvent.putContentType("Fragment");
        } else {
            contentViewEvent.putContentType(subName);
        }


        Answers.getInstance().logContentView(new ContentViewEvent());
    }

    /**
     * Track an undetermined error
     *
     * @param key   the key error
     * @param value the error detail
     */
    public static void trackError(String key, String value) {
        if (!isEventEnable()) return;

        CustomEvent event = new CustomEvent("Error");
        event.putCustomAttribute(key, value);

        Answers.getInstance().logCustom(event);
    }

    /**
     * Track when a website configuration is edited
     *
     * @param websiteName website name
     * @param isSave      if the edit is saved or not
     */
    public static void trackWebsiteEdit(String websiteName, boolean isSave) {
        if (!isEventEnable()) return;

        CustomEvent event = new CustomEvent("Website edit");

        if (isSave) {
            event.putCustomAttribute("mode", "save");
        } else {
            event.putCustomAttribute("mode", "open");
        }

        event.putCustomAttribute("Website name", websiteName);

        Answers.getInstance().logCustom(event);
    }

    /**
     * Track when a website is deleted/removed from the nagivationView
     *
     * @param websiteName website name
     */
    public static void trackWebsiteDelete(String websiteName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Website delete")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track when a website is set as the default/first website
     *
     * @param websiteName website name
     */
    public static void trackWebsiteDefault(String websiteName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Website set default")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track when all websites are restored for new ones
     */
    public static void trackWebsitesRestored() {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(new CustomEvent("Websites restored"));
    }

    /**
     * Track when a custom website is added
     */
    public static void trackCustomWebsiteAdded() {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(new CustomEvent("Websites custom added"));
    }

    /**
     * Track the copy action on an anecdote
     *
     * @param event the copy event
     */
    public static void trackAnecdoteCopy(CopyAnecdoteEvent event) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Anecdote copied")
                        .putCustomAttribute("Website name", event.websiteName)
                        .putCustomAttribute("Type", event.type));
    }

    /**
     * Track when the anecdote is shared
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteShare(String websiteName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Anecdote shared")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track when the anecdote details is open
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteDetails(String websiteName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Anecdote details")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track when the anecdote is opened with the "Read more" or open in browser button
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteReadMore(String websiteName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Anecdote read more")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track the click on third parties library
     *
     * @param thirdPartiesName the name of the third parties
     */
    public static void trackThirdPartiesClick(String thirdPartiesName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Third-parties click")
                        .putCustomAttribute("Third-parties", thirdPartiesName));
    }

    /**
     * Track the new value assigned to a setting
     *
     * @param name  the setting key
     * @param value the setting new value
     */
    public static void trackSettingChange(String name, String value) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Setting " + name + " changed")
                        .putCustomAttribute("value", value));
    }

    /**
     * Track when to data has been retrieved from this website, indicating possibly an issue with the selectors
     *
     * @param websiteName the website name
     */
    public static void trackWebsiteWrongConfiguration(String websiteName) {
        if (!isEventEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Website wrong configuration")
                        .putCustomAttribute("Website name", websiteName));
    }
}
