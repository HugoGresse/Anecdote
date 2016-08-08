package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gresse.hugo.anecdote.R;
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
        ButterKnife.unbind(this);
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

    /**
     * Display a dialog to have some option
     */
    protected void onContentLongTouch(final String contentUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.anecdote_content_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                switch (which) {
                    // Copy
                    case 0:
                        EventBus.getDefault().post(new CopyAnecdoteEvent(mWebsiteName, mAnecdote, CopyAnecdoteEvent.TYPE_MEDIA, contentUrl));
                        break;
                    default:
                        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builder.show();
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
