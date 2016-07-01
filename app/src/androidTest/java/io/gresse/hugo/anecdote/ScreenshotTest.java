package io.gresse.hugo.anecdote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;

import junit.framework.Assert;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.gresse.hugo.anecdote.storage.SpStorage;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;


@RunWith(JUnit4.class)
public class ScreenshotTest {


    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @SuppressLint("CommitPrefEdits")
        @Override
        protected void beforeActivityLaunched() {
            SharedPreferences prefs =
                    InstrumentationRegistry.getTargetContext().getSharedPreferences(SpStorage.SP_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            super.beforeActivityLaunched();
        }
    };

    @Test
    public void websiteChooserScreenshots() throws InterruptedException {
        Screengrab.screenshot("websiteChooserFragment1");

        //progress dialog is now shown
        Thread.sleep(1500);

        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.actionOnItemAtPosition(
                        0, AnecdoteRecyclerViewAction.clickChildViewWithId(R.id.checkBox)));
        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.actionOnItemAtPosition(
                        1, AnecdoteRecyclerViewAction.clickChildViewWithId(R.id.checkBox)));
        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.actionOnItemAtPosition(
                        2, AnecdoteRecyclerViewAction.clickChildViewWithId(R.id.checkBox)));
        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.actionOnItemAtPosition(
                        4, AnecdoteRecyclerViewAction.clickChildViewWithId(R.id.checkBox)));

        Thread.sleep(300);

        Screengrab.screenshot("websiteChooserFragment2");

        onView(withId(R.id.saveButton)).perform(click());
        Thread.sleep(2000);

        Screengrab.screenshot("anecdote1");


//        onView(allOf(withId(R.id.recyclerView), withClassName(endsWith("TextView")))).perform(
//                RecyclerViewActions.actionOnItemAtPosition(
//                        0,  AnecdoteRecyclerViewAction.clickAnecdoteTextViewWithId(R.id.contentTextView)));

        onView(allOf(withId(R.id.recyclerView))).perform(
                RecyclerViewActions.actionOnItemAtPosition(
                        0,  AnecdoteRecyclerViewAction.clickAnecdoteTextViewWithId(R.id.contentTextView)));

        Thread.sleep(500);

        Screengrab.screenshot("anecdoteCliked");

        // Dump check
        Assert.assertEquals(1, 1);
    }

}