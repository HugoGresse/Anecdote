package io.gresse.hugo.anecdote;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.gresse.hugo.anecdote.storage.SpStorage;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class ScreenshotTest {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    private ActivityScenario<MainActivity> mScenario;

    @Before
    public void launchWithCleanPreferences() {
        InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences(SpStorage.SP_KEY, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
        mScenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void closeActivity() {
        mScenario.close();
    }

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

        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.actionOnItemAtPosition(
                        0,  AnecdoteRecyclerViewAction.clickAnecdoteTextViewWithId(R.id.contentTextView)));

        Thread.sleep(500);

        Screengrab.screenshot("anecdoteCliked");
    }

}