package io.gresse.hugo.anecdote.anecdote.list;

import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteExpandBinding;

/***************************
 * ViewHolder
 ***************************/

public class MixedBaseViewHolder
        extends AnecdoteAdapter.BaseAnecdoteViewHolder
        implements View.OnClickListener {

    protected final AdapterListener     mAdapterListener;
    protected final MixedContentAdapter mAdapter;
    private final   RowStyle            mRowStyle;

    protected String   mWebsiteName = "";
    protected Anecdote mCurrentAnecdote;

    private final TextView     mTextView;
    @Nullable
    private final LinearLayout mExpandLayout;
    @Nullable
    private final View         mSeparatorView;
    @Nullable
    private final ImageButton  mFavoriteButton;

    public MixedBaseViewHolder(RowAnecdoteBinding binding,
                               AdapterListener adapterListener,
                               MixedContentAdapter adapter,
                               RowStyle rowStyle) {
        this(binding.getRoot(),
                binding.contentTextView,
                binding.separator,
                RowAnecdoteExpandBinding.bind(binding.getRoot()),
                adapterListener,
                adapter,
                rowStyle);
    }

    protected MixedBaseViewHolder(View itemView,
                                  TextView contentTextView,
                                  @Nullable View separatorView,
                                  @Nullable RowAnecdoteExpandBinding expandBinding,
                                  AdapterListener adapterListener,
                                  MixedContentAdapter adapter,
                                  RowStyle rowStyle) {
        super(itemView);

        mAdapterListener = adapterListener;
        mAdapter = adapter;
        mRowStyle = rowStyle;
        mTextView = contentTextView;
        mSeparatorView = separatorView;
        itemView.setOnClickListener(this);

        if (expandBinding == null) {
            mExpandLayout = null;
            mFavoriteButton = null;
            return;
        }
        mExpandLayout = expandBinding.expandLayout;
        mFavoriteButton = expandBinding.favoritesButton;
        expandBinding.shareButton.setOnClickListener(v -> notifyAction(AdapterListener.ACTION_SHARE));
        expandBinding.copyButton.setOnClickListener(v -> notifyAction(AdapterListener.ACTION_COPY));
        expandBinding.openButton.setOnClickListener(v -> notifyAction(AdapterListener.ACTION_OPEN_IN_BROWSER));
        expandBinding.favoritesButton.setOnClickListener(v -> notifyAction(AdapterListener.ACTION_FAVORIS));
    }

    @Override
    public void setData(int position, boolean isExpanded, String websiteName, Anecdote anecdote) {
        mWebsiteName = websiteName;
        mCurrentAnecdote = anecdote;
        try {
            mTextView.setText(Html.fromHtml(mCurrentAnecdote.text, Html.FROM_HTML_MODE_LEGACY));
        } catch (Throwable error) {
            mTextView.setText(mCurrentAnecdote.text);
        }
        mTextView.setTextSize(mRowStyle.textSize());

        if (mRowStyle.rowStriping()) {
            itemView.setBackgroundColor(mRowStyle.backgroundForPosition(position));
        }

        if (mExpandLayout == null || mSeparatorView == null || mFavoriteButton == null) {
            return;
        }

        if (isExpanded) {
            mSeparatorView.setVisibility(View.VISIBLE);
            mExpandLayout.setVisibility(View.VISIBLE);
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).topMargin = 50;
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).bottomMargin = 50;
            itemView.setElevation(8);
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
            itemView.setElevation(0);
        }
    }

    @Override
    public void onClick(View v) {
        mAdapter.toggleExpanded(getBindingAdapterPosition());
    }

    private void notifyAction(int action) {
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getBindingAdapterPosition()),
                    itemView,
                    action);
        }
    }
}
