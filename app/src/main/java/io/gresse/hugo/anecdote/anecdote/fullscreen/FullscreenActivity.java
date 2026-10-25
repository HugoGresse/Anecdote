package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import io.gresse.hugo.anecdote.AnecdoteApplication;
import io.gresse.hugo.anecdote.anecdote.MediaContextDialog;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.OpenAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.ShareAnecdoteEvent;
import io.gresse.hugo.anecdote.databinding.ActivityFullscreenBaseBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteExpandBinding;
import io.gresse.hugo.anecdote.util.Utils;

/**
 * Fullscreen activity base class.
 * <p>
 * Created by Hugo Gresse on 15/04/2017.
 */

public abstract class FullscreenActivity extends AppCompatActivity {

    private static final String TAG                     = FullscreenActivity.class.getSimpleName();
    public static final  String INTENT_ANECDOTE_ID      = "intent_anecdote_id";
    public static final  String INTENT_SERVICESLUG      = "intent_serviceslug";
    public static final  String INTENT_MEDIA_TRANSITION = "intent_mediatransition";

    private   String          mWebsitePageFullName;
    protected Anecdote        mAnecdote;
    protected String          mMediaTransitionName;

    private View         mGradientView;
    private LinearLayout mOverlayLinearLayout;
    private TextView     mContentTextView;

    public static Intent createIntent(Context context,
                                      int anecdoteNumber,
                                      Anecdote anecdote,
                                      String serviceSlug,
                                      View mediaView) {
        Intent intent;

        if (anecdote.type == null) {
            throw new IllegalArgumentException("Null anecdote type for this anecdote on " + serviceSlug);
        }

        switch (anecdote.type) {
            case MediaType.IMAGE:
                intent = new Intent(context, FullscreenImageActivity.class);
                break;
            case MediaType.VIDEO:
                intent = new Intent(context, FullscreenVideoActivity.class);
                break;
            default:
                throw new IllegalArgumentException("Cannot open a fullscreen with the type: " + anecdote.type);
        }

        intent.putExtra(INTENT_ANECDOTE_ID, anecdoteNumber);
        intent.putExtra(INTENT_SERVICESLUG, serviceSlug);
        intent.putExtra(INTENT_MEDIA_TRANSITION, mediaView.getTransitionName());

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaTransitionName = getIntent().getStringExtra(INTENT_MEDIA_TRANSITION);
        String serviceSlug = getIntent().getStringExtra(INTENT_SERVICESLUG);
        int anecoteId = getIntent().getIntExtra(INTENT_ANECDOTE_ID, -1);

        if (serviceSlug == null || anecoteId < 0) {
            finish();
            return;
        }

        View contentView = inflateContentView();
        setContentView(contentView);
        postponeEnterTransition();
        bindOverlay(contentView);

        AnecdoteService anecdoteService = AnecdoteApplication.from(this)
                .getServiceProvider()
                .getAnecdoteService(serviceSlug);

        mWebsitePageFullName = anecdoteService.getPageFullName();
        mAnecdote = anecdoteService.getAnecdotes().get(anecoteId);

        Log.d(TAG, "Loading " + mAnecdote.media);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Preload the website page
        EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsitePageFullName, mAnecdote, true));
    }

    @Override
    public void finishAfterTransition() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    /**
     * Toggle the  Overlay/UI above the fullscreen content. This should be called by child fragment
     */
    protected void toggleOverlayVisibility() {
        if (mContentTextView.isShown()) {
            mOverlayLinearLayout.setVisibility(View.GONE);
            mGradientView.setVisibility(View.GONE);
        } else {
            mOverlayLinearLayout.setVisibility(View.VISIBLE);
            mGradientView.setVisibility(View.VISIBLE);
        }
    }

    protected void startPostponedEnterTransition(final View view) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                FullscreenActivity.this.startPostponedEnterTransition();
                return true;
            }
        });
    }

    /**
     * Display a dialog to have some option
     */
    protected void onContentLongTouch(String contentUrl, View viewTouched) {
        MediaContextDialog.openDialog(this, mWebsitePageFullName, mAnecdote, contentUrl, viewTouched);
    }

    /**
     * To be implemented by child: inflate its view binding and return the root view to display.
     */
    protected abstract View inflateContentView();

    private void bindOverlay(View contentView) {
        ActivityFullscreenBaseBinding overlayBinding = ActivityFullscreenBaseBinding.bind(contentView);
        mGradientView = overlayBinding.gradientBottom;
        mOverlayLinearLayout = overlayBinding.overlayLinearLayout;
        mContentTextView = overlayBinding.contentTextView;
        Utils.applyNavigationBarPadding(mOverlayLinearLayout);

        RowAnecdoteExpandBinding expandBinding = RowAnecdoteExpandBinding.bind(contentView);
        expandBinding.shareButton.setOnClickListener(v -> EventBus.getDefault().post(
                new ShareAnecdoteEvent(
                        mWebsitePageFullName,
                        mAnecdote,
                        mAnecdote.getShareString(getApplicationContext()))));
        expandBinding.copyButton.setOnClickListener(v -> EventBus.getDefault().post(
                new CopyAnecdoteEvent(
                        mWebsitePageFullName,
                        mAnecdote,
                        CopyAnecdoteEvent.TYPE_ANECDOTE,
                        mAnecdote.getShareString(getApplicationContext()))));
        expandBinding.openButton.setOnClickListener(v -> EventBus.getDefault().post(
                new OpenAnecdoteEvent(mWebsitePageFullName, mAnecdote, false)));
    }
}
