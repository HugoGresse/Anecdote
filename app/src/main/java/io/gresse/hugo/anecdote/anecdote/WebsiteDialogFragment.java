package io.gresse.hugo.anecdote.anecdote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.api.model.Content;
import io.gresse.hugo.anecdote.api.model.ContentItem;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.storage.SpStorage;
import io.gresse.hugo.anecdote.tracking.EventTracker;

/**
 * FialogFragment to edit or add wesites
 * <p/>
 * Created by Hugo Gresse on 29/02/16.
 */
public class WebsiteDialogFragment extends AppCompatDialogFragment {

    public static final String ARGS_WEBSITE = "args_website";

    @BindView(R.id.nameContainer)
    public TextInputLayout mNameTextInputLayout;
    @BindView(R.id.nameEditText)
    public EditText        mNameEditText;
    @BindView(R.id.pageNameContainer)
    public TextInputLayout mPageNameTextInputLayout;
    @BindView(R.id.pageNameEditText)
    public EditText        mPageNameEditText;
    @BindView(R.id.urlContainer)
    public TextInputLayout mUrlTextInputLayout;
    @BindView(R.id.urlEditText)
    public EditText        mUrlEditText;
    @BindView(R.id.urlSuffixEditText)
    public EditText        mUrlSuffixEditText;
    @BindView(R.id.selectorContainer)
    public TextInputLayout mSelectorTextInputLayout;
    @BindView(R.id.selectorEditText)
    public EditText        mSelectorEditText;
    @BindView(R.id.firstPageZeroSwitchCompat)
    public SwitchCompat    mFirstPageZeroSwitchCompat;
    @BindView(R.id.saveButton)
    public Button          mSaveButton;

    protected Website     mWebsite;
    protected WebsitePage mWebsitePage;
    protected boolean     mEditMode;
    private   Unbinder    mUnbinder;

    public static WebsiteDialogFragment newInstance(@Nullable Website website) {
        WebsiteDialogFragment frag = new WebsiteDialogFragment();
        if (website != null) {
            Bundle args = new Bundle();
            args.putString(ARGS_WEBSITE, new Gson().toJson(website));
            frag.setArguments(args);
        }
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_website, container);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString(ARGS_WEBSITE))) {
            mWebsite = new Gson().fromJson(
                    getArguments().getString(ARGS_WEBSITE),
                    new TypeToken<Website>() {
                    }.getType());
            mWebsitePage = mWebsite.pages.get(0);
            initEdit();
        } else {
            mWebsite = new Website();
            mWebsitePage = new WebsitePage();
            initAdd();
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDataCorrect()) {
                    return;
                }
                mWebsite.name = mNameEditText.getText().toString();

                mWebsitePage.name = mPageNameEditText.getText().toString();
                mWebsitePage.url = mUrlEditText.getText().toString();
                mWebsitePage.urlSuffix = mUrlSuffixEditText.getText().toString();
                mWebsitePage.isFirstPageZero = mFirstPageZeroSwitchCompat.isChecked();
                mWebsitePage.selector = mSelectorEditText.getText().toString();
                if (mWebsitePage.content == null) {
                    mWebsitePage.content = new Content();
                    mWebsitePage.content.items.add(new ContentItem(MediaType.TEXT, 1));
                }

                if (mWebsite.pages.size() > 0) {
                    mWebsite.pages.set(0, mWebsitePage);
                } else {
                    mWebsite.pages.add(mWebsitePage);
                }

                SpStorage.saveWebsite(getContext(), mWebsite);

                if (mEditMode) {
                    EventTracker.trackWebsiteEdit(mWebsite.name, true);
                } else {
                    EventTracker.trackCustomWebsiteAdded();
                }

                EventBus.getDefault().post(new WebsitesChangeEvent());
                WebsiteDialogFragment.this.getDialog().dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    protected void initAdd() {
        getDialog().setTitle(R.string.dialog_website_new_title);
        mSaveButton.setText(R.string.dialog_website_add);
    }

    protected void initEdit() {
        mEditMode = true;
        getDialog().setTitle(R.string.dialog_website_edit_title);
        mNameEditText.setText(mWebsite.name);
        mPageNameEditText.setText(mWebsitePage.name);
        mUrlEditText.setText(mWebsitePage.url);
        mUrlSuffixEditText.setText(mWebsitePage.urlSuffix);
        mSelectorEditText.setText(mWebsitePage.selector);
        mFirstPageZeroSwitchCompat.setChecked(mWebsitePage.isFirstPageZero);
    }

    protected boolean isDataCorrect() {

        if (TextUtils.isEmpty(mNameEditText.getText().toString())) {
            mNameTextInputLayout.setErrorEnabled(true);
            mNameTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_name));
            mNameEditText.requestLayout();
            return false;
        } else {
            mNameTextInputLayout.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(mPageNameEditText.getText().toString())) {
            mPageNameTextInputLayout.setErrorEnabled(true);
            mPageNameTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_pagename));
            mPageNameEditText.requestLayout();
            return false;
        } else {
            mPageNameTextInputLayout.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(mUrlEditText.getText().toString())) {
            mUrlTextInputLayout.setErrorEnabled(true);
            mUrlTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_url));
            mUrlEditText.requestLayout();
            return false;
        } else {
            mUrlTextInputLayout.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(mSelectorEditText.getText().toString())) {
            mSelectorTextInputLayout.setErrorEnabled(true);
            mSelectorTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_selector));
            mSelectorEditText.requestLayout();
            return false;
        } else {
            mSelectorTextInputLayout.setErrorEnabled(false);
        }

        return true;
    }
}
