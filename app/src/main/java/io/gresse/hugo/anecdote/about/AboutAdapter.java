package io.gresse.hugo.anecdote.about;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;

import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.databinding.RowAboutBinding;
import io.gresse.hugo.anecdote.databinding.RowAboutHeaderBinding;

/**
 * About adapter
 * <p/>
 * Created by Hugo Gresse on 14/02/16.
 */
public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.BaseAboutViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    @Nullable
    private OnClickListener mOnClickListener;
    private String[]        mData;

    public AboutAdapter(@Nullable OnClickListener onClickListener, String[] data) {
        mOnClickListener = onClickListener;
        mData = data;
    }

    @NonNull
    @Override
    public BaseAboutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_NORMAL) {
            return new LibAboutViewHolder(RowAboutBinding.inflate(inflater, parent, false));
        }
        return new HeaderAboutViewHolder(RowAboutHeaderBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseAboutViewHolder holder, int position) {
        if (holder instanceof LibAboutViewHolder) {
            holder.setData(mData[position - 1]);
        } else {
            holder.setData("managed in layout");
        }
    }

    @Override
    public int getItemCount() {
        return mData.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_NORMAL;
    }

    public abstract class BaseAboutViewHolder extends RecyclerView.ViewHolder {
        public BaseAboutViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setData(String text);
    }

    public class HeaderAboutViewHolder extends BaseAboutViewHolder {

        private Intent mAuthorIntent;
        private Intent mCommunityIntent;
        private Intent mDonateIntent;

        private final TextView  authorTextView;
        private final TextView  communityTextView;
        private final ImageView donationImageView;

        public HeaderAboutViewHolder(RowAboutHeaderBinding binding) {
            super(binding.getRoot());
            authorTextView = binding.meTextview;
            communityTextView = binding.communityTextview;
            donationImageView = binding.donationImageView;

            authorTextView.setOnClickListener(v -> onItemClick(mAuthorIntent));
            communityTextView.setOnClickListener(v -> onItemClick(mCommunityIntent));
            donationImageView.setOnClickListener(v -> onItemClick(mDonateIntent));
        }

        @Override
        public void setData(String text) {
            String urlString = authorTextView.getText().toString();

            int lastUrl = urlString.lastIndexOf("http://");
            if (lastUrl == -1) {
                authorTextView.setText(urlString);
            } else try {
                authorTextView.setText(urlString.substring(0, lastUrl));
                URL url = new URL(urlString.substring(lastUrl));
                mAuthorIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
            } catch (MalformedURLException e) {
                // Do nothing
            }

            String comunityString = communityTextView.getText().toString();

            int communityStringEnd = comunityString.lastIndexOf("https://");
            if (communityStringEnd == -1) {
                communityTextView.setText(comunityString);
            } else try {
                communityTextView.setText(comunityString.substring(0, communityStringEnd));
                URL url = new URL(comunityString.substring(communityStringEnd));
                mCommunityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
            } catch (MalformedURLException e) {
                // Do nothing
            }

            mDonateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Configuration.DONATION_LINK));

            Glide.with(donationImageView.getContext())
                    .load("https://www.paypalobjects.com/webstatic/en_US/i/btn/png/silver-rect-paypal-60px.png")
                    .into(donationImageView);
        }

        private void onItemClick(@Nullable Intent intent) {
            if (mOnClickListener != null && intent != null) {
                mOnClickListener.onItemClick(intent);
            }
        }
    }

    public class LibAboutViewHolder extends BaseAboutViewHolder implements View.OnClickListener {

        private final TextView textView;

        public LibAboutViewHolder(RowAboutBinding binding) {
            super(binding.getRoot());
            textView = binding.libTextView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData(String text) {
            int lastUrl = urlStartIndex(text);
            if (lastUrl == -1) {
                textView.setText(text);
            } else {
                textView.setText(text.substring(0, lastUrl));
            }
        }

        @Override
        public void onClick(View v) {
            // When clicked, we try to open the page in the default browser
            String textData = mData[getBindingAdapterPosition() - 1];
            int lastUrl = urlStartIndex(textData);
            if (lastUrl > -1) try {
                URL url = new URL(textData.substring(lastUrl));

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));

                if (mOnClickListener != null) {
                    mOnClickListener.onItemClick(browserIntent);
                }
            } catch (MalformedURLException e) {
                // Do nothing
            }
        }
    }

    /**
     * @return the index where the trailing url of the given text starts, -1 if it does not end with one
     */
    static int urlStartIndex(String text) {
        int lastTokenStart = text.lastIndexOf(' ') + 1;
        return text.startsWith("http", lastTokenStart) ? lastTokenStart : -1;
    }

    public interface OnClickListener {
        void onItemClick(Intent intent);
    }

}
