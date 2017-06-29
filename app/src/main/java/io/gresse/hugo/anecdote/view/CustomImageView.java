package io.gresse.hugo.anecdote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;

import java.io.File;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.util.ImageSaver;

/**
 * Custom imageview that add some functionalities to it
 * Created by Hugo Gresse on 18/03/2017.
 */

public class CustomImageView extends android.support.v7.widget.AppCompatImageView {
    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Nullable
    public File saveImage(){

        Drawable drawable = getDrawable();
        Bitmap bitmap = null;

        if(drawable instanceof BitmapDrawable){
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof GlideDrawable){
            bitmap = ((GlideBitmapDrawable)drawable.getCurrent()).getBitmap();
        }

        File output = new ImageSaver(getContext())
                .setExternal(true)
                .setDirectoryName(Configuration.DOWNLOAD_FOLDER)
                .setFileName(getContext().getString(R.string.app_name) + "-" + System.currentTimeMillis() + ".jpg")
                .save(bitmap);

        if(output != null){
            return output;
        }
        return null;
    }
}
