package io.gresse.hugo.anecdote.anecdote.social;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.util.EventUtils;
import io.gresse.hugo.anecdote.util.Utils;
import io.gresse.hugo.anecdote.util.chrome.ChromeCustomTabsManager;

/**
 * Manage social stuff like sharing, copy content, open link and so on.
 *
 * Created by Hugo Gresse on 02/08/16.
 */
public class SocialService {

    private static final String TAG = SocialService.class.getSimpleName();

    @Nullable
    private Activity mActivity;
    @Nullable
    private ChromeCustomTabsManager mChromeCustomTabsManager;

    public SocialService(@Nullable Activity activity) {
        mActivity = activity;
        if(mActivity == null){
            return;
        }
        mChromeCustomTabsManager = new ChromeCustomTabsManager(mActivity);
        mChromeCustomTabsManager.bindCustomTabsService(mActivity);
    }

    /**
     * Called when the main activity is destroyed
     */
    public void unregister() {
        if (mChromeCustomTabsManager != null) {
            mChromeCustomTabsManager.unbindCustomTabsService(mActivity);
        }
        mActivity = null;
    }

    @Subscribe
    public void onShareAnecdote(ShareAnecdoteEvent event) {
        if(mActivity == null) return;

        EventUtils.trackAnecdoteShare(event.websiteName);

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                mActivity.getString(R.string.app_name));

        sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                event.shareString);

        mActivity.startActivity(
                Intent.createChooser(
                        sharingIntent,
                        mActivity.getResources().getString(R.string.anecdote_share_title)));
    }

    @Subscribe
    public void onCopyAnecdote(CopyAnecdoteEvent event) {
        if(mActivity == null) return;

        EventUtils.trackAnecdoteCopy(event);

        Toast.makeText(mActivity, R.string.copied, Toast.LENGTH_SHORT).show();
        Utils.copyToClipboard(
                mActivity,
                mActivity.getString(R.string.app_name),
                event.shareString);
    }

    @Subscribe
    public void onOpenAnecdote(OpenAnecdoteEvent event) {
        if(mActivity == null) return;

        if (event.preloadOnly) {
            if (mChromeCustomTabsManager != null && !TextUtils.isEmpty(event.anecdote.permalink)) {
                mChromeCustomTabsManager.mayLaunch(event.anecdote.permalink);
            }
            return;
        }
        if (mChromeCustomTabsManager != null) {
            EventUtils.trackAnecdoteDetails(event.websiteName);
            mChromeCustomTabsManager.openChrome(mActivity, event.anecdote);
        }
    }
}
