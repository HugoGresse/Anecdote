package io.gresse.hugo.anecdote.util;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import io.gresse.hugo.anecdote.Configuration;

/**
 * Fabric related utils
 * <p/>
 * Created by Hugo Gresse on 25/04/16.
 */
public class FabricUtils {

    /**
     * Return true if fabric is enable, checking the BuildConfig
     *
     * @return true if enable, false otherweise
     */
    public static boolean isFabricEnable() {
        return !Configuration.DEBUG;
    }

    /**
     * Track fragment view, should be called in onResume
     *
     * @param fragment   the fragment name to track
     * @param screenName the additional name if any
     */
    public static void trackFragmentView(Fragment fragment, @Nullable String screenName) {
        if (!isFabricEnable()) return;

        String name;

        if (TextUtils.isEmpty(screenName)) {
            name = fragment.getClass().getSimpleName();
        } else {
            name = screenName;
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(name)
                .putContentType("Fragment"));
    }

    /**
     * Track when a website configuration is edited
     *
     * @param websiteName website name
     * @param isSave      if the edit is saved or not
     */
    public static void trackWebsiteEdit(String websiteName, boolean isSave) {
        if (!isFabricEnable()) return;

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
        if (!isFabricEnable()) return;

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
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Website set default")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track when all websites are restored for new ones
     */
    public static void trackWebsitesRestored() {
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(new CustomEvent("Websites restored"));
    }

    /**
     * Track when a custom website is added
     */
    public static void trackCustomWebsiteAdded() {
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(new CustomEvent("Websites custom added"));
    }

    /**
     * Track the copy action on an anecdote
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteCopy(String websiteName) {
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Anecdote copied")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track when the anecdote is shared
     *
     * @param websiteName the website name
     */
    public static void trackAnecdoteShare(String websiteName) {
        if (!isFabricEnable()) return;

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
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Anecdote details")
                        .putCustomAttribute("Website name", websiteName));
    }

    /**
     * Track the click on third parties library
     *
     * @param thirdPartiesName the name of the third parties
     */
    public static void trackThirdPartiesClick(String thirdPartiesName){
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Third-parties click")
                        .putCustomAttribute("Third-parties", thirdPartiesName));
    }

    /**
     * Track the new value assigned to a setting
     *
     * @param name the setting key
     * @param value the setting new value
     */
    public static void trackSettingChange(String name, String value){
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Setting " + name + " changed")
                        .putCustomAttribute("value", value));
    }

    /**
     * Track when to data has been retrieved from this website, indicating possibly an issue with the selectors
     *
     * @param websiteName the website name
     */
    public static void trackWebsiteWrongConfiguration(String websiteName){
        if (!isFabricEnable()) return;

        Answers.getInstance().logCustom(
                new CustomEvent("Website wrong configuration")
                        .putCustomAttribute("Website name", websiteName));
    }
}
