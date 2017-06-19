package io.gresse.hugo.anecdote.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;

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
    public static Bitmap getBitmapFromVectorDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
