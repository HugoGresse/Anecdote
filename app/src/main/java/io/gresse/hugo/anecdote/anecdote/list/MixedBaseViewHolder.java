package io.gresse.hugo.anecdote.anecdote.list;

import android.os.Build;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
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

    private int mExpandedPosition = -1;
    protected final AdapterListener mAdapterListener;
    protected final MixedContentAdapter mAdapter;
    private final int             mTextSize;
    private final boolean         mRowStriping;
    private final int             mRowBackground;
    private final int             mRowStripingBackground;

    private View mItemView;

    @Bind(R.id.contentTextView)
    TextView mTextView;

    @Bind(R.id.expandLayout)
    LinearLayout mExpandLayout;

    @Bind(R.id.separator)
    View mSeparatorView;

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
    public void setData(int position, Anecdote anecdote) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTextView.setText(Html.fromHtml(anecdote.text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            try {
                //noinspection deprecation
                mTextView.setText(Html.fromHtml(anecdote.text));
            } catch (Error error){
                mTextView.setText(anecdote.text);
            }
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

        if ( position == mExpandedPosition) {
            mSeparatorView.setVisibility(View.VISIBLE);
            mExpandLayout.setVisibility(View.VISIBLE);
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).topMargin = 50;
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).bottomMargin = 50;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(8);
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
        if (mExpandedPosition == getAdapterPosition()) {
            mExpandedPosition = -1;
            mAdapter.notifyItemChanged(getAdapterPosition());
            return;
        }
        // Notify expanded last position
        mAdapter.notifyItemChanged(mExpandedPosition);
        mExpandedPosition = getAdapterPosition();
        // Notify new element
        mAdapter.notifyItemChanged(mExpandedPosition);
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    itemView,
                    AdapterListener.ACTION_OPEN_IN_BROWSER_PRELOAD);
        }
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
}
