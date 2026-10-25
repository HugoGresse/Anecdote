package io.gresse.hugo.anecdote.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.gresse.hugo.anecdote.api.model.Website;

/**
 * Generals utils
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class Utils {

    /**
     * Return the user agent sent on all request. It's not default Android user agent as we don't really want that
     * websites see the app trafic (for now at least).
     * <p/>
     * Be only replace the device by the correct one.
     *
     * @return the user agent
     */
    public static String getUserAgent() {
        return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1 " + Build.MODEL +
                ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.95 Mobile Safari/537.36";
    }

    /**
     * Return the user agent sent on all request. It will choose between our local custom user agent or given
     * useragent for this website.
     *
     * @return the user agent
     */
    public static String getUserAgent(Website website) {
        if (TextUtils.isEmpty(website.userAgent)) {
            return getUserAgent();
        }
        return website.userAgent;
    }

    /**
     * Copy given context to device clipboard
     *
     * @param context app context
     * @param title   title/label of this clipboard
     * @param content text to be copied
     */
    public static void copyToClipboard(@NonNull Context context, String title, String content) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(title, content);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Load bitmap in memory from a vector
     *
     * @param context    app context
     * @param drawableId drawable res
     * @return the bitmap
     */
    @Nullable
    public static Bitmap getBitmapFromVectorDrawable(Context context, @DrawableRes int drawableId) {
        return drawableToBitmap(ContextCompat.getDrawable(context, drawableId));
    }

    /**
     * @return the drawable bitmap if it has one, else the drawable rendered at its intrinsic size
     */
    @Nullable
    public static Bitmap drawableToBitmap(@Nullable Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable bitmapDrawable && bitmapDrawable.getBitmap() != null) {
            return bitmapDrawable.getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Chech if we have write permissions
     */
    public static boolean isStoragePermissionGranted(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return application.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Pad the bottom of the view by the navigation bar height, keeping its original padding.
     */
    public static void applyNavigationBarPadding(View view) {
        int initialBottomPadding = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    initialBottomPadding + navigationBars.bottom);
            return insets;
        });
    }
}
