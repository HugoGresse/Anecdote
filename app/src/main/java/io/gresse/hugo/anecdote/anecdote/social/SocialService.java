package io.gresse.hugo.anecdote.anecdote.social;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.DisplaySnackbarEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;
import io.gresse.hugo.anecdote.util.Utils;
import io.gresse.hugo.anecdote.util.chrome.ChromeCustomTabsManager;

/**
 * Manage social stuff like sharing, copy content, open link and so on.
 * <p>
 * Created by Hugo Gresse on 02/08/16.
 */
public class SocialService {

    @SuppressWarnings("unused")
    private static final String TAG = SocialService.class.getSimpleName();

    @Nullable
    private Activity                mActivity;
    @Nullable
    private ChromeCustomTabsManager mChromeCustomTabsManager;

    public SocialService(@Nullable Activity activity) {
        mActivity = activity;
        if (mActivity == null) {
            return;
        }
        mChromeCustomTabsManager = new ChromeCustomTabsManager(mActivity);
    }

    /**
     * Register service
     */
    public void register(Activity activity) {
        mActivity = activity;
        if (mChromeCustomTabsManager != null) {
            mChromeCustomTabsManager.bindCustomTabsService(mActivity);
        }
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
        if (mActivity == null) return;

        EventTracker.trackAnecdoteShare(event.websiteName);

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
        if (mActivity == null) return;

        EventTracker.trackAnecdoteCopy(event);

        Utils.copyToClipboard(
                mActivity,
                mActivity.getString(R.string.app_name),
                event.shareString);
        Toast.makeText(mActivity, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onOpenAnecdote(OpenAnecdoteEvent event) {
        if (mActivity == null) return;

        if (event.preloadOnly) {
            if (mChromeCustomTabsManager != null && !TextUtils.isEmpty(event.anecdote.permalink)) {
                mChromeCustomTabsManager.mayLaunch(event.anecdote.permalink);
            }
            return;
        }
        if (mChromeCustomTabsManager != null) {
            EventTracker.trackAnecdoteDetails(event.websiteName);
            mChromeCustomTabsManager.openChrome(mActivity, event.anecdote);
        }
    }

    @Subscribe
    public void onSaveFileAnecdote(SaveAndShareAnecdoteEvent event) {
        if (mActivity == null) return;

        File file = event.customImageView.saveImage();

        if (file == null) {
            Toast.makeText(mActivity, R.string.error_general, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/*");

        EventBus.getDefault().post(
                new DisplaySnackbarEvent(
                        mActivity.getString(R.string.notice_image_saved),
                        mActivity.getString(R.string.action_open),
                        intent,
                        Configuration.IMAGE_SAVE_TOAST_DURATION));
    }
}
