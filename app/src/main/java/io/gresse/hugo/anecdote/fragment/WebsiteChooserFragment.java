package io.gresse.hugo.anecdote.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gresse.hugo.anecdote.Configuration;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.adapter.WebsiteChooserAdapter;
import io.gresse.hugo.anecdote.adapter.WebsiteViewHolderListener;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.WebsitesChangeEvent;
import io.gresse.hugo.anecdote.event.network.NetworkConnectivityChangeEvent;
import io.gresse.hugo.anecdote.model.Website;
import io.gresse.hugo.anecdote.storage.SpStorage;
import io.gresse.hugo.anecdote.util.FabricUtils;
import io.gresse.hugo.anecdote.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Display a list of website from Firebase so the user can choose which website he want to load after initial
 * application launch
 * <p/>
 * Created by Hugo Gresse on 03/03/16.
 */
public class WebsiteChooserFragment extends Fragment implements WebsiteViewHolderListener {

    @SuppressWarnings("unused")
    public static final String TAG = WebsiteChooserFragment.class.getSimpleName();

    public static final String BUNDLE_MODE_KEY     = "modeKey";
    public static final String BUNDLE_MODE_RESTORE = "restore";
    public static final String BUNDLE_MODE_ADD     = "add";

    @Bind(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    @Bind(R.id.saveButton)
    public Button mSaveButton;

    @Nullable
    protected String                mMode;
    protected OkHttpClient          mOkHttpClient;
    @Nullable
    protected Request               mFailRequest;
    private   List<Website>         mWebsites;
    private   WebsiteChooserAdapter mAdapter;
    private   List<Website>         mSelectedWebsites;

    public static WebsiteChooserFragment newInstance(String mode) {
        WebsiteChooserFragment fragment = new WebsiteChooserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_MODE_KEY, mode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_websitechooser, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            mMode = getArguments().getString(WebsiteChooserFragment.BUNDLE_MODE_KEY, null);
        }

        mSelectedWebsites = new ArrayList<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new WebsiteChooserAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mOkHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Configuration.API_URL)
                .header("User-Agent", Utils.getUserAgent())
                .build();

        getWebsites(request);

        if (!TextUtils.isEmpty(mMode) && mMode.equals(BUNDLE_MODE_ADD)) {
            mSaveButton.setText(R.string.dialog_website_add);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new ChangeTitleEvent(
                getString(R.string.dialog_websitechooser_title),
                this.getClass().getName()));


        if (mWebsites != null && !mWebsites.isEmpty()) {
            mAdapter.setData(mWebsites);
        }

        FabricUtils.trackFragmentView(this, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @OnClick(R.id.saveButton)
    public void onSaveButtonClicked() {
        // If we are on the first launch, we assume the user didn't have any Websites, so we override it.
        // Else, we add the selected one
        if (TextUtils.isEmpty(mMode)) {
            SpStorage.saveWebsites(getActivity(), mSelectedWebsites);
            SpStorage.setFirstLaunch(getActivity(), false);
            BusProvider.getInstance().post(new WebsitesChangeEvent(true));
        } else if (mMode.equals(BUNDLE_MODE_RESTORE)) {
            SpStorage.saveWebsites(getActivity(), mSelectedWebsites);
            FabricUtils.trackWebsitesRestored();
            BusProvider.getInstance().post(new WebsitesChangeEvent(true));
        } else if (mMode.equals(BUNDLE_MODE_ADD)) {
            for (Website website : mSelectedWebsites) {
                SpStorage.saveWebsite(getActivity(), website);
            }
            BusProvider.getInstance().post(new WebsitesChangeEvent(false));
        }
    }

    /***************************
     * Inner method
     ***************************/

    private void getWebsites(final Request request) {
        mFailRequest = null;
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mFailRequest = request;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mFailRequest = request;
                    if (getActivity() != null) {
                        Toast
                                .makeText(
                                        getActivity(),
                                        getActivity().getString(R.string.error_server_unknown),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                    return;
                }
                // We are not on main thread
                String websitesString = response.body().string();

                websitesString = java.net.URLDecoder.decode(websitesString, "UTF-8");

                Type type = new TypeToken<HashMap<String, Website>>() {
                }.getType();

                Map<String, Website> websites = new Gson().fromJson(websitesString, type);

                if (websites == null || websites.isEmpty()) {
                    return;
                }

                mWebsites = new ArrayList<>();

                if (TextUtils.isEmpty(mMode) || mMode.equals(BUNDLE_MODE_RESTORE)) {

                    for (Map.Entry<String, Website> entry : websites.entrySet()) {
                        mWebsites.add(entry.getValue());
                    }

                } else if (getContext() != null) {
                    // We want to add some websites : remove duplicates or already added ones
                    List<Website> savedWebsite = SpStorage.getWebsites(getActivity());

                    for (Map.Entry<String, Website> entry : websites.entrySet()) {
                        if (savedWebsite.contains(entry.getValue())) {
                            continue;
                        }
                        mWebsites.add(entry.getValue());
                    }
                }

                Collections.sort(mWebsites, new Comparator<Website>() {
                    @Override
                    public int compare(Website website, Website website2) {
                        if (website.like > website2.like) {
                            return -1;
                        } else if (website.like < website2.like) {
                            return 1;
                        }
                        return 0;
                    }
                });

                if (getActivity() != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.setData(mWebsites);
                        }
                    });
                }
            }
        });

    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onConnectivityChange(NetworkConnectivityChangeEvent event) {
        if (mFailRequest != null) {
            getWebsites(mFailRequest);
        }
    }

    /***************************
     * ViewHolderListener
     ***************************/

    @Override
    public void onClick(Object object) {
        if(object instanceof Integer){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogFragment dialogFragment = WebsiteDialogFragment.newInstance(null);
            dialogFragment.show(fm, dialogFragment.getClass().getSimpleName());
        } else if (object instanceof Website){

            Website website = (Website) object;
            if (mSelectedWebsites.contains(website)) {
                mSelectedWebsites.remove(website);
            } else {
                mSelectedWebsites.add(website);
            }
        }
    }
}
