package io.gresse.hugo.anecdote.util.chrome;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Anecdote;

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

    public ChromeCustomTabsManager(@NonNull Activity activity) {
        mToolbarBackgroundColor = ContextCompat.getColor(activity, R.color.colorAccent);
    }

    /**
     * Bins to custom chrome tab service
     */
    public void bindCustomTabsService(Activity activity) {
        if (mClient != null) return;
        mConnection = new ChromeCustomTabsConnection(this);
        CustomTabsClient.bindCustomTabsService(activity, CHROME_PACKAGE, mConnection);
    }

    /**
     * Unbind from custom tabs chrome service
     */
    public void unbindCustomTabsService(Activity activity) {
        if (mConnection == null || activity == null) return;

        try {
            activity.unbindService(mConnection);
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
        Log.i(TAG, "mayLaunch");
        CustomTabsSession session = getSession();
        session.mayLaunchUrl(Uri.parse(url), null, null);
    }

    /**
     * Open url
     */
    public void openChrome(Activity activity, Anecdote anecdote) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        //If we cant find a package name, it means theres no browser that supports
        //Chrome Custom Tabs installed. So, we fallback to the webview
        if (packageName == null) {
            try {
                Toast.makeText(activity, R.string.open_intent_browser, Toast.LENGTH_SHORT).show();
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(anecdote.permalink)));
            } catch (ActivityNotFoundException exception) {
                Toast.makeText(activity, R.string.open_intent_browser_error, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Log.i(TAG, "openChrome");
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession());
        setIntentAction(activity, builder, anecdote);

        builder.setShowTitle(true);
        builder.enableUrlBarHiding();
        builder.setToolbarColor(mToolbarBackgroundColor);
        builder.setSecondaryToolbarColor(activity.getResources().getColor(android.R.color.white));
        builder.setStartAnimations(activity, R.anim.slide_in_right, R.anim.hold);
        builder.setExitAnimations(activity, R.anim.hold, R.anim.slide_out_left);
        builder.setCloseButtonIcon(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_navigation_arrow_back));

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.launchUrl(activity, Uri.parse(anecdote.permalink));
    }

    private void setIntentAction(Activity activity, CustomTabsIntent.Builder builder, Anecdote anecdote){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));

        sharingIntent.putExtra(
                android.content.Intent.EXTRA_TEXT,
                anecdote.getPlainTextContent() + " " + activity.getString(R.string.app_share_credits));

        builder.setActionButton(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_social_share),
                anecdote.getPlainTextContent() + " " + activity.getString(R.string.app_share_credits),
                PendingIntent.getActivity(activity, 0, sharingIntent, 0),
                false);
    }
}
