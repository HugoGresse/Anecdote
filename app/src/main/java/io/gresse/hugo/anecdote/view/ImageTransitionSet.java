package io.gresse.hugo.anecdote.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.AttributeSet;

import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.EnterTransitionEndEvent;

/**
 * Transition that performs almost exactly like {@link android.transition.AutoTransition}, but has an
 * added {@link ChangeImageTransform} to support properly scaling up  images.
 *
 * Source: https://github.com/bherbst/FragmentTransitionSample/blob/master/app/src/main/java/com/example/fragmenttransitions/DetailsTransition.java
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ImageTransitionSet extends TransitionSet implements Transition.TransitionListener {

    public ImageTransitionSet() {
        init();
    }

    /**
     * This constructor allows us to use this transition in XML
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ImageTransitionSet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());

        super.addListener(this);
    }


    /***************************
     * Implements Transition.TransitionListener
     ***************************/

    @Override
    public void onTransitionStart(Transition transition) {

    }

    @Override
    public void onTransitionEnd(Transition transition) {
        BusProvider.getInstance().post(new EnterTransitionEndEvent());
    }

    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }
}
