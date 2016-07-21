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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.OpenAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.ShareAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.util.FabricUtils;

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
    protected String mWebsiteName;

    @Bind(R.id.gradientBottom)
    public View mGradientView;

    @Bind(R.id.overlayLinearLayout)
    public LinearLayout mOverlayLinearLayout;

    @Bind(R.id.textView)
    public TextView mContentTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().post(new ChangeFullscreenEvent(true));

        if (getArguments() != null) {
            mAnecdote = new Gson().fromJson(
                    getArguments().getString(BUNDLE_ANECDOTE),
                    new TypeToken<Anecdote>() {}.getType());
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
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().post(new ChangeFullscreenEvent(false));
    }

    @Override
    public void onResume() {
        super.onResume();
        FabricUtils.trackFragmentView(this, null);
    }

    /***************************
     * Private methods
     ***************************/

    protected void toggleOverlayVisibility() {
        if (mContentTextView.isShown()) {
            Log.d(TAG, "toggleOverlayVisibility to GONE");
            mOverlayLinearLayout.setVisibility(View.GONE);
            mGradientView.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "toggleOverlayVisibility to VISIBLE");
            mOverlayLinearLayout.setVisibility(View.VISIBLE);
            mGradientView.setVisibility(View.VISIBLE);
        }
    }

    /***************************
     * onClick
     ***************************/

    @OnClick(R.id.shareButton)
    public void onShareClick() {
        EventBus.getDefault().post(new ShareAnecdoteEvent(mWebsiteName, mAnecdote));
    }

    @OnClick(R.id.copyButton)
    public void onCopyClick() {
        EventBus.getDefault().post(new CopyAnecdoteEvent(mWebsiteName, mAnecdote));
    }

    @OnClick(R.id.openButton)
    public void onOpenClick() {
        EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsiteName, mAnecdote, false));
    }
}
