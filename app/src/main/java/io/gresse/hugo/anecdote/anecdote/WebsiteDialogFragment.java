package io.gresse.hugo.anecdote.anecdote;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.api.model.Content;
import io.gresse.hugo.anecdote.api.model.ContentItem;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.databinding.FragmentDialogWebsiteBinding;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.storage.SpStorage;

/**
 * FialogFragment to edit or add wesites
 * <p/>
 * Created by Hugo Gresse on 29/02/16.
 */
public class WebsiteDialogFragment extends AppCompatDialogFragment {

    public static final String ARGS_WEBSITE = "args_website";

    private TextInputLayout mNameTextInputLayout;
    private EditText        mNameEditText;
    private TextInputLayout mPageNameTextInputLayout;
    private EditText        mPageNameEditText;
    private TextInputLayout mUrlTextInputLayout;
    private EditText        mUrlEditText;
    private EditText        mUrlSuffixEditText;
    private TextInputLayout mSelectorTextInputLayout;
    private EditText        mSelectorEditText;
    private SwitchCompat    mFirstPageZeroSwitchCompat;
    private Button          mSaveButton;

    protected Website     mWebsite;
    protected WebsitePage mWebsitePage;
    protected boolean     mEditMode;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDialogWebsiteBinding binding = FragmentDialogWebsiteBinding.inflate(inflater, container, false);
        mNameTextInputLayout = binding.nameContainer;
        mNameEditText = binding.nameEditText;
        mPageNameTextInputLayout = binding.pageNameContainer;
        mPageNameEditText = binding.pageNameEditText;
        mUrlTextInputLayout = binding.urlContainer;
        mUrlEditText = binding.urlEditText;
        mUrlSuffixEditText = binding.urlSuffixEditText;
        mSelectorTextInputLayout = binding.selectorContainer;
        mSelectorEditText = binding.selectorEditText;
        mFirstPageZeroSwitchCompat = binding.firstPageZeroSwitchCompat;
        mSaveButton = binding.saveButton;
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

                EventBus.getDefault().post(new WebsitesChangeEvent());
                WebsiteDialogFragment.this.getDialog().dismiss();
            }
        });
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
