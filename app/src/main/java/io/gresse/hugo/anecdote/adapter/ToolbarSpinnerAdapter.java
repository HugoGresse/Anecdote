package io.gresse.hugo.anecdote.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.api.Website;
import io.gresse.hugo.anecdote.model.api.WebsitePage;

/**
 * Adapter for the toolbar spinner
 * <p/>
 * Created by Hugo Gresse on 31/05/16.
 */
public class ToolbarSpinnerAdapter extends BaseAdapter {

    private final Context      mContext;
    private       Website      mWebsite;
    private       String       mTitle;
    private       List<String> mItems;

    public ToolbarSpinnerAdapter(Context context, String title, List<String> items) {
        //noinspection unchecked
        mContext = context;
        mItems = items;
        mTitle = title;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String item = (String) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.toolbar_spinner_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.title)).setText(mTitle);
        ((TextView) convertView.findViewById(R.id.subtitle)).setText(item);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        String item = (String) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.toolbar_spinner_dropdown_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.title)).setText(item);

        return convertView;
    }

    /**
     * Populate the spinner with the given website. It will browse it add add the websitep ages name.
     *
     * @param website                 the website to get pages from
     * @param websitePageSlugSelected the slug of the selected item, if any
     * @return the selected item index
     */
    public int populate(Website website, @Nullable String websitePageSlugSelected) {
        mItems.clear();

        mWebsite = website;

        mTitle = website.name;

        int selected = 0;

        int i = 0;
        for (WebsitePage websitePage : website.pages) {
            mItems.add(websitePage.name);
            if (websitePageSlugSelected != null && websitePageSlugSelected.equals(websitePage.slug)) {
                selected = i;
            }
            i++;
        }

        return selected;
    }

    /**
     * Get the current displayed website, if any
     * @return the current website
     */
    @Nullable
    public Website getWebsite() {
        return mWebsite;
    }
}
