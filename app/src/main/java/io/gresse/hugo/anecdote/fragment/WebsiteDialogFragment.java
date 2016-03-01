package io.gresse.hugo.anecdote.fragment;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.model.Website;
import io.gresse.hugo.anecdote.util.SharedPreferencesStorage;

/**
 * FialogFragment to edit or add wesites
 * <p/>
 * Created by Hugo Gresse on 29/02/16.
 */
public class WebsiteDialogFragment extends AppCompatDialogFragment {

    public static final String ARGS_WEBSITE = "args_website";

    @Bind(R.id.nameContainer)
    public TextInputLayout mNameTextInputLayout;
    @Bind(R.id.nameEditText)
    public EditText        mNameEditText;
    @Bind(R.id.urlContainer)
    public TextInputLayout mUrlTextInputLayout;
    @Bind(R.id.urlEditText)
    public EditText        mUrlEditText;
    @Bind(R.id.urlSuffixContainer)
    public TextInputLayout mUrlSuffixTextInputLayout;
    @Bind(R.id.urlSuffixEditText)
    public EditText        mUrlSuffixEditText;
    @Bind(R.id.selectorContainer)
    public TextInputLayout mSelectorTextInputLayout;
    @Bind(R.id.selectorEditText)
    public EditText        mSelectorEditText;
    @Bind(R.id.itemPerPageContainer)
    public TextInputLayout mItemPerPageInputLayout;
    @Bind(R.id.itemPerPageEditText)
    public EditText        mItemPerPageEditText;
    @Bind(R.id.firstPageZeroSwitchCompat)
    public SwitchCompat    mFirstPageZeroSwitchCompat;
    @Bind(R.id.saveButton)
    public Button          mSaveButton;

    protected Website mWebsite;
    protected boolean mEditMode;

    public static WebsiteDialogFragment newInstance(@Nullable Website website) {
        WebsiteDialogFragment frag = new WebsiteDialogFragment();
        if(website != null){
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
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        getDialog().getWindow().setLayout(width, getDialog().getWindow().getAttributes().height);

        if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString(ARGS_WEBSITE))) {
            mWebsite = new Gson().fromJson(
                    getArguments().getString(ARGS_WEBSITE),
                    new TypeToken<Website>() {
                    }.getType());
            initEdit();
        } else {
            mWebsite = new Website();
            initAdd();
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDataCorrect()){
                    return;
                }
                mWebsite.name = mNameEditText.getText().toString();
                mWebsite.pageUrl = mUrlEditText.getText().toString();
                mWebsite.pageSuffix = mUrlSuffixEditText.getText().toString();
                mWebsite.itemSelector = mSelectorEditText.getText().toString();
                mWebsite.itemPerPage = Integer.parseInt(mItemPerPageEditText.getText().toString());
                mWebsite.isFirstPageZero = mFirstPageZeroSwitchCompat.isChecked();

                SharedPreferencesStorage.saveWebsite(getContext(), mWebsite);
                BusProvider.getInstance().post(new WebsitesChangeEvent());
                WebsiteDialogFragment.this.getDialog().hide();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    protected void initAdd() {
        getDialog().setTitle(R.string.dialog_website_new_title);
        mSaveButton.setText(R.string.dialog_website_add);
    }

    protected void initEdit() {
        mEditMode = true;
        getDialog().setTitle(R.string.dialog_website_edit_title);
        mNameEditText.setText(mWebsite.name);
        mUrlEditText.setText(mWebsite.pageUrl);
        mUrlSuffixEditText.setText(mWebsite.pageSuffix);
        mSelectorEditText.setText(mWebsite.itemSelector);
        mItemPerPageEditText.setText(String.valueOf(mWebsite.itemPerPage));
        mFirstPageZeroSwitchCompat.setChecked(mWebsite.isFirstPageZero);
    }

    protected boolean isDataCorrect(){

        if(TextUtils.isEmpty(mNameEditText.getText().toString())){
            mNameTextInputLayout.setErrorEnabled(true);
            mNameTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_name));
            mNameEditText.requestLayout();
            return false;
        } else {
            mNameTextInputLayout.setErrorEnabled(false);
        }

        if(TextUtils.isEmpty(mUrlEditText.getText().toString())){
            mUrlTextInputLayout.setErrorEnabled(true);
            mUrlTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_url));
            mUrlEditText.requestLayout();
            return false;
        } else {
            mUrlTextInputLayout.setErrorEnabled(false);
        }

        if(TextUtils.isEmpty(mSelectorEditText.getText().toString())){
            mSelectorTextInputLayout.setErrorEnabled(true);
            mSelectorTextInputLayout.setError(getContext().getString(R.string.dialog_website_error_selector));
            mSelectorEditText.requestLayout();
            return false;
        } else {
            mSelectorTextInputLayout.setErrorEnabled(false);
        }

        if(TextUtils.isEmpty(mItemPerPageEditText.getText().toString())){
            mItemPerPageInputLayout.setErrorEnabled(true);
            mItemPerPageInputLayout.setError(getContext().getString(R.string.dialog_website_error_itemperpage));
            mItemPerPageEditText.requestLayout();
            return false;
        } else {
            mItemPerPageInputLayout.setErrorEnabled(false);
        }

        return true;
    }


}
