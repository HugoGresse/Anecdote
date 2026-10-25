package io.gresse.hugo.anecdote.anecdote.list;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import io.gresse.hugo.anecdote.MainActivity;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.anecdote.fullscreen.FullscreenActivity;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.anecdote.service.event.FavoritesEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.anecdote.service.event.UpdateAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.CopyAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.OpenAnecdoteEvent;
import io.gresse.hugo.anecdote.anecdote.social.ShareAnecdoteEvent;
import io.gresse.hugo.anecdote.api.model.Website;
import io.gresse.hugo.anecdote.api.model.WebsitePage;
import io.gresse.hugo.anecdote.databinding.FragmentAnecdoteBinding;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.util.TitledFragment;
import io.gresse.hugo.anecdote.util.Utils;
import io.gresse.hugo.anecdote.view.EnhancedFrameLayout;

/**
 * A generic anecdote fragment
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteFragment extends TitledFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        AdapterListener {

    private static final String TAG                      = AnecdoteFragment.class.getSimpleName();
    private static final String ARGS_WEBSITE_PARENT_NAME = "key_website_parent_name";
    private static final String ARGS_WEBSITE_PARENT_SLUG = "key_website_parent_slug";
    public static final  String ARGS_WEBSITE_PAGE_SLUG   = "key_website_page_slug";
    private static final String ARGS_WEBSITE_NAME        = "key_website_name";

    /**
     * Define the threshold to load new items. It's the number of items not visible after the current last visible.
     */
    public static final int PREFETECH_THRESHOLD = 4;

    private SwipeRefreshLayout  mSwipeRefreshLayout;
    private EnhancedFrameLayout mRecyclerViewContainer;
    private RecyclerView        mRecyclerView;

    private   String              mWebsiteName;
    protected String              mWebsiteSlug;
    protected String              mPageSlug;
    protected String              mWebsiteAndPageName;
    protected AnecdoteAdapter     mAdapter;
    @Nullable
    protected AnecdoteService     mAnecdoteService;

    private LinearLayoutManager mLayoutManager;
    private boolean             mIsLoadingNewItems;
    private boolean             mNoData;
    private boolean             mEverythingLoaded;
    private int                 mNextPageNumber;

    public static AnecdoteFragment newInstance(Website website, WebsitePage page) {
        AnecdoteFragment fragment = new AnecdoteFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AnecdoteFragment.ARGS_WEBSITE_PARENT_NAME, website.name);
        bundle.putString(AnecdoteFragment.ARGS_WEBSITE_PARENT_SLUG, website.slug);
        bundle.putString(AnecdoteFragment.ARGS_WEBSITE_PAGE_SLUG, page.slug);
        bundle.putString(AnecdoteFragment.ARGS_WEBSITE_NAME, website.name + " " + page.name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentAnecdoteBinding binding = FragmentAnecdoteBinding.inflate(inflater, container, false);
        mSwipeRefreshLayout = binding.swipeRefreshLayout;
        mRecyclerViewContainer = binding.recyclerViewContainer;
        mRecyclerView = binding.recyclerView;
        mRecyclerView.setClipToPadding(false);
        Utils.applyNavigationBarPadding(mRecyclerView);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new ChangeTitleEvent(this, mPageSlug, true));

    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public String getTitle() {
        return mWebsiteName;
    }

    public String getWebsitePageSlug() {
        return mPageSlug;
    }

    protected void init() {
        if (getArguments() != null) {
            mWebsiteName = getArguments().getString(ARGS_WEBSITE_PARENT_NAME);
            mWebsiteSlug = getArguments().getString(ARGS_WEBSITE_PARENT_SLUG);
            mPageSlug = getArguments().getString(ARGS_WEBSITE_PAGE_SLUG);
            mWebsiteAndPageName = getArguments().getString(ARGS_WEBSITE_NAME);
        }

        // Get text pref
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int textSize = Integer.parseInt(preferences.getString(getString(R.string.pref_textsize_key), String.valueOf(getResources().getInteger(R.integer.anecdote_textsize_default))));
        boolean rowStripping = preferences.getBoolean(getString(R.string.pref_rowstriping_key), getResources().getBoolean(R.bool.pref_rowstripping_default));

        mAnecdoteService = ((MainActivity) getActivity()).getAnecdoteService(mPageSlug);

        if (mAnecdoteService == null) {
            Log.e(TAG, "Unable to get an AnecdoteService");
            return;
        }

        mAdapter = new MixedContentAdapter(this, mWebsiteAndPageName);
        mAdapter.setLoaderDisplay(!mAnecdoteService.getWebsitePage().isSinglePage);

        mAdapter.setData(mAnecdoteService.getAnecdotes());
        mRecyclerView.setAdapter((RecyclerView.Adapter) mAdapter);

        // Set default values
        mIsLoadingNewItems = false;

        int colorBackground = ContextCompat.getColor(requireContext(), R.color.rowColorBackground);
        int colorBackgroundStripping = ContextCompat.getColor(requireContext(), R.color.rowColorBackgroundStripping);
        mAdapter.setTextStyle(textSize, rowStripping, colorBackground, colorBackgroundStripping);
    }

    /**
     * To be called by child fragment when a request if finished, could be an error or success.
     *
     * @param dataChanged true if the dataSet has changed, like new Anecdote is here!
     */
    protected void afterRequestFinished(boolean dataChanged) {
        mIsLoadingNewItems = false;

        if (dataChanged && mAnecdoteService != null) {
            if (mAnecdoteService.getAnecdotes().size() == 0) {
                // We don't have any data to display, we prevent any more request and display a nice friendly message.
                mNoData = true;
                mRecyclerViewContainer.displayOverlay(R.layout.overlay_nodata);
                return;
            } else {
                if (mAdapter.getContentItemCount() > 0 &&
                        mAdapter.getContentItemCount() >= mAnecdoteService.getAnecdotes().size()) {
                    mAdapter.setLoaderDisplay(false);
                    mEverythingLoaded = true;
                } else {
                    mAdapter.setLoaderDisplay(true);
                }
                mNextPageNumber++;
                mAdapter.setData(mAnecdoteService.getAnecdotes());
            }
        }

        mRecyclerViewContainer.hideOverlay();

        // A failed request must not chain another one, else a broken website is requested in a tight loop.
        if (dataChanged && shouldLoadNewAnecdote()) {
            Log.d(TAG, "afterRequestFinished, load new anecdotes");
            loadNewAnecdotes(mNextPageNumber);
        }

    }

    /**
     * Post a new event to load a new event page
     *
     * @param page the page to load
     */
    protected void loadNewAnecdotes(int page) {
        mIsLoadingNewItems = true;
        EventBus.getDefault().post(new LoadNewAnecdoteEvent(mPageSlug, page));
    }

    /**
     * Open the given anecdote content in fullscreen if it's an image or video, else in the browser.
     *
     * @param anecdote anecdote to open
     * @param view     view to have nice transition if possible
     */
    private void fullscreenAnecdote(Anecdote anecdote, View view) {
        if (anecdote.media == null) {
            EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsiteAndPageName, anecdote, false));
            return;
        }

        if (anecdote.type == null || mAnecdoteService == null) {
            Log.w(TAG, "Fullscreen not possible");
            return;
        }

        Intent intent = FullscreenActivity.createIntent(
                getContext(),
                mAnecdoteService.getAnecdotes().indexOf(anecdote),
                anecdote,
                mPageSlug,
                view);


        startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(
                        getActivity(),
                        view,
                        view.getTransitionName()).toBundle()
        );
    }


    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // Scrolled to bottom. Do something here.
            if (shouldLoadNewAnecdote()) {
                Log.d(TAG, "Scrolled to end, load new anecdotes");
                loadNewAnecdotes(mNextPageNumber);
            }
        }
    };

    /**
     * Define the condition to preload enw anecdote before arriving at the end of the list
     *
     * @return true if should preload, false otherweise
     */
    private boolean shouldLoadNewAnecdote() {
        int totalItemCount = mAdapter.getContentItemCount();
        int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        boolean willPrefetch = (lastVisibleItem >= (totalItemCount - PREFETECH_THRESHOLD));

        // Scrolled to bottom. Do something here.
        if (!mIsLoadingNewItems && willPrefetch && !mNoData && !mEverythingLoaded) {
            //noinspection RedundantIfStatement
            if (mAnecdoteService != null && mAnecdoteService.getWebsitePage().isSinglePage) {
                return false;
            }
            return true;
        }
        return false;
    }

    /***************************
     * Implement SwipeRefreshLayout.OnRefreshListener
     ***************************/

    @Override
    public void onRefresh() {
        if (mAnecdoteService == null) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);

        // Reset the no data flag
        mNoData = mEverythingLoaded = false;
        mAnecdoteService.cleanAnecdotes();
        mAdapter.setData(new ArrayList<Anecdote>());
        loadNewAnecdotes(mNextPageNumber = 0);
    }


    /***************************
     * Implement ViewHolderListener
     **************************/

    @Override
    public void onClick(Anecdote anecdote, View view, int action) {
        switch (action) {
            default:
            case AdapterListener.ACTION_COPY:
                EventBus.getDefault().post(new CopyAnecdoteEvent(mWebsiteAndPageName, anecdote, CopyAnecdoteEvent.TYPE_ANECDOTE, anecdote.getShareString(getContext())));
                break;
            case AdapterListener.ACTION_SHARE:
                EventBus.getDefault().post(new ShareAnecdoteEvent(mWebsiteAndPageName, anecdote, anecdote.getShareString(getContext())));
                break;
            case AdapterListener.ACTION_OPEN_IN_BROWSER_PRELOAD:
                EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsiteAndPageName, anecdote, true));
                break;
            case AdapterListener.ACTION_OPEN_IN_BROWSER:
                EventBus.getDefault().post(new OpenAnecdoteEvent(mWebsiteAndPageName, anecdote, false));
                break;
            case AdapterListener.ACTION_FULLSCREEN:
                fullscreenAnecdote(anecdote, view);
                break;
            case AdapterListener.ACTION_FAVORIS:
                EventBus.getDefault().post(new FavoritesEvent(mPageSlug, anecdote, !anecdote.isFavorite()));
                break;
        }
    }

    @Override
    public void onLongClick(final Object object) {
        // Nothing here
    }


    /***************************
     * Event
     ***************************/

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRequestFailedEvent(RequestFailedEvent event) {
        if (event.originalEvent instanceof LoadNewAnecdoteEvent &&
                !((LoadNewAnecdoteEvent) event.originalEvent).websitePageSlug.equals(mPageSlug)) return;

        EventBus.getDefault().removeStickyEvent(event.getClass());
        afterRequestFinished(false);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAnecdoteReceived(OnAnecdoteLoadedEvent event) {
        EventBus.getDefault().removeStickyEvent(event.getClass());
        afterRequestFinished(true);
    }

    @Subscribe
    public void onUpdateAnecdoteFragment(UpdateAnecdoteFragmentEvent event) {
        init();
        EventBus.getDefault().post(new ChangeTitleEvent(this, mPageSlug, true));
    }

    @Subscribe
    public void onUpdateAnecdote(UpdateAnecdoteEvent event) {
        if (mAnecdoteService != null) {
            afterRequestFinished(true);
        }
    }
}