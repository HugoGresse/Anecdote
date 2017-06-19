package io.gresse.hugo.anecdote.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Enhanced FrameLayout with some features such as an ovelray/empty view.
 *
 * Created by Hugo Gresse on 18/06/2017.
 */

public class EnhancedFrameLayout extends FrameLayout {

    @Nullable
    private View mOverlayView;

    public EnhancedFrameLayout(Context context) {
        super(context);
    }

    public EnhancedFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhancedFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Display a given layout on top of everything.
     * @param layoutRes the layout id to be inflated
     */
    public void displayOverlay(@LayoutRes int layoutRes){
        if(mOverlayView != null){
            this.removeView(mOverlayView);
        }
        mOverlayView = LayoutInflater.from(getContext()).inflate(layoutRes, this, true);
    }

    /**
     * Hide the overlay if there was one.
     */
    public void hideOverlay(){
        if(mOverlayView != null){
            this.removeView(mOverlayView);
            mOverlayView = null;
        }
    }
}
