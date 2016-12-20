package io.gresse.hugo.anecdote.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
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
        if(TextUtils.isEmpty(website.userAgent)){
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
    public static void copyToClipboard(@NonNull Context context, String title, String content){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            @SuppressWarnings("deprecation")
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(content);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(title, content);
            clipboard.setPrimaryClip(clip);
        }
    }

}
