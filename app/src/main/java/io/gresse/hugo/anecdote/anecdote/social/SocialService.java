package io.gresse.hugo.anecdote.anecdote.social;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.DisplaySnackbarEvent;
import io.gresse.hugo.anecdote.event.RequestPermissionEvent;
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
    private final Application             mApplication;
    private final ChromeCustomTabsManager mChromeCustomTabsManager;

    public SocialService(Application application, ChromeCustomTabsManager chromeCustomTabsManager) {
        mApplication = application;
        mChromeCustomTabsManager = chromeCustomTabsManager;
    }

    /**
     * Register service
     */
    public void register() {
        mChromeCustomTabsManager.bindCustomTabsService(mApplication);
    }

    /**
     * Called when the main activity is destroyed
     */
    public void unregister() {
        mChromeCustomTabsManager.unbindCustomTabsService(mApplication);
    }

    @Subscribe
    public void onShareAnecdote(ShareAnecdoteEvent event) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        sharingIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                mApplication.getString(R.string.app_name));

        sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                event.shareString);

        Intent startIntent = Intent.createChooser(
                sharingIntent,
                mApplication.getResources().getString(R.string.anecdote_share_title));

        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mApplication.startActivity(startIntent);
    }

    @Subscribe
    public void onCopyAnecdote(CopyAnecdoteEvent event) {
        Utils.copyToClipboard(
                mApplication,
                mApplication.getString(R.string.app_name),
                event.shareString);
        Toast.makeText(mApplication, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onOpenAnecdote(OpenAnecdoteEvent event) {
        if (event.preloadOnly) {
            if (!TextUtils.isEmpty(event.anecdote.permalink)) {
                mChromeCustomTabsManager.mayLaunch(event.anecdote.permalink);
            }
            return;
        }
        mChromeCustomTabsManager.openChrome(mApplication, event.anecdote);
    }

    @Subscribe
    public void onSaveFileAnecdote(SaveAndShareAnecdoteEvent event) {
        if (mApplication == null) return;

        if(!Utils.isStoragePermissionGranted(mApplication)){
            EventBus.getDefault().post(new RequestPermissionEvent(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, event));
            return;
        }

        Uri imageUri = event.customImageView.saveImage();

        if (imageUri == null) {
            Toast.makeText(mApplication, R.string.error_general, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(imageUri, "image/*");

        EventBus.getDefault().postSticky(
                new DisplaySnackbarEvent(
                        mApplication.getString(R.string.notice_image_saved),
                        mApplication.getString(R.string.action_open),
                        intent,
                        Configuration.IMAGE_SAVE_TOAST_DURATION));
    }
}
