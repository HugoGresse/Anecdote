package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.MediaContextDialog;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.OpenAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.ShareAnecdoteEvent;

/**
 * Base fragment for all fullscreen fragment
 * <p/>
 * Created by Hugo Gresse on 20/07/16.
 */
public abstract class FullscreenFragment extends Fragment {

    public static final String TAG                = FullscreenFragment.class.getSimpleName();
    public static final String BUNDLE_ANECDOTE    = "anecdoteJson";
    public static final String BUNDLE_WEBSITENAME = "websiteName";

    protected Anecdote mAnecdote;
    protected String   mWebsiteName;
    protected Unbinder mUnbinder;

    @BindView(R.id.gradientBottom)
    public View mGradientView;

    @BindView(R.id.overlayLinearLayout)
    public LinearLayout mOverlayLinearLayout;

    @BindView(R.id.contentTextView)
    public TextView mContentTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().post(new ChangeFullscreenEvent(true));

        if (getArguments() != null) {
            mAnecdote = new Gson().fromJson(
                    getArguments().getString(BUNDLE_ANECDOTE),
                    new TypeToken<Anecdote>() {
                    }.getType());
            mWebsiteName = getArguments().getString(BUNDLE_WEBSITENAME);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContentTextView.setText(mAnecdote.text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().post(new ChangeFullscreenEvent(false));
    }

    /***************************
     * Private methods
     ***************************/

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

    /**
     * Display a dialog to have some option
     */
    protected void onContentLongTouch(String contentUrl, View viewTouched) {
        MediaContextDialog.openDialog(getActivity(), mWebsiteName, mAnecdote, contentUrl, viewTouched);
    }

    /***************************
     * onClick
     ***************************/

    @OnClick(R.id.shareButton)
    public void onShareClick() {
        EventBus.getDefault().post(new ShareAnecdoteEvent(mWebsiteName, mAnecdote, mAnecdote.getShareString(getContext())));
    }

    @OnClick(R.id.copyButton)
    public void onCopyClick() {
        EventBus.getDefault().post(new CopyAnecdoteEvent(mWebsiteName, mAnecdote, CopyAnecdoteEvent.TYPE_ANECDOTE, mAnecdote.getShareString(getContext())));
    }

    @OnClick(R.id.openButton)
    public void onOpenClick() {
        EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsiteName, mAnecdote, false));
    }
}
