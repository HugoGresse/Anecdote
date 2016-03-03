package io.gresse.hugo.anecdote.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Website;

/**
 * Display a list of website
 *
 * Created by Hugo Gresse on 03/03/16.
 */
public class WebsiteChooserAdapter extends RecyclerView.Adapter<WebsiteChooserAdapter.BaseWebsiteViewHolder> {

    public static final String TAG = WebsiteChooserAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_CONTENT = 0;
    public static final int VIEW_TYPE_LOAD    = 1;

    private List<Website>      mWebsites;
    @Nullable
    private ViewHolderListener mViewHolderListener;

    public WebsiteChooserAdapter(@Nullable ViewHolderListener viewHolderListener) {
        mWebsites = new ArrayList<>();
        mViewHolderListener = viewHolderListener;
    }

    public void setData(List<Website> websites) {
        mWebsites = websites;
        notifyDataSetChanged();
    }

    @Override
    public BaseWebsiteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            default:
            case VIEW_TYPE_CONTENT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_websitechooser, parent, false);
                return new WebsiteViewHolder(v);
            case VIEW_TYPE_LOAD:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loader, parent, false);
                return new LoadViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(BaseWebsiteViewHolder holder, int position) {
        if (position < mWebsites.size()) {
            holder.setData(mWebsites.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if(mWebsites.size() > 0){
            return mWebsites.size();
        }
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mWebsites.size() > 0) {
            return VIEW_TYPE_CONTENT;
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    /***************************
     * ViewHolder
     ***************************/

    public abstract class BaseWebsiteViewHolder extends RecyclerView.ViewHolder {

        public BaseWebsiteViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setData(Website website);
    }

    public class WebsiteViewHolder extends BaseWebsiteViewHolder implements CompoundButton.OnCheckedChangeListener {

        @Bind(R.id.checkBox)
        CheckBox checkBox;

        public WebsiteViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void setData(Website website) {
            checkBox.setText(website.name);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mViewHolderListener != null) {
                mViewHolderListener.onClick(mWebsites.get(getAdapterPosition()));
            }
        }
    }

    public class LoadViewHolder extends BaseWebsiteViewHolder{

        public LoadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(Website website) {

        }
    }
}
