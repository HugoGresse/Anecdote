package io.gresse.hugo.anecdote.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Website;

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

    protected Website mWebsite;

    public static WebsiteDialogFragment newInstance(@Nullable Website website) {
        WebsiteDialogFragment frag = new WebsiteDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_WEBSITE, new Gson().toJson(website));
        frag.setArguments(args);
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

        if(getArguments() != null && !TextUtils.isEmpty(getArguments().getString(ARGS_WEBSITE))){
            mWebsite = new Gson().fromJson(
                    getArguments().getString(ARGS_WEBSITE),
                    new TypeToken<Website>() {}.getType());
            initEdit();
        } else {
            mWebsite = new Website();
            initAdd();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    protected void initAdd(){

    }

    protected void initEdit(){
        mNameEditText.setText(mWebsite.name);
    }


}
