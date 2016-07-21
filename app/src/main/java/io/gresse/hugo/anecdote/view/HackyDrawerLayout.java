package io.gresse.hugo.anecdote.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * See https://github.com/chrisbanes/PhotoView#issues-with-viewgroups
 *
 * Created by Hugo Gresse on 24/04/16.
 */
public class HackyDrawerLayout extends DrawerLayout {

    public HackyDrawerLayout(Context context) {
        super(context);
    }

    public HackyDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HackyDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d("HackyDrawerLayout", "onInterceptTouchEvent exception received");
            return false;
        }
    }
}
