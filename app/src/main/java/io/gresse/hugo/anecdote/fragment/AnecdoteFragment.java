package io.gresse.hugo.anecdote.fragment;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.MainActivity;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.adapter.AnecdoteAdapter;
import io.gresse.hugo.anecdote.adapter.MixedContentAdapter;
import io.gresse.hugo.anecdote.adapter.TextAdapter;
import io.gresse.hugo.anecdote.adapter.AnecdoteViewHolderListener;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeTitleEvent;
import io.gresse.hugo.anecdote.event.FullscreenEvent;
import io.gresse.hugo.anecdote.event.LoadNewAnecdoteEvent;
import io.gresse.hugo.anecdote.event.OnAnecdoteLoadedEvent;
import io.gresse.hugo.anecdote.event.RequestFailedEvent;
import io.gresse.hugo.anecdote.event.UpdateAnecdoteFragmentEvent;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.RichContent;
import io.gresse.hugo.anecdote.service.AnecdoteService;
import io.gresse.hugo.anecdote.util.FabricUtils;
import io.gresse.hugo.anecdote.util.Utils;

/**
 * A generic anecdote fragment
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class AnecdoteFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        AnecdoteViewHolderListener {

    private static final String TAG               = AnecdoteFragment.class.getSimpleName();
    public static final  String ARGS_WEBSITE_ID   = "key_website_id";
    public static final  String ARGS_WEBSITE_NAME = "key_website_name";

    @Bind(R.id.swipeRefreshLayout)
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    protected int             mWebsiteId;
    protected String          mWebsiteName;
    protected AnecdoteAdapter mAdapter;
    @Nullable
    protected AnecdoteService mAnecdoteService;
    protected boolean         mIsLoadingNewItems;

    private LinearLayoutManager mLayoutManager;
    private int                 mTotalItemCount;
    private int                 mLastVisibleItem;
    // TODO: check all loaded
    private boolean             mAllAnecdotesLoaded;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                if (!mIsLoadingNewItems && mLastVisibleItem == mTotalItemCount - 1 && !mAllAnecdotesLoaded) {
                    mIsLoadingNewItems = true;
                    Log.d(TAG, "Scrolled to end, load new anecdotes");
                    loadNewAnecdotes(mTotalItemCount);
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

        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new ChangeTitleEvent(mWebsiteId));

        FabricUtils.trackFragmentView(this, mWebsiteName);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    protected void init() {
        if (getArguments() != null) {
            mWebsiteId = getArguments().getInt(ARGS_WEBSITE_ID);
            mWebsiteName = getArguments().getString(ARGS_WEBSITE_NAME);
        }

        // Get text pref
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int textSize = Integer.parseInt(preferences.getString(getString(R.string.pref_textsize_key), String.valueOf(getResources().getInteger(R.integer.anecdote_textsize_default))));
        boolean rowStripping = preferences.getBoolean(getString(R.string.pref_rowstriping_key), getResources().getBoolean(R.bool.pref_rowstripping_default));

        mAnecdoteService = ((MainActivity) getActivity()).getAnecdoteService(mWebsiteId);

        if (mAnecdoteService == null) {
            Log.e(TAG, "Unable to get an AnecdoteService");
            return;
        }

        if (mAnecdoteService.getWebsite().hasAdditionalContent()) {
            mAdapter = new MixedContentAdapter(this);
        } else {
            mAdapter = new TextAdapter(this);
        }

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
            loadNewAnecdotes(0);
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
            mAdapter.setData(mAnecdoteService.getAnecdotes());
        }
    }

    protected void loadNewAnecdotes(int start) {
        BusProvider.getInstance().post(new LoadNewAnecdoteEvent(mWebsiteId, start));
    }


    /***************************
     * Implement SwipeRefreshLayout.OnRefreshListener
     ***************************/

    @Override
    public void onRefresh() {
        if(mAnecdoteService == null){
            return;
        }
        mAnecdoteService.cleanAnecdotes();
        loadNewAnecdotes(0);
    }


    /***************************
     * Implement ViewHolderListener
     **************************/

    @Override
    public void onClick(Anecdote anecdote, View view) {

        String contentUrl;
        if (anecdote.mixedContent == null) {
            return;
        }

        contentUrl = anecdote.mixedContent.contentUrl;

        switch (anecdote.mixedContent.type) {
            case RichContent.TYPE_IMAGE:
                BusProvider.getInstance().post(new FullscreenEvent(
                        FullscreenEvent.TYPE_IMAGE,
                        this,
                        view,
                        getString(R.string.anecdote_image_transition_name),
                        contentUrl
                ));
                break;
            case RichContent.TYPE_VIDEO:
                BusProvider.getInstance().post(new FullscreenEvent(
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
        // Open a dialog picker on item long click to choose between Open details, Share or copy the content

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.anecdote_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                switch (which) {
                    // Share
                    case 0:
                        FabricUtils.trackAnecdoteShare(mWebsiteName);

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");

                        sharingIntent.putExtra(
                                android.content.Intent.EXTRA_SUBJECT,
                                getString(R.string.app_name));

                        sharingIntent.putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                anecdote.getPlainTextContent() + " " + getString(R.string.app_share_credits));

                        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.anecdote_share_title)));
                        break;
                    // Open details
                    case 1:
                        try {
                            FabricUtils.trackAnecdoteDetails(mWebsiteName);
                            Toast.makeText(getActivity(), R.string.open_intent_browser, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(anecdote.permalink)));
                        } catch (ActivityNotFoundException exception) {
                            Toast.makeText(getActivity(), R.string.open_intent_browser_error, Toast.LENGTH_SHORT).show();
                        }
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

    @Subscribe
    public void onRequestFailedEvent(RequestFailedEvent event) {
        if (event.websiteId != mWebsiteId) return;
        afterRequestFinished(false);
    }

    @Subscribe
    public void onAnecdoteReceived(OnAnecdoteLoadedEvent event) {
        if (event.websiteId != mWebsiteId) return;
        afterRequestFinished(true);
    }

    @Subscribe
    public void onUpdateAnecdoteFragment(UpdateAnecdoteFragmentEvent event) {
        init();
        BusProvider.getInstance().post(new ChangeTitleEvent(mWebsiteId));
    }
}
