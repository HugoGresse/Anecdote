package io.gresse.hugo.anecdote.api.chooser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.gresse.hugo.anecdote.anecdote.WebsiteViewHolderListener;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.databinding.RowLoaderBinding;
import io.gresse.hugo.anecdote.databinding.RowWebsitechooserBinding;
import io.gresse.hugo.anecdote.databinding.RowWebsitechooserCustomBinding;

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

    @NonNull
    @Override
    public BaseWebsiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return switch (viewType) {
            case VIEW_TYPE_CUSTOM -> new CustomAddViewHolder(
                    RowWebsitechooserCustomBinding.inflate(inflater, parent, false).getRoot());
            case VIEW_TYPE_LOAD -> new LoadViewHolder(RowLoaderBinding.inflate(inflater, parent, false).getRoot());
            default -> new WebsiteViewHolder(RowWebsitechooserBinding.inflate(inflater, parent, false));
        };
    }

    @Override
    public void onBindViewHolder(@NonNull BaseWebsiteViewHolder holder, int position) {
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

        private final CheckBox checkBox;

        public WebsiteViewHolder(RowWebsitechooserBinding binding) {
            super(binding.getRoot());
            checkBox = binding.checkBox;
            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void setData(Website website) {
            checkBox.setText(website.name);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mViewHolderListener != null && mWebsites != null) {
                mViewHolderListener.onClick(mWebsites.get(getBindingAdapterPosition()));
            }
        }
    }

    public class CustomAddViewHolder extends BaseWebsiteViewHolder implements View.OnClickListener {

        public CustomAddViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData(Website website) {
            // Static view, nothing to set
        }

        @Override
        public void onClick(View v) {
            if (mViewHolderListener != null) {
                mViewHolderListener.onClick(getBindingAdapterPosition());
            }
        }
    }

    public class LoadViewHolder extends BaseWebsiteViewHolder {

        public LoadViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void setData(Website website) {

        }
    }
}
