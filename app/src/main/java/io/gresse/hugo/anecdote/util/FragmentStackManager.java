package io.gresse.hugo.anecdote.util;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.list.AnecdoteFragment;

/**
 * Manage fragment
 *
 * Created by Hugo Gresse on 16/04/2017.
 */

public class FragmentStackManager {

    private static final String TAG = FragmentStackManager.class.getSimpleName();

    @Nullable
    private Snackbar mSnackbar;

    public FragmentStackManager(Snackbar snackbar) {
        mSnackbar = snackbar;
    }

    /**
     * Change tu current displayed fragment by a new one.
     *
     * @param frag            the new fragment to display
     * @param saveInBackstack if we want the fragment to be in backstack
     * @param animate         if we want a nice animation or not
     * @param sharedView      the shared view for the transition
     * @param sharedName      the shared name of the transition
     */
    public void changeFragment(
                                AppCompatActivity activity,
                                Fragment frag,
                                boolean saveInBackstack,
                                boolean animate,
                                @Nullable View sharedView,
                                @Nullable String sharedName) {
        String log = "changeFragment: ";
        String backStateName = ((Object) frag).getClass().getName();

        if (frag instanceof AnecdoteFragment) {
            backStateName += frag.getArguments().getString(AnecdoteFragment.ARGS_WEBSITE_PAGE_SLUG);
        }

        try {
            android.support.v4.app.FragmentManager manager = activity.getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
                FragmentTransaction transaction = manager.beginTransaction();

                if (animate) {
                    log += " animate";
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                }
                if (sharedView != null && !TextUtils.isEmpty(sharedName)) {
                    ViewCompat.setTransitionName(sharedView, sharedName);
                    transaction.addSharedElement(sharedView, sharedName);
                }

                transaction.replace(R.id.fragment_container, frag, backStateName);

                if (saveInBackstack) {
                    log += " addToBackTack(" + backStateName + ")";
                    transaction.addToBackStack(backStateName);
                } else {
                    log += " NO addToBackTack(" + backStateName + ")";
                }

                transaction.commit();

                // If some snackbar is display, hide it
                if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
                    mSnackbar.dismiss();
                }

            } else if (!fragmentPopped && manager.findFragmentByTag(backStateName) != null) {
                log += " fragment not popped but finded: " + backStateName;
            } else {
                log += " nothing to do : " + backStateName + " fragmentPopped: " + fragmentPopped;
                // custom effect if fragment is already instanciated
            }
            Log.d(TAG, log);
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
        }
    }

}
