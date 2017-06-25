package io.gresse.hugo.anecdote.anecdote.list;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;

/***************************
 * ViewHolder
 ***************************/

public class MixedBaseViewHolder
        extends AnecdoteAdapter.BaseAnecdoteViewHolder
        implements View.OnClickListener {

    protected final AdapterListener     mAdapterListener;
    protected final MixedContentAdapter mAdapter;
    private final   int                 mTextSize;
    private final   boolean             mRowStriping;
    private final   int                 mRowBackground;
    private final   int                 mRowStripingBackground;

    private View mItemView;
    protected String mWebsiteName = "";
    protected Anecdote mCurrentAnecdote;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.contentTextView)
    public TextView mTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.expandLayout)
    public LinearLayout mExpandLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.separator)
    public View mSeparatorView;

    @BindView(R.id.favoritesButton)
    public ImageButton mFavoriteButton;

    public MixedBaseViewHolder(View itemView,
                               AdapterListener adapterListener,
                               MixedContentAdapter adapter,
                               int textSize,
                               boolean rowStriping,
                               int rowBackground,
                               int rawStripingBackground) {
        super(itemView);

        mAdapterListener = adapterListener;
        mAdapter = adapter;
        mTextSize = textSize;
        mItemView = itemView;
        mRowStriping = rowStriping;
        mRowBackground = rowBackground;
        mRowStripingBackground = rawStripingBackground;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void setData(int position, boolean isExpanded, String websiteName, Anecdote anecdote) {
        mWebsiteName = websiteName;
        mCurrentAnecdote = anecdote;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTextView.setText(Html.fromHtml(mCurrentAnecdote.text, Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                mTextView.setText(Html.fromHtml(mCurrentAnecdote.text));
            }
        } catch (Throwable error) {
            mTextView.setText(mCurrentAnecdote.text);
        }
        mTextView.setTextSize(mTextSize);

        if (mRowStriping) {
            if (position % 2 == 0) {
                mItemView.setBackgroundColor(mRowStripingBackground);
            } else {
                mItemView.setBackgroundColor(mRowBackground);
            }
        }

        if (mExpandLayout == null) {
            return;
        }

        if (isExpanded) {
            mSeparatorView.setVisibility(View.VISIBLE);
            mExpandLayout.setVisibility(View.VISIBLE);
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).topMargin = 50;
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).bottomMargin = 50;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(8);
            }
            if(mCurrentAnecdote.isFavorite()){
                DrawableCompat.setTint(
                        mFavoriteButton.getDrawable(),
                        ContextCompat.getColor(mFavoriteButton.getContext(), R.color.favorite));
            } else {
                DrawableCompat.setTint(
                        mFavoriteButton.getDrawable(),
                        Color.WHITE);
            }
        } else {
            mSeparatorView.setVisibility(View.GONE);
            mExpandLayout.setVisibility(View.GONE);
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).topMargin = 0;
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).bottomMargin = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        mAdapter.toggleExpanded(getAdapterPosition());
    }

    @OnClick(R.id.shareButton)
    public void onShareClick() {
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    itemView,
                    AdapterListener.ACTION_SHARE);
        }
    }

    @OnClick(R.id.copyButton)
    public void onCopyClick() {
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    itemView,
                    AdapterListener.ACTION_COPY);
        }
    }

    @OnClick(R.id.openButton)
    public void onOpenClick() {
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    itemView,
                    AdapterListener.ACTION_OPEN_IN_BROWSER);
        }
    }

    @OnClick(R.id.favoritesButton)
    public void onFavoriteClick(){
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    itemView,
                    AdapterListener.ACTION_FAVORIS);
        }
    }
}
