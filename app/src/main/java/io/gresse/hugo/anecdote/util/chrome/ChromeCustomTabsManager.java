package io.gresse.hugo.anecdote.util.chrome;

import android.app.Application;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import javax.inject.Inject;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.util.Utils;

/**
 * Manage Chrome custom tabs binding and launching
 * <p/>
 * Created by Hugo Gresse on 06/05/16.
 */
public class ChromeCustomTabsManager implements ChromeCustomTabsConnectionCallback {

    public static final String TAG = ChromeCustomTabsManager.class.getSimpleName();
    public static final String CHROME_PACKAGE = "com.android.chrome";
    public static final String CHROME_SERVICE = "android.support.customtabs.action.CustomTabsService";

    private CustomTabsSession           mCustomTabsSession;
    private CustomTabsClient            mClient;
    private CustomTabsServiceConnection mConnection;

    // Configuration
    private int      mToolbarBackgroundColor;

    @Inject
    public ChromeCustomTabsManager(Application application) {
        mToolbarBackgroundColor = ContextCompat.getColor(application, R.color.colorAccent);
    }

    /**
     * Bins to custom chrome tab service
     */
    public void bindCustomTabsService(Context context) {
        if (mClient != null) return;
        mConnection = new ChromeCustomTabsConnection(this);
        CustomTabsClient.bindCustomTabsService(context, CHROME_PACKAGE, mConnection);
    }

    /**
     * Unbind from custom tabs chrome service
     */
    public void unbindCustomTabsService(Context context) {
        if (mConnection == null || context == null) return;

        Log.i(TAG, "Unbinding");

        try {
            context.unbindService(mConnection);
        } catch (IllegalArgumentException ignored) {
            // No need to unbind the service as it's not binded
        }
        mClient = null;
        mCustomTabsSession = null;
    }

    private CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(new CustomTabsCallback());
        }
        return mCustomTabsSession;
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        Log.i(TAG, "onServiceConnected");
        mClient = client;
        mClient.warmup(0L);
    }

    @Override
    public void onServiceDisconnected() {
        Log.i(TAG, "onServiceDisconnected");
        mClient = null;
    }

    /**
     * Prevent the Chrome client that the given url may be opened.
     */
    public void mayLaunch(String url) {
        if (mClient == null || url == null) {
            return;
        }
        Log.i(TAG, "mayLaunch " + url);
        CustomTabsSession session = getSession();
        session.mayLaunchUrl(Uri.parse(url), null, null);
    }

    /**
     * Open url
     */
    public void openChrome(Context context, Anecdote anecdote) {
        String packageName = CustomTabsHelper.getPackageNameToUse(context);

        //If we cant find a package name, it means theres no browser that supports
        //Chrome Custom Tabs installed. So, we fallback to the webview
        if (packageName == null) {
            try {
                Toast.makeText(context, R.string.open_intent_browser, Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(anecdote.permalink)));
            } catch (ActivityNotFoundException exception) {
                Toast.makeText(context, R.string.open_intent_browser_error, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Log.i(TAG, "openChrome");
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession());
        setIntentAction(context, builder, anecdote);

        builder.setShowTitle(true);
        builder.enableUrlBarHiding();
        builder.setToolbarColor(mToolbarBackgroundColor);

        builder.setSecondaryToolbarColor(Color.WHITE);
        builder.setStartAnimations(context, R.anim.slide_in_right, R.anim.hold);
        builder.setExitAnimations(context, R.anim.hold, R.anim.slide_out_left);
        builder.setCloseButtonIcon(
                Utils.getBitmapFromVectorDrawable(context, R.drawable.ic_arrow_back_white_24dp));

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        customTabsIntent.launchUrl(context, Uri.parse(anecdote.permalink));
    }

    private void setIntentAction(Context context, CustomTabsIntent.Builder builder, Anecdote anecdote){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));

        sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                anecdote.getPlainTextContent() + " " + context.getString(R.string.app_share_credits));

        builder.setActionButton(
                Utils.getBitmapFromVectorDrawable(context, R.drawable.ic_share_white_24dp),
                anecdote.getPlainTextContent() + " " + context.getString(R.string.app_share_credits),
                PendingIntent.getActivity(context, 0, sharingIntent, 0),
                false);
    }
}
