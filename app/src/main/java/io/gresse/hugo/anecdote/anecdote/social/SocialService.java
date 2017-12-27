package io.gresse.hugo.anecdote.anecdote.social;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.DisplaySnackbarEvent;
import io.gresse.hugo.anecdote.event.RequestPermissionEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;
import io.gresse.hugo.anecdote.util.Utils;
import io.gresse.hugo.anecdote.util.chrome.ChromeCustomTabsManager;

/**
 * Manage social stuff like sharing, copy content, open link and so on.
 * <p>
 * Created by Hugo Gresse on 02/08/16.
 */
@Singleton
public class SocialService {

    @SuppressWarnings("unused")
    private static final String TAG = SocialService.class.getSimpleName();
    @Inject
    public Application mApplication;
    @Inject
    public ChromeCustomTabsManager mChromeCustomTabsManager;

    /**
     * Register service
     */
    public void register() {
        if (mChromeCustomTabsManager != null) {
            mChromeCustomTabsManager.bindCustomTabsService(mApplication);
        }
    }

    /**
     * Called when the main activity is destroyed
     */
    public void unregister() {
        if (mChromeCustomTabsManager != null) {
            mChromeCustomTabsManager.unbindCustomTabsService(mApplication);
        }
    }

    @Subscribe
    public void onShareAnecdote(ShareAnecdoteEvent event) {
        if (mApplication == null) return;

        EventTracker.trackAnecdoteShare(event.websiteName);

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                mApplication.getString(R.string.app_name));

        sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                event.shareString);

        Intent startIntent =Intent.createChooser(
                sharingIntent,
                mApplication.getResources().getString(R.string.anecdote_share_title));

        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mApplication.startActivity(startIntent);
    }

    @Subscribe
    public void onCopyAnecdote(CopyAnecdoteEvent event) {
        if (mApplication == null) return;

        EventTracker.trackAnecdoteCopy(event);

        Utils.copyToClipboard(
                mApplication,
                mApplication.getString(R.string.app_name),
                event.shareString);
        Toast.makeText(mApplication, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onOpenAnecdote(OpenAnecdoteEvent event) {
        if (mApplication == null) return;

        if (event.preloadOnly) {
            if (mChromeCustomTabsManager != null && !TextUtils.isEmpty(event.anecdote.permalink)) {
                mChromeCustomTabsManager.mayLaunch(event.anecdote.permalink);
            }
            return;
        }
        if (mChromeCustomTabsManager != null) {
            EventTracker.trackAnecdoteDetails(event.websiteName);
            mChromeCustomTabsManager.openChrome(mApplication, event.anecdote);
        }
    }

    @Subscribe
    public void onSaveFileAnecdote(SaveAndShareAnecdoteEvent event) {
        if (mApplication == null) return;

        if(!Utils.isStoragePermissionGranted(mApplication)){
            EventBus.getDefault().post(new RequestPermissionEvent(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, event));
            return;
        }

        File file = event.customImageView.saveImage();

        if (file == null) {
            Toast.makeText(mApplication, R.string.error_general, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            intent.setDataAndType(FileProvider.getUriForFile(mApplication, mApplication.getPackageName() + ".provider", file), "image/*");
        } else {
            intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/*");

        }

        EventBus.getDefault().postSticky(
                new DisplaySnackbarEvent(
                        mApplication.getString(R.string.notice_image_saved),
                        mApplication.getString(R.string.action_open),
                        intent,
                        Configuration.IMAGE_SAVE_TOAST_DURATION));
    }
}
