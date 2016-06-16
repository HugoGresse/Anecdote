package io.gresse.hugo.anecdote.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.MainActivity;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.adapter.AnecdoteAdapter;
import io.gresse.hugo.anecdote.adapter.AnecdoteViewHolderListener;
import io.gresse.hugo.anecdote.adapter.MixedContentAdapter;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.FullscreenEvent;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.util.FabricUtils;
import io.gresse.hugo.anecdote.util.Utils;
import io.gresse.hugo.anecdote.util.chrome.ChromeCustomTabsManager;

/**
 * A generic anecdote fragment
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        AnecdoteViewHolderListener {

    private static final String TAG                      = AnecdoteFragment.class.getSimpleName();
    public static final  String ARGS_WEBSITE_PARENT_SLUG = "key_website_parent_slug";
    public static final  String ARGS_WEBSITE_PAGE_SLUG   = "key_website_page_slug";
    public static final  String ARGS_WEBSITE_NAME        = "key_website_name";

    @Bind(R.id.swipeRefreshLayout)
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    protected String          mWebsiteParentSlug;
    protected String          mWebsiteSlug;
    protected String          mWebsiteName;
    protected AnecdoteAdapter mAdapter;
    @Nullable
    protected AnecdoteService mAnecdoteService;
    protected boolean         mIsLoadingNewItems;

    private LinearLayoutManager mLayoutManager;
    private int                 mTotalItemCount;
    private int                 mLastVisibleItem;
    private int                 mNextPageNumber;
    // TODO: check all loaded
    private boolean             mAllAnecdotesLoaded;

    @Nullable
    private ChromeCustomTabsManager mChromeCustomTabsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mChromeCustomTabsManager = new ChromeCustomTabsManager(getActivity());
        mChromeCustomTabsManager.bindCustomTabsService(getActivity());
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_anecdote, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();


                // Scrolled to bottom. Do something here.
                if (!mIsLoadingNewItems && mLastVisibleItem == mTotalItemCount - 4 && !mAllAnecdotesLoaded) {
                    if (mAnecdoteService != null && mAnecdoteService.getWebsitePage().isSinglePage) {
                        return;
                    }
                    mIsLoadingNewItems = true;
                    Log.d(TAG, "Scrolled to end, load new anecdotes");
                    loadNewAnecdotes(mNextPageNumber);
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);

        init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new ChangeTitleEvent(mWebsiteSlug));

        FabricUtils.trackFragmentView(this, mWebsiteName);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        if (mChromeCustomTabsManager != null) {
            mChromeCustomTabsManager.unbindCustomTabsService(getActivity());
        }
        super.onStop();
    }

    protected void init() {
        if (getArguments() != null) {
            mWebsiteParentSlug = getArguments().getString(ARGS_WEBSITE_PARENT_SLUG);
            mWebsiteSlug = getArguments().getString(ARGS_WEBSITE_PAGE_SLUG);
            mWebsiteName = getArguments().getString(ARGS_WEBSITE_NAME);
        }

        // Get text pref
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int textSize = Integer.parseInt(preferences.getString(getString(R.string.pref_textsize_key), String.valueOf(getResources().getInteger(R.integer.anecdote_textsize_default))));
        boolean rowStripping = preferences.getBoolean(getString(R.string.pref_rowstriping_key), getResources().getBoolean(R.bool.pref_rowstripping_default));

        mAnecdoteService = ((MainActivity) getActivity()).getAnecdoteService(mWebsiteSlug);

        if (mAnecdoteService == null) {
            Log.e(TAG, "Unable to get an AnecdoteService");
            return;
        }

        mAdapter = new MixedContentAdapter(this, mAnecdoteService.getWebsitePage().isSinglePage);

        mRecyclerView.setAdapter(mAdapter);

        // Set default values
        mIsLoadingNewItems = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdapter.setTextStyle(textSize, rowStripping, getResources().getColor(R.color.colorBackgroundStripping, null));
        } else {
            // noinspection deprecation
            mAdapter.setTextStyle(textSize, rowStripping, getResources().getColor(R.color.colorBackgroundStripping));
        }

        mAdapter.setData(mAnecdoteService.getAnecdotes());

        if (mAnecdoteService.getAnecdotes().isEmpty()) {
            loadNewAnecdotes(mNextPageNumber);
        }
    }

    /**
     * To be called by child fragment when a request if finished, could be an error or success.
     *
     * @param dataChanged true if the dataSet has changed, like new Anecdote is here!
     */
    protected void afterRequestFinished(boolean dataChanged) {
        mIsLoadingNewItems = false;
        mSwipeRefreshLayout.setRefreshing(false);

        if (dataChanged && mAnecdoteService != null) {
            mNextPageNumber++;
            mAdapter.setData(mAnecdoteService.getAnecdotes());
        }
    }

    /**
     * Post a new event to load a new event page
     *
     * @param page the page to load
     */
    protected void loadNewAnecdotes(int page) {
        EventBus.getDefault().post(new LoadNewAnecdoteEvent(mWebsiteSlug, page));
    }


    /***************************
     * Implement SwipeRefreshLayout.OnRefreshListener
     ***************************/

    @Override
    public void onRefresh() {
        if (mAnecdoteService == null) {
            return;
        }
        mAnecdoteService.cleanAnecdotes();
        loadNewAnecdotes(mNextPageNumber = 0);
    }


    /***************************
     * Implement ViewHolderListener
     **************************/

    @Override
    public void onClick(Anecdote anecdote, View view) {
        String contentUrl;
        if (anecdote.media == null) {
            if (mChromeCustomTabsManager != null) {
                FabricUtils.trackAnecdoteDetails(mWebsiteName);
                mChromeCustomTabsManager.openChrome(getActivity(), anecdote);
            }
            return;
        }

        contentUrl = anecdote.media;

        switch (anecdote.type) {
            case MediaType.IMAGE:
                EventBus.getDefault().post(new FullscreenEvent(
                        FullscreenEvent.TYPE_IMAGE,
                        this,
                        view,
                        getString(R.string.anecdote_image_transition_name),
                        contentUrl
                ));
                break;
            case MediaType.VIDEO:
                EventBus.getDefault().post(new FullscreenEvent(
                        FullscreenEvent.TYPE_VIDEO,
                        this,
                        view,
                        getString(R.string.anecdote_image_transition_name),
                        contentUrl
                ));
                break;
            default:
                Log.w(TAG, "Not managed RichContent type");
                break;
        }

    }

    @Override
    public void onLongClick(final Object object) {
        if (!(object instanceof Anecdote)) {
            return;
        }
        final Anecdote anecdote = (Anecdote) object;
        // Open a dialog picker on item long click to choose between Open details, Share or copy the text

        if (mChromeCustomTabsManager != null && !TextUtils.isEmpty(anecdote.permalink)) {
            mChromeCustomTabsManager.mayLaunch(anecdote.permalink);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.anecdote_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                switch (which) {
                    // Share
                    case 0:
                        FabricUtils.trackAnecdoteShare(mWebsiteName);

                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");

                        sharingIntent.putExtra(
                                Intent.EXTRA_SUBJECT,
                                getString(R.string.app_name));

                        sharingIntent.putExtra(
                                Intent.EXTRA_TEXT,
                                anecdote.getPlainTextContent() + " " + getString(R.string.app_share_credits));

                        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.anecdote_share_title)));
                        break;
                    // Open details
                    case 1:
                        FabricUtils.trackAnecdoteDetails(mWebsiteName);
                        mChromeCustomTabsManager.openChrome(getActivity(), anecdote);
                        break;
                    // Copy
                    case 2:
                        FabricUtils.trackAnecdoteCopy(mWebsiteName);
                        Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();
                        Utils.copyToClipboard(
                                getActivity(),
                                getString(R.string.app_name),
                                anecdote.getPlainTextContent() + " " + getString(R.string.app_share_credits));
                        break;
                    default:
                        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builder.show();
    }


    /***************************
     * Event
     ***************************/

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRequestFailedEvent(RequestFailedEvent event) {
        if (event.originalEvent instanceof LoadNewAnecdoteEvent &&
                !((LoadNewAnecdoteEvent) event.originalEvent).websitePageSlug.equals(mWebsiteSlug)) return;

        EventBus.getDefault().removeStickyEvent(event.getClass());
        afterRequestFinished(false);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAnecdoteReceived(OnAnecdoteLoadedEvent event) {
        if (!event.websitePageSlug.equals(mWebsiteSlug)) return;

        EventBus.getDefault().removeStickyEvent(event.getClass());
        afterRequestFinished(true);
    }

    @Subscribe
    public void onUpdateAnecdoteFragment(UpdateAnecdoteFragmentEvent event) {
        init();
        EventBus.getDefault().post(new ChangeTitleEvent(mWebsiteSlug));
    }
}
