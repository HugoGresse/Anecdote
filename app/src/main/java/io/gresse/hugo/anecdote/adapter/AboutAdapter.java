package io.gresse.hugo.anecdote.adapter;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.util.EventUtils;

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

    @Override
    public BaseAboutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            default:
            case VIEW_TYPE_HEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_about_header, parent, false);
                return new HeaderAboutViewHolder(v);
            case VIEW_TYPE_NORMAL:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_about, parent, false);
                return new LibAboutViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(BaseAboutViewHolder holder, int position) {
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

    public class HeaderAboutViewHolder extends BaseAboutViewHolder implements View.OnClickListener {

        private Intent mAuthorIntent;

        @Bind(R.id.me_textview)
        public TextView authorTextView;

        public HeaderAboutViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            authorTextView.setOnClickListener(this);
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
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                EventUtils.trackThirdPartiesClick(authorTextView.getText().toString());
                mOnClickListener.onItemClick(mAuthorIntent);
            }
        }
    }

    public class LibAboutViewHolder extends BaseAboutViewHolder implements View.OnClickListener {

        @Bind(R.id.libTextView)
        public TextView textView;

        public LibAboutViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData(String text) {
            int lastUrl = text.lastIndexOf("http://");
            if (lastUrl == -1) {
                textView.setText(text);
            } else {
                textView.setText(text.substring(0, lastUrl));
            }
        }

        @Override
        public void onClick(View v) {
            // When clicked, we try to open the page in the default browser
            String textData = mData[getAdapterPosition() - 1];
            int lastUrl = textData.lastIndexOf("http://");
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

    public interface OnClickListener {
        void onItemClick(Intent intent);
    }

}
