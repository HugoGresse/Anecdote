package io.gresse.hugo.anecdote.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.util.ImageSaver;

/**
 * ImageView able to save its current drawable to the user gallery.
 * Created by Hugo Gresse on 18/03/2017.
 */
public class CustomImageView extends AppCompatImageView {

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
    public Uri saveImage() {
        String fileName = getContext().getString(R.string.app_name) + "-" + System.currentTimeMillis() + ".png";
        return ImageSaver.saveToGallery(getContext(), getDrawable(), Configuration.DOWNLOAD_FOLDER, fileName);
    }
}
