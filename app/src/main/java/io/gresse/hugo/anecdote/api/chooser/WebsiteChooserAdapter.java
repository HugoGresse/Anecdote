package io.gresse.hugo.anecdote.api.chooser;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.WebsiteViewHolderListener;
import io.gresse.hugo.anecdote.api.model.Website;

/**
 * Display a list of website
 * <p/>
 * Created by Hugo Gresse on 03/03/16.
 */
public class WebsiteChooserAdapter extends RecyclerView.Adapter<WebsiteChooserAdapter.BaseWebsiteViewHolder> {

    @SuppressWarnings("unused")
    public static final String TAG = WebsiteChooserAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_WEBSITE = 0;
    public static final int VIEW_TYPE_CUSTOM  = 1;
    public static final int VIEW_TYPE_LOAD    = 2;

    /**
     * Null list = no data, empty list = data loaded but nothing to display
     */
    @Nullable
    private List<Website>             mWebsites;
    @Nullable
    private WebsiteViewHolderListener mViewHolderListener;

    public WebsiteChooserAdapter(@Nullable WebsiteViewHolderListener viewHolderListener) {
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
            case VIEW_TYPE_WEBSITE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_websitechooser, parent, false);
                return new WebsiteViewHolder(v);
            case VIEW_TYPE_CUSTOM:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_websitechooser_custom, parent, false);
                return new CustomAddViewHolder(v);
            case VIEW_TYPE_LOAD:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loader, parent, false);
                return new LoadViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(BaseWebsiteViewHolder holder, int position) {
        if (mWebsites != null && position < mWebsites.size()) {
            holder.setData(mWebsites.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (mWebsites != null) {
            return mWebsites.size() + 1;
        }
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mWebsites != null && mWebsites.size() > 0 && position < mWebsites.size()) {
            return VIEW_TYPE_WEBSITE;
        } else if (mWebsites != null && mWebsites.size() == position) {
            return VIEW_TYPE_CUSTOM;
        } else if (mWebsites == null && position == 1) {
            return VIEW_TYPE_CUSTOM;
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

        @BindView(R.id.checkBox)
        protected CheckBox checkBox;

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
            if (mViewHolderListener != null && mWebsites != null) {
                mViewHolderListener.onClick(mWebsites.get(getAdapterPosition()));
            }
        }
    }

    public class CustomAddViewHolder extends BaseWebsiteViewHolder implements View.OnClickListener {

        public CustomAddViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData(Website website) {
            // Static view, nothing to set
        }

        @Override
        public void onClick(View v) {
            if (mViewHolderListener != null) {
                mViewHolderListener.onClick(getAdapterPosition());
            }
        }
    }

    public class LoadViewHolder extends BaseWebsiteViewHolder {

        public LoadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(Website website) {

        }
    }
}
