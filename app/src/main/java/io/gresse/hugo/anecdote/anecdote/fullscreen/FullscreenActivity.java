package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.ServiceProvider;
import io.gresse.hugo.anecdote.anecdote.MediaContextDialog;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.OpenAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.ShareAnecdoteEvent;
import io.gresse.hugo.anecdote.tracking.EventTracker;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.smoothie.module.SmoothieActivityModule;

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

    @Inject
    public    ServiceProvider mServiceProvider;
    private   String          mWebsitePageFullName;
    protected Anecdote        mAnecdote;
    protected String          mMediaTransitionName;

    @BindView(R.id.gradientBottom)
    public View         mGradientView;
    @BindView(R.id.overlayLinearLayout)
    public LinearLayout mOverlayLinearLayout;
    @BindView(R.id.contentTextView)
    public TextView     mContentTextView;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.putExtra(INTENT_MEDIA_TRANSITION, mediaView.getTransitionName());
        }

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Scope scope = Toothpick.openScopes(getApplication(), this);
        scope.installModules(new SmoothieActivityModule(this));
        super.onCreate(savedInstanceState);

        if (EventTracker.isEventEnable()) {
            new EventTracker(this);
        }

        mMediaTransitionName = getIntent().getStringExtra(INTENT_MEDIA_TRANSITION);
        String serviceSlug = getIntent().getStringExtra(INTENT_SERVICESLUG);
        int anecoteId = getIntent().getIntExtra(INTENT_ANECDOTE_ID, -1);

        if (serviceSlug == null || anecoteId < 0) {
            finish();
            return;
        }

        setContentView(getLayoutRes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
        ButterKnife.bind(this);
        Toothpick.inject(this, scope);

        AnecdoteService anecdoteService = mServiceProvider.getAnecdoteService(serviceSlug);

        mWebsitePageFullName = anecdoteService.getPageFullName();
        mAnecdote = anecdoteService.getAnecdotes().get(anecoteId);

        Log.d(TAG, "Loading " + mAnecdote.media);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventTracker.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Preload the website page
        EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsitePageFullName, mAnecdote, true));
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventTracker.onStop();
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
        // sometime mContentTextView is null, cf http://crashes.to/s/42615391f2d
        if (mContentTextView != null && mContentTextView.isShown()) {
            mOverlayLinearLayout.setVisibility(View.GONE);
            mGradientView.setVisibility(View.GONE);
        } else {
            mOverlayLinearLayout.setVisibility(View.VISIBLE);
            mGradientView.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
     * To be implemented by child, should return a layout to be inflated.
     */
    @LayoutRes
    protected abstract int getLayoutRes();

    @OnClick({R.id.shareButton, R.id.copyButton, R.id.openButton})
    public void onSocialClick(View v) {
        switch (v.getId()) {
            case R.id.shareButton:
                EventBus.getDefault().post(
                        new ShareAnecdoteEvent(
                                mWebsitePageFullName,
                                mAnecdote,
                                mAnecdote.getShareString(getApplicationContext())));
                break;
            case R.id.copyButton:
                EventBus.getDefault().post(
                        new CopyAnecdoteEvent(
                                mWebsitePageFullName,
                                mAnecdote,
                                CopyAnecdoteEvent.TYPE_ANECDOTE,
                                mAnecdote.getShareString(getApplicationContext())));
                break;
            case R.id.openButton:
                EventBus.getDefault().post(
                        new OpenAnecdoteEvent(
                                mWebsitePageFullName,
                                mAnecdote,
                                false));
                break;
            default:
                throw new IllegalArgumentException("This social share action should be implemented here...");
        }
    }
}
